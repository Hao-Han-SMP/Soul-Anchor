package dev.haohansmp.soulanchor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SoulAnchorPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private NamespacedKey itemTypeKey;
    private NamespacedKey anchorIdKey;
    private NamespacedKey recipeKey;
    private File anchorsFile;
    private File messagesFile;
    private FileConfiguration anchorsConfig;
    private FileConfiguration messages;
    private final Map<UUID, Anchor> anchorsById = new HashMap<>();
    private final Map<String, UUID> anchorIdsByLocation = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Warmup> warmups = new ConcurrentHashMap<>();
    private BukkitTask idleParticlesTask;

    @Override
    public void onEnable() {
        itemTypeKey = new NamespacedKey(this, "item_type");
        anchorIdKey = new NamespacedKey(this, "anchor_id");
        recipeKey = NamespacedKey.fromString(getConfig().getString("item.id", "haohansmp:soul_anchor"));
        if (recipeKey == null) {
            recipeKey = new NamespacedKey(this, "soul_anchor");
        }

        saveDefaultConfig();
        saveResource("messages.yml", false);
        messagesFile = new File(getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        anchorsFile = new File(getDataFolder(), "anchors.yml");
        anchorsConfig = YamlConfiguration.loadConfiguration(anchorsFile);

        loadAnchors();
        registerRecipe();

        Objects.requireNonNull(getCommand("soulanchor")).setExecutor(this);
        Objects.requireNonNull(getCommand("soulanchor")).setTabCompleter(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        startIdleParticles();
        getLogger().info("Loaded " + anchorsById.size() + " Soul Anchors.");
    }

    @Override
    public void onDisable() {
        for (Warmup warmup : new ArrayList<>(warmups.values())) {
            warmup.cancel(false);
        }
        warmups.clear();
        if (idleParticlesTask != null) {
            idleParticlesTask.cancel();
        }
        saveAnchors();
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (getConfig().getBoolean("recipe.discover-automatically", true)) {
            event.getPlayer().discoverRecipe(recipeKey);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack handItem = event.getItemInHand();
        if (!isSoulAnchorItem(handItem)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("soulanchor.place")) {
            event.setCancelled(true);
            send(player, "no-permission");
            return;
        }
        if (isWorldBlocked(event.getBlockPlaced().getWorld())) {
            event.setCancelled(true);
            send(player, "world-disabled");
            return;
        }

        int limit = getAnchorLimit(player);
        if (limit >= 0 && ownedAnchors(player.getUniqueId()).size() >= limit) {
            event.setCancelled(true);
            send(player, "anchor-limit", "{limit}", String.valueOf(limit));
            return;
        }

        event.setCancelled(true);
        Location location = event.getBlockPlaced().getLocation();
        location.getBlock().setType(getAnchorBlockMaterial(), false);
        consumePlacedItem(player, event.getHand());

        String name = nextDefaultName(player.getUniqueId());
        Anchor anchor = new Anchor(UUID.randomUUID(), player.getUniqueId(), name, location, 0F, 0F, Instant.now().toEpochMilli(), null, null);
        anchor = spawnVisuals(anchor);
        anchorsById.put(anchor.id(), anchor);
        anchorIdsByLocation.put(locationKey(anchor.location()), anchor.id());
        saveAnchors();

        send(player, "anchor-placed", "{name}", anchor.name());
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8F, 1.1F);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction().isLeftClick()) {
            return;
        }
        Anchor anchor = anchorAt(event.getClickedBlock()).orElse(null);
        if (anchor == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!player.hasPermission("soulanchor.use")) {
            send(player, "no-permission");
            return;
        }
        if (!anchor.ownerId().equals(player.getUniqueId()) && !player.hasPermission("soulanchor.admin")) {
            send(player, "anchor-not-owner");
            return;
        }
        openMenu(player, anchor);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Anchor anchor = anchorFromEntity(event.getRightClicked()).orElse(null);
        if (anchor == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!player.hasPermission("soulanchor.use")) {
            send(player, "no-permission");
            return;
        }
        if (!anchor.ownerId().equals(player.getUniqueId()) && !player.hasPermission("soulanchor.admin")) {
            send(player, "anchor-not-owner");
            return;
        }
        openMenu(player, anchor);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AnchorMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        UUID targetId = readAnchorId(item).orElse(null);
        if (targetId == null) {
            if (item.getType() == Material.BARRIER) {
                player.closeInventory();
            }
            return;
        }
        Anchor source = anchorsById.get(holder.sourceAnchorId());
        Anchor target = anchorsById.get(targetId);
        if (source == null || target == null || source.id().equals(target.id())) {
            send(player, "teleport-failed");
            return;
        }
        player.closeInventory();
        requestTeleport(player, source.id(), target.id());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof AnchorMenuHolder) {
            event.getInventory().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Anchor anchor = anchorAt(event.getBlock()).orElse(null);
        if (anchor == null) {
            return;
        }
        Player player = event.getPlayer();
        boolean owner = anchor.ownerId().equals(player.getUniqueId());
        if (!(owner && player.hasPermission("soulanchor.break.own")) && !player.hasPermission("soulanchor.admin.remove")) {
            event.setCancelled(true);
            send(player, "anchor-not-owner");
            return;
        }

        removeAnchor(anchor.id());
        cancelWarmupsTouching(anchor.id());
        if (getConfig().getBoolean("breaking.drop-item", true)) {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), createAnchorItem(1));
        }
        send(player, "anchor-broken", "{name}", anchor.name());
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 1F);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent event) {
        if (anchorAt(event.getBlock()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFluid(BlockFromToEvent event) {
        if (anchorAt(event.getToBlock()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> anchorAt(block).isPresent())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> anchorAt(block).isPresent())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> anchorAt(block).isPresent());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> anchorAt(block).isPresent());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Warmup warmup = warmups.get(event.getPlayer().getUniqueId());
        if (warmup == null || event.getTo() == null) {
            return;
        }
        double tolerance = getConfig().getDouble("teleport.movement-tolerance", 0.5D);
        if (!sameWorld(warmup.startLocation, event.getTo()) || warmup.startLocation.distanceSquared(event.getTo()) > tolerance * tolerance) {
            warmup.cancel(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Warmup warmup = warmups.get(player.getUniqueId());
            if (warmup != null) {
                warmup.cancel(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDealDamage(EntityDamageByEntityEvent event) {
        Anchor anchor = anchorFromEntity(event.getEntity()).orElse(null);
        if (anchor != null) {
            event.setCancelled(true);
            Player breaker = damagerPlayer(event.getDamager());
            if (breaker == null) {
                return;
            }
            boolean owner = anchor.ownerId().equals(breaker.getUniqueId());
            if (!(owner && breaker.hasPermission("soulanchor.break.own")) && !breaker.hasPermission("soulanchor.admin.remove")) {
                send(breaker, "anchor-not-owner");
                return;
            }
            removeAnchor(anchor.id());
            cancelWarmupsTouching(anchor.id());
            anchor.location().getBlock().setType(Material.AIR, false);
            if (getConfig().getBoolean("breaking.drop-item", true)) {
                breaker.getWorld().dropItemNaturally(anchor.location().clone().add(0.5, 0.5, 0.5), createAnchorItem(1));
            }
            send(breaker, "anchor-broken", "{name}", anchor.name());
            return;
        }

        Player player = damagerPlayer(event.getDamager());
        if (player == null) {
            return;
        }
        Warmup warmup = warmups.get(player.getUniqueId());
        if (warmup != null) {
            warmup.cancel(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Warmup warmup = warmups.get(event.getPlayer().getUniqueId());
        if (warmup != null) {
            warmup.cancel(false);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                send(sender, "player-only");
                return true;
            }
            listAnchors(player, player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("give")) {
            return commandGive(sender, args);
        }
        if (sub.equals("list")) {
            return commandList(sender, args);
        }
        if (sub.equals("rename")) {
            return commandRename(sender, args);
        }
        if (sub.equals("remove")) {
            return commandRemove(sender, args);
        }
        if (sub.equals("reload")) {
            if (!sender.hasPermission("soulanchor.admin.reload")) {
                send(sender, "no-permission");
                return true;
            }
            reloadConfig();
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            registerRecipe();
            send(sender, "reloaded");
            return true;
        }
        sender.sendMessage(color("&3SoulAnchor &7commands: &f/soulanchor give|list|rename|remove|reload"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("give", "list", "rename", "remove", "reload"), new ArrayList<>());
        }
        if (args.length == 2 && List.of("give", "list").contains(args[0].toLowerCase(Locale.ROOT))) {
            return null;
        }
        if (sender instanceof Player player && args.length == 2 && List.of("rename", "remove").contains(args[0].toLowerCase(Locale.ROOT))) {
            return StringUtil.copyPartialMatches(args[1], ownedAnchors(player.getUniqueId()).stream().map(Anchor::name).toList(), new ArrayList<>());
        }
        return List.of();
    }

    private boolean commandGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("soulanchor.admin.give")) {
            send(sender, "no-permission");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /soulanchor give <player> [amount]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(color("&cPlayer not found."));
            return true;
        }
        int amount = args.length >= 3 ? parseInt(args[2], 1) : 1;
        amount = Math.max(1, Math.min(64, amount));
        target.getInventory().addItem(createAnchorItem(amount)).values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        send(sender, "given", "{amount}", String.valueOf(amount), "{player}", target.getName());
        return true;
    }

    private boolean commandList(CommandSender sender, String[] args) {
        if (args.length >= 2 && sender.hasPermission("soulanchor.admin")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(color("&cPlayer not found."));
                return true;
            }
            listAnchors(sender, target);
            return true;
        }
        if (!(sender instanceof Player player)) {
            send(sender, "player-only");
            return true;
        }
        listAnchors(sender, player);
        return true;
    }

    private boolean commandRename(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission("soulanchor.rename")) {
            send(player, "no-permission");
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(color("&cUsage: /soulanchor rename <anchor> <new-name>"));
            return true;
        }
        Anchor anchor = findOwnedAnchor(player.getUniqueId(), args[1]).orElse(null);
        if (anchor == null) {
            send(player, "not-anchor");
            return true;
        }
        String name = sanitizeName(String.join(" ", List.of(args).subList(2, args.length)));
        Anchor renamed = anchor.withName(name);
        anchorsById.put(renamed.id(), renamed);
        saveAnchors();
        send(player, "anchor-renamed", "{name}", renamed.name());
        return true;
    }

    private boolean commandRemove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "player-only");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(color("&cUsage: /soulanchor remove <anchor>"));
            return true;
        }
        Anchor anchor = findOwnedAnchor(player.getUniqueId(), args[1]).orElse(null);
        if (anchor == null) {
            send(player, "not-anchor");
            return true;
        }
        removeAnchor(anchor.id());
        cancelWarmupsTouching(anchor.id());
        if (anchor.location().getBlock().getType() == getAnchorBlockMaterial()) {
            anchor.location().getBlock().setType(Material.AIR, false);
        }
        send(player, "anchor-broken", "{name}", anchor.name());
        return true;
    }

    private void listAnchors(CommandSender sender, Player owner) {
        List<Anchor> anchors = ownedAnchors(owner.getUniqueId());
        sender.sendMessage(color("&3Soul Anchors of &f" + owner.getName() + "&7 (" + anchors.size() + "/" + getAnchorLimit(owner) + ")"));
        if (anchors.isEmpty()) {
            sender.sendMessage(color("&7No Soul Anchors yet."));
            return;
        }
        for (Anchor anchor : anchors) {
            Location loc = anchor.location();
            sender.sendMessage(color("&b- &f" + anchor.name() + " &7" + loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
        }
    }

    private void requestTeleport(Player player, UUID sourceId, UUID targetId) {
        if (warmups.containsKey(player.getUniqueId())) {
            send(player, "warmup-cancelled");
            return;
        }
        long now = System.currentTimeMillis();
        long remaining = cooldowns.getOrDefault(player.getUniqueId(), 0L) - now;
        if (remaining > 0 && !player.hasPermission("soulanchor.bypass.cooldown")) {
            send(player, "cooldown", "{seconds}", String.valueOf((remaining + 999) / 1000));
            return;
        }

        Anchor source = anchorsById.get(sourceId);
        Anchor target = anchorsById.get(targetId);
        Validation validation = validateTeleport(player, source, target, false);
        if (!validation.ok()) {
            send(player, validation.messageKey(), validation.replacements());
            return;
        }

        int warmupSeconds = player.hasPermission("soulanchor.bypass.warmup") ? 0 : getConfig().getInt("teleport.warmup-seconds", 3);
        if (warmupSeconds <= 0) {
            finishTeleport(player, sourceId, targetId);
            return;
        }

        send(player, "warmup-start", "{seconds}", String.valueOf(warmupSeconds));
        Warmup warmup = new Warmup(player, sourceId, targetId, player.getLocation().clone(), warmupSeconds);
        warmups.put(player.getUniqueId(), warmup);
        warmup.task = new BukkitRunnable() {
            int remainingTicks = warmupSeconds;

            @Override
            public void run() {
                if (!warmups.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                if (remainingTicks > 0) {
                    send(player, "warmup-tick", "{seconds}", String.valueOf(remainingTicks));
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 8, 0.4, 0.6, 0.4, 0.01);
                    remainingTicks--;
                    return;
                }
                warmups.remove(player.getUniqueId());
                cancel();
                finishTeleport(player, sourceId, targetId);
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void finishTeleport(Player player, UUID sourceId, UUID targetId) {
        Anchor source = anchorsById.get(sourceId);
        Anchor target = anchorsById.get(targetId);
        Validation validation = validateTeleport(player, source, target, true);
        if (!validation.ok()) {
            send(player, validation.messageKey(), validation.replacements());
            return;
        }

        Cost cost = calculateCost(source.location(), target.location());
        Location destination = validation.safeDestination();

        if (!player.hasPermission("soulanchor.bypass.cost")) {
            removeEchoShards(player, cost.shards());
            player.giveExpLevels(-cost.levels());
        }

        boolean teleported = player.teleport(destination);
        if (!teleported) {
            if (!player.hasPermission("soulanchor.bypass.cost")) {
                player.getInventory().addItem(new ItemStack(Material.ECHO_SHARD, cost.shards()));
                player.giveExpLevels(cost.levels());
            }
            send(player, "teleport-failed");
            return;
        }

        int cooldownSeconds = getConfig().getInt("teleport.cooldown-seconds", 30);
        if (!player.hasPermission("soulanchor.bypass.cooldown")) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
        }
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation().add(0, 1, 0), 16, 0.5, 0.8, 0.5, 0.02);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.9F, 1.15F);
        send(player, "teleport-success", "{anchor}", target.name());
    }

    private Validation validateTeleport(Player player, Anchor source, Anchor target, boolean requireSafeDestination) {
        if (source == null || target == null || source.id().equals(target.id())) {
            return Validation.fail("teleport-failed");
        }
        if (!target.ownerId().equals(player.getUniqueId()) && !player.hasPermission("soulanchor.admin")) {
            return Validation.fail("anchor-not-owner");
        }
        if (!isAnchorStillPlaced(source) || !isAnchorStillPlaced(target)) {
            return Validation.fail("teleport-failed");
        }
        if (isWorldBlocked(target.location().getWorld())) {
            return Validation.fail("world-disabled");
        }
        if (!sameWorld(source.location(), target.location()) && !getConfig().getBoolean("cross-dimension.enabled", true)) {
            return Validation.fail("teleport-failed");
        }

        Cost cost = calculateCost(source.location(), target.location());
        if (!player.hasPermission("soulanchor.bypass.cost")) {
            if (player.getLevel() < cost.levels()) {
                return Validation.fail("not-enough-levels", "{required}", String.valueOf(cost.levels()), "{current}", String.valueOf(player.getLevel()));
            }
            if (countEchoShards(player) < cost.shards()) {
                return Validation.fail("not-enough-shards", "{amount}", String.valueOf(cost.shards()));
            }
        }

        if (requireSafeDestination) {
            target.location().getChunk().load(true);
            Location safe = findSafeLocation(target.location());
            if (safe == null) {
                return Validation.fail("unsafe-destination");
            }
            return Validation.ok(safe);
        }
        return Validation.ok(null);
    }

    private void openMenu(Player player, Anchor source) {
        Inventory inventory = Bukkit.createInventory(new AnchorMenuHolder(source.id()), 27, color("&3Soul Anchor Network"));
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, namedItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<Anchor> anchors = ownedAnchors(player.getUniqueId());
        int[] slots = {11, 13, 15};
        for (int i = 0; i < Math.min(slots.length, anchors.size()); i++) {
            Anchor target = anchors.get(i);
            Cost cost = calculateCost(source.location(), target.location());
            boolean current = source.id().equals(target.id());
            List<String> lore = new ArrayList<>();
            Location loc = target.location();
            lore.add("&7World: &f" + loc.getWorld().getName());
            lore.add("&7Coords: &f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            if (current) {
                lore.add("");
                lore.add("&eYou are at this Soul Anchor.");
            } else {
                lore.add("&7Distance: &f" + formatDistance(source.location(), target.location()) + " blocks");
                lore.add("");
                lore.add("&7Cost: &a" + cost.levels() + " levels &7+ &b" + cost.shards() + " Echo Shard");
                lore.add("&fClick to teleport");
            }
            ItemStack icon = namedItem(current ? Material.LODESTONE : Material.RESPAWN_ANCHOR, "&b" + target.name(), lore);
            if (!current) {
                writeAnchorId(icon, target.id());
            }
            inventory.setItem(slots[i], icon);
        }
        inventory.setItem(4, namedItem(Material.ECHO_SHARD, "&bNetwork", List.of("&7Anchors: &f" + anchors.size() + "/" + getAnchorLimit(player), "&7Access: &fOwner only")));
        inventory.setItem(22, namedItem(Material.BARRIER, "&cClose"));
        player.openInventory(inventory);
    }

    private void registerRecipe() {
        Bukkit.removeRecipe(recipeKey);
        recipeKey = NamespacedKey.fromString(getConfig().getString("item.id", "haohansmp:soul_anchor"));
        if (recipeKey == null) {
            recipeKey = new NamespacedKey(this, "soul_anchor");
        }
        if (!getConfig().getBoolean("recipe.enabled", true)) {
            return;
        }
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createAnchorItem(1));
        recipe.shape(" S ", "#o#", "DOD");
        recipe.setIngredient('S', Material.SOUL_LANTERN);
        recipe.setIngredient('#', Material.SOUL_SAND);
        recipe.setIngredient('o', Material.ENDER_PEARL);
        recipe.setIngredient('D', Material.DEEPSLATE);
        recipe.setIngredient('O', Material.OBSIDIAN);
        Bukkit.addRecipe(recipe);
    }

    private ItemStack createAnchorItem(int amount) {
        ItemStack item = new ItemStack(getAnchorMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(getConfig().getString("item.display-name", "&b&lSoul Anchor")));
        meta.setLore(getConfig().getStringList("item.lore").stream().map(this::color).toList());
        meta.setCustomModelData(getConfig().getInt("item.custom-model-data", 910001));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, getConfig().getString("item.id", "haohansmp:soul_anchor"));
        item.setItemMeta(meta);
        return item;
    }

    private boolean isSoulAnchorItem(ItemStack item) {
        if (item == null || item.getType() != getAnchorMaterial() || !item.hasItemMeta()) {
            return false;
        }
        return getConfig().getString("item.id", "haohansmp:soul_anchor")
                .equals(item.getItemMeta().getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING));
    }

    private ItemStack namedItem(Material material, String name) {
        return namedItem(material, name, List.of());
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(this::color).toList());
        }
        item.setItemMeta(meta);
        return item;
    }

    private void writeAnchorId(ItemStack item, UUID id) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(anchorIdKey, PersistentDataType.STRING, id.toString());
        item.setItemMeta(meta);
    }

    private Optional<UUID> readAnchorId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String raw = pdc.get(anchorIdKey, PersistentDataType.STRING);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private void loadAnchors() {
        anchorsById.clear();
        anchorIdsByLocation.clear();
        ConfigurationSection section = anchorsConfig.getConfigurationSection("anchors");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection node = section.getConfigurationSection(key);
            if (node == null) {
                continue;
            }
            World world = Bukkit.getWorld(UUID.fromString(node.getString("world-uuid")));
            if (world == null) {
                world = Bukkit.getWorld(node.getString("world-name", ""));
            }
            if (world == null) {
                getLogger().warning("Skipping Soul Anchor " + key + " because its world is not loaded.");
                continue;
            }
            Location location = new Location(world, node.getInt("x"), node.getInt("y"), node.getInt("z"));
            Anchor anchor = new Anchor(
                    UUID.fromString(key),
                    UUID.fromString(node.getString("owner")),
                    node.getString("name", "Soul Anchor"),
                    location,
                    (float) node.getDouble("yaw", 0D),
                    (float) node.getDouble("pitch", 0D),
                    node.getLong("created-at", System.currentTimeMillis()),
                    readUuid(node.getString("visual-uuid")),
                    readUuid(node.getString("interaction-uuid"))
            );
            if (location.getBlock().getType() == Material.AIR || location.getBlock().getType() == Material.GRINDSTONE) {
                location.getBlock().setType(getAnchorBlockMaterial(), false);
            }
            anchor = spawnVisuals(anchor);
            anchorsById.put(anchor.id(), anchor);
            anchorIdsByLocation.put(locationKey(location), anchor.id());
        }
    }

    private void saveAnchors() {
        anchorsConfig.set("anchors", null);
        for (Anchor anchor : anchorsById.values()) {
            String path = "anchors." + anchor.id();
            Location loc = anchor.location();
            anchorsConfig.set(path + ".owner", anchor.ownerId().toString());
            anchorsConfig.set(path + ".name", anchor.name());
            anchorsConfig.set(path + ".world-uuid", loc.getWorld().getUID().toString());
            anchorsConfig.set(path + ".world-name", loc.getWorld().getName());
            anchorsConfig.set(path + ".x", loc.getBlockX());
            anchorsConfig.set(path + ".y", loc.getBlockY());
            anchorsConfig.set(path + ".z", loc.getBlockZ());
            anchorsConfig.set(path + ".yaw", anchor.yaw());
            anchorsConfig.set(path + ".pitch", anchor.pitch());
            anchorsConfig.set(path + ".created-at", anchor.createdAt());
            anchorsConfig.set(path + ".visual-uuid", anchor.visualId() == null ? null : anchor.visualId().toString());
            anchorsConfig.set(path + ".interaction-uuid", anchor.interactionId() == null ? null : anchor.interactionId().toString());
        }
        try {
            anchorsConfig.save(anchorsFile);
        } catch (IOException exception) {
            getLogger().severe("Could not save anchors.yml: " + exception.getMessage());
        }
    }

    private Optional<Anchor> anchorAt(Block block) {
        UUID id = anchorIdsByLocation.get(locationKey(block.getLocation()));
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(anchorsById.get(id));
    }

    private boolean isAnchorStillPlaced(Anchor anchor) {
        return anchor != null
                && anchorsById.containsKey(anchor.id())
                && anchor.location().getBlock().getType() == getAnchorBlockMaterial()
                && anchorIdsByLocation.containsKey(locationKey(anchor.location()));
    }

    private void removeAnchor(UUID id) {
        Anchor anchor = anchorsById.remove(id);
        if (anchor != null) {
            anchorIdsByLocation.remove(locationKey(anchor.location()));
            removeEntity(anchor.visualId());
            removeEntity(anchor.interactionId());
            saveAnchors();
        }
    }

    private void cancelWarmupsTouching(UUID anchorId) {
        for (Warmup warmup : new ArrayList<>(warmups.values())) {
            if (warmup.sourceId.equals(anchorId) || warmup.targetId.equals(anchorId)) {
                warmup.cancel(true);
            }
        }
    }

    private List<Anchor> ownedAnchors(UUID ownerId) {
        return anchorsById.values().stream()
                .filter(anchor -> anchor.ownerId().equals(ownerId))
                .sorted(Comparator.comparingLong(Anchor::createdAt))
                .collect(Collectors.toList());
    }

    private Optional<Anchor> findOwnedAnchor(UUID ownerId, String nameOrId) {
        String query = nameOrId.toLowerCase(Locale.ROOT);
        return ownedAnchors(ownerId).stream()
                .filter(anchor -> anchor.id().toString().equalsIgnoreCase(nameOrId) || anchor.name().toLowerCase(Locale.ROOT).equals(query))
                .findFirst();
    }

    private String nextDefaultName(UUID ownerId) {
        Set<String> existing = ownedAnchors(ownerId).stream().map(Anchor::name).collect(Collectors.toCollection(HashSet::new));
        for (int i = 1; i <= 99; i++) {
            String name = "Soul Anchor #" + i;
            if (!existing.contains(name)) {
                return name;
            }
        }
        return "Soul Anchor";
    }

    private int getAnchorLimit(Player player) {
        if (player.hasPermission("soulanchor.limit.unlimited")) {
            return -1;
        }
        int limit = getConfig().getInt("limits.default", 3);
        if (getConfig().getBoolean("limits.permission-based", true)) {
            for (String permission : player.getEffectivePermissions().stream().map(info -> info.getPermission()).toList()) {
                if (permission.startsWith("soulanchor.limit.")) {
                    String raw = permission.substring("soulanchor.limit.".length());
                    if (!raw.equals("unlimited")) {
                        limit = Math.max(limit, parseInt(raw, limit));
                    }
                }
            }
        }
        return limit;
    }

    private Cost calculateCost(Location source, Location target) {
        if (!sameWorld(source, target)) {
            return new Cost(
                    getConfig().getInt("cross-dimension.level-cost", 30),
                    getConfig().getInt("cross-dimension.echo-shard-cost", 1)
            );
        }
        double dx = target.getX() - source.getX();
        double dz = target.getZ() - source.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        int blocksPerTier = Math.max(1, getConfig().getInt("distance.blocks-per-tier", 1000));
        int levelsPerTier = Math.max(1, getConfig().getInt("distance.levels-per-tier", 10));
        int minCost = Math.max(0, getConfig().getInt("distance.minimum-level-cost", 10));
        int levels = Math.max(minCost, (int) Math.ceil(distance / blocksPerTier) * levelsPerTier);
        return new Cost(levels, getConfig().getInt("teleport.echo-shard-cost", 1));
    }

    private String formatDistance(Location source, Location target) {
        if (!sameWorld(source, target)) {
            return "cross-dimension";
        }
        double dx = target.getX() - source.getX();
        double dz = target.getZ() - source.getZ();
        return String.valueOf((int) Math.round(Math.sqrt(dx * dx + dz * dz)));
    }

    private int countEchoShards(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.ECHO_SHARD) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeEchoShards(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != Material.ECHO_SHARD) {
                continue;
            }
            int take = Math.min(remaining, item.getAmount());
            item.setAmount(item.getAmount() - take);
            remaining -= take;
            if (item.getAmount() <= 0) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private Location findSafeLocation(Location anchorLocation) {
        int horizontal = getConfig().getInt("safe-location.horizontal-radius", 3);
        int vertical = getConfig().getInt("safe-location.vertical-radius", 4);
        World world = anchorLocation.getWorld();
        int baseX = anchorLocation.getBlockX();
        int baseY = anchorLocation.getBlockY();
        int baseZ = anchorLocation.getBlockZ();

        for (int dy = 0; dy <= vertical; dy++) {
            for (int radius = 0; radius <= horizontal; radius++) {
                for (int x = baseX - radius; x <= baseX + radius; x++) {
                    for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                        for (int sign : new int[]{1, -1}) {
                            int y = baseY + dy * sign + 1;
                            Location candidate = new Location(world, x + 0.5D, y, z + 0.5D, anchorLocation.getYaw(), anchorLocation.getPitch());
                            if (isSafe(candidate)) {
                                return candidate;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafe(Location location) {
        World world = location.getWorld();
        if (world == null || location.getBlockY() <= world.getMinHeight() || location.getBlockY() >= world.getMaxHeight() - 2) {
            return false;
        }
        WorldBorder border = world.getWorldBorder();
        if (!border.isInside(location)) {
            return false;
        }
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block below = feet.getRelative(0, -1, 0);
        return feet.isPassable()
                && head.isPassable()
                && below.getType().isSolid()
                && !isHazard(feet.getType())
                && !isHazard(head.getType())
                && !isHazard(below.getType());
    }

    private boolean isHazard(Material material) {
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.CACTUS
                || material == Material.POWDER_SNOW
                || material == Material.MAGMA_BLOCK
                || material == Material.CAMPFIRE
                || material == Material.SOUL_CAMPFIRE
                || material == Material.END_PORTAL
                || material == Material.NETHER_PORTAL;
    }

    private boolean isWorldBlocked(World world) {
        String mode = getConfig().getString("worlds.mode", "blacklist").toLowerCase(Locale.ROOT);
        List<String> worlds = getConfig().getStringList("worlds.list");
        boolean listed = worlds.stream().anyMatch(name -> name.equalsIgnoreCase(world.getName()));
        return mode.equals("blacklist") ? listed : !listed;
    }

    private Material getAnchorMaterial() {
        String configured = getConfig().getString("item.material", "GRINDSTONE");
        Material material = Material.matchMaterial(configured);
        return material == null ? Material.GRINDSTONE : material;
    }

    private Material getAnchorBlockMaterial() {
        String configured = getConfig().getString("item.placed-block", "BARRIER");
        Material material = Material.matchMaterial(configured);
        return material == null ? Material.BARRIER : material;
    }

    private Anchor spawnVisuals(Anchor anchor) {
        Entity existingDisplay = anchor.visualId() == null ? null : Bukkit.getEntity(anchor.visualId());
        Entity existingInteraction = anchor.interactionId() == null ? null : Bukkit.getEntity(anchor.interactionId());
        UUID visualId = existingDisplay instanceof ItemDisplay ? existingDisplay.getUniqueId() : null;
        UUID interactionId = existingInteraction instanceof Interaction ? existingInteraction.getUniqueId() : null;

        if (visualId == null) {
            Location displayLocation = anchor.location().clone().add(0.5D, 0.0D, 0.5D);
            ItemDisplay display = anchor.location().getWorld().spawn(displayLocation, ItemDisplay.class, entity -> {
                entity.setItemStack(createAnchorItem(1));
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                entity.setGravity(false);
                entity.setPersistent(true);
                entity.setSilent(true);
                entity.getPersistentDataContainer().set(anchorIdKey, PersistentDataType.STRING, anchor.id().toString());
            });
            visualId = display.getUniqueId();
        }

        if (interactionId == null) {
            Location interactionLocation = anchor.location().clone().add(0.5D, 0.2D, 0.5D);
            Interaction interaction = anchor.location().getWorld().spawn(interactionLocation, Interaction.class, entity -> {
                entity.setInteractionWidth(1.2F);
                entity.setInteractionHeight(1.4F);
                entity.setGravity(false);
                entity.setPersistent(true);
                entity.setSilent(true);
                entity.getPersistentDataContainer().set(anchorIdKey, PersistentDataType.STRING, anchor.id().toString());
            });
            interactionId = interaction.getUniqueId();
        }

        return new Anchor(anchor.id(), anchor.ownerId(), anchor.name(), anchor.location(), anchor.yaw(), anchor.pitch(), anchor.createdAt(), visualId, interactionId);
    }

    private Optional<Anchor> anchorFromEntity(Entity entity) {
        String raw = entity.getPersistentDataContainer().get(anchorIdKey, PersistentDataType.STRING);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(anchorsById.get(UUID.fromString(raw)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private void removeEntity(UUID id) {
        if (id == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(id);
        if (entity != null) {
            entity.remove();
        }
    }

    private UUID readUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void consumePlacedItem(Player player, EquipmentSlot hand) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack stack = hand == EquipmentSlot.OFF_HAND ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
        if (stack.getAmount() <= 1) {
            if (hand == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            return;
        }
        stack.setAmount(stack.getAmount() - 1);
    }

    private Player damagerPlayer(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean sameWorld(Location left, Location right) {
        return left.getWorld() != null && right.getWorld() != null && left.getWorld().getUID().equals(right.getWorld().getUID());
    }

    private String locationKey(Location location) {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String sanitizeName(String input) {
        String stripped = ChatColor.stripColor(color(input)).replaceAll("[\\p{Cntrl}<>]", "").trim();
        if (stripped.isEmpty()) {
            return "Soul Anchor";
        }
        return stripped.length() > 24 ? stripped.substring(0, 24) : stripped;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    private void send(CommandSender sender, String key, String... replacements) {
        String text = messages.getString(key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(color(messages.getString("prefix", "") + text));
    }

    private void startIdleParticles() {
        if (!getConfig().getBoolean("visuals.idle-particles", true)) {
            return;
        }
        long interval = Math.max(5L, getConfig().getLong("visuals.idle-particle-interval-ticks", 20L));
        idleParticlesTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Anchor anchor : anchorsById.values()) {
                    if (!isAnchorStillPlaced(anchor)) {
                        continue;
                    }
                    Location loc = anchor.location().clone().add(0.5D, 0.9D, 0.5D);
                    loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 2, 0.15D, 0.25D, 0.15D, 0.005D);
                }
            }
        }.runTaskTimer(this, interval, interval);
    }

    private record Anchor(UUID id, UUID ownerId, String name, Location location, float yaw, float pitch, long createdAt, UUID visualId, UUID interactionId) {
        Anchor withName(String newName) {
            return new Anchor(id, ownerId, newName, location, yaw, pitch, createdAt, visualId, interactionId);
        }
    }

    private record Cost(int levels, int shards) {
    }

    private record Validation(boolean ok, String messageKey, Location safeDestination, String[] replacements) {
        static Validation ok(Location safeDestination) {
            return new Validation(true, "", safeDestination, new String[0]);
        }

        static Validation fail(String messageKey, String... replacements) {
            return new Validation(false, messageKey, null, replacements);
        }
    }

    private static final class AnchorMenuHolder implements InventoryHolder {
        private final UUID sourceAnchorId;

        private AnchorMenuHolder(UUID sourceAnchorId) {
            this.sourceAnchorId = sourceAnchorId;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        private UUID sourceAnchorId() {
            return sourceAnchorId;
        }
    }

    private final class Warmup {
        private final Player player;
        private final UUID sourceId;
        private final UUID targetId;
        private final Location startLocation;
        private BukkitTask task;

        private Warmup(Player player, UUID sourceId, UUID targetId, Location startLocation, int seconds) {
            this.player = player;
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.startLocation = startLocation;
        }

        private void cancel(boolean notify) {
            warmups.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
            if (notify && player.isOnline()) {
                send(player, "warmup-cancelled");
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.6F, 1F);
            }
        }
    }
}
