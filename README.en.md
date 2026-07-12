# Soul Anchor

![Soul Anchor banner](asset/image/anchor.png)

**Language:** [Tieng Viet](README.md) | English

Soul Anchor is a personal teleport plugin for HaoHan SMP. It lets players place their own physical Soul Anchors, open a GUI to choose a destination, and teleport with clear survival-friendly costs instead of acting like a free `/home` command.

## Overview

Soul Anchor targets Paper/Purpur `1.21.11`. Each anchor is a physical teleport point in the world. Players interact with an anchor to open their private Soul Anchor network, then choose another owned anchor as the destination.

By default, each player can own up to `3` Soul Anchors. Every teleport costs `1 Echo Shard` plus levels based on distance.

## Features

- Default limit of `3` Soul Anchors per player.
- 27-slot GUI with the three anchor slots centered.
- Warmup, cooldown, and safe-location checks.
- Destination search prefers a safe block beside the target anchor.
- Default cost: `10 levels / 1000 blocks` + `1 Echo Shard`.
- Cross-dimension cost: `30 levels` + `1 Echo Shard`.
- Anchor data persists in `plugins/SoulAnchor/anchors.yml`.
- Protection against pistons, explosions, fluids, and non-owner breaking.
- Resource pack support for the custom Soul Anchor model.

## Project Components

| Component | Description |
| --- | --- |
| Plugin | Paper/Purpur plugin source in `src/`, handling items, anchors, GUI, teleporting, commands, and config. |
| Resource pack (`rsp`) | Resource pack source used to render the custom Soul Anchor model and textures. |

## Requirements

- Paper or Purpur `1.21.11`.
- Java `21`.
- Maven if building from source.
- Soul Anchor resource pack for the custom model.

## Installation

1. Build or download the plugin jar.
2. Copy the jar into the server `plugins/` folder.
3. Install the Soul Anchor resource pack for clients or configure it on the server.
4. Restart the server.
5. Craft a Soul Anchor or use `/soulanchor give <player> [amount]`.

## Resource Pack

The resource pack is required for the custom Soul Anchor model. Without it, the item or display model may render incorrectly.

Local pack build output:

```text
target/anchor_spawn_point_fixed.zip
```

If the model appears as a purple-black missing texture, replace the pack with the latest zip and reload resources with `F3 + T` or restart the game.

## Recipe

![Soul Anchor recipe](asset/image/recipe.png)

Crafts `1x Soul Anchor` using Soul Lantern, Soul Sand, Ender Pearl, Deepslate, and Obsidian. The recipe can be enabled or disabled in `config.yml` with `recipe.enabled`.

## Usage

1. Place a Soul Anchor in the world.
2. Right click the Soul Anchor to open the GUI.
3. Choose a destination anchor.
4. Wait for the warmup to finish.
5. The plugin rechecks anchor state, safe destination, and cost.
6. The player is teleported to a safe block beside the destination anchor.

Teleport is cancelled if the player moves too far, takes damage, deals damage, dies, or logs out during warmup.

## Commands

| Command | Description |
| --- | --- |
| `/soulanchor` | Shows your anchor list. |
| `/soulanchor list` | Shows your anchor list. |
| `/soulanchor list <player>` | Lets admins view another player's anchors. |
| `/soulanchor give <player> [amount]` | Gives Soul Anchors to a player. |
| `/soulanchor rename <anchor> <new-name>` | Renames one of your anchors. |
| `/soulanchor remove <anchor>` | Removes one of your anchors. |
| `/soulanchor reload` | Reloads config and recipe. |

Alias:

```text
/sa
```

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `soulanchor.use` | true | Allows GUI and teleport use. |
| `soulanchor.place` | true | Allows placing Soul Anchors. |
| `soulanchor.break.own` | true | Allows breaking owned anchors. |
| `soulanchor.rename` | true | Allows renaming anchors. |
| `soulanchor.admin` | op | General admin permission. |
| `soulanchor.admin.give` | op | Allows the give command. |
| `soulanchor.admin.remove` | op | Allows removing/breaking other players' anchors. |
| `soulanchor.admin.reload` | op | Allows config reloads. |
| `soulanchor.bypass.cost` | op | Bypasses teleport cost. |
| `soulanchor.bypass.cooldown` | op | Bypasses cooldown. |
| `soulanchor.bypass.warmup` | op | Bypasses warmup. |
| `soulanchor.limit.unlimited` | false | Removes the anchor limit. |

Permission-based limits such as `soulanchor.limit.5` or `soulanchor.limit.10` are also supported.

## Configuration

Config file:

```text
plugins/SoulAnchor/config.yml
```

Important keys:

| Key | Default | Description |
| --- | --- | --- |
| `limits.default` | `3` | Default anchors per player. |
| `item.id` | `haohansmp:soul_anchor` | Internal ID and item model. |
| `item.material` | `GRINDSTONE` | Base item used for crafting/giving. |
| `item.placed-block` | `BARRIER` | Placeholder block used after placement. |
| `distance.blocks-per-tier` | `1000` | Distance per cost tier. |
| `distance.levels-per-tier` | `10` | Levels per tier. |
| `teleport.echo-shard-cost` | `1` | Echo Shard cost per teleport. |
| `teleport.warmup-seconds` | `3` | Warmup duration. |
| `teleport.cooldown-seconds` | `30` | Cooldown after teleport. |
| `cross-dimension.level-cost` | `30` | Level cost for cross-dimension teleport. |

## Build From Source

```bash
mvn clean package
```

Maven jar output:

```text
target/soul-anchor-1.0.1.jar
```

The local workspace may also contain:

```text
target/SoulAnchor-1.0.1.jar
target/anchor_spawn_point_fixed.zip
```

## Notes

- When updating the model, replace both the plugin jar and resource pack.
- If old items still render incorrectly, get a fresh item with `/soulanchor give` after the new jar is running.
- `rsp/` is local resource-pack source and should not be pushed unless intentionally needed.
