# Soul Anchor

![Soul Anchor banner](asset/image/anchor.png)

**Ngon ngu:** Tieng Viet | [English](README.en.md)

Soul Anchor la plugin teleport ca nhan cho HaoHan SMP. Plugin cho phep nguoi choi dat cac Soul Anchor rieng, mo GUI de chon diem den, va dich chuyen voi chi phi ro rang thay vi tao mot lenh `/home` mien phi.

## Tong Quan

Soul Anchor duoc thiet ke cho server survival Paper/Purpur `1.21.11`. Moi anchor la mot diem dich chuyen vat ly trong the gioi. Nguoi choi phai tuong tac voi anchor de mo mang dich chuyen, sau do chon anchor dich den trong GUI.

Mac dinh, moi nguoi choi co toi da `3` Soul Anchor. Moi lan teleport ton `1 Echo Shard` va level theo khoang cach.

## Tinh Nang

- Gioi han mac dinh `3` Soul Anchor moi nguoi choi.
- GUI 27 slot, 3 anchor duoc can giua.
- Teleport co warmup, cooldown va kiem tra vi tri an toan.
- Diem den uu tien vi tri dung canh Soul Anchor.
- Chi phi mac dinh: `10 level / 1000 block` + `1 Echo Shard`.
- Khac dimension: `30 level` + `1 Echo Shard`.
- Luu du lieu anchor vao `plugins/SoulAnchor/anchors.yml`.
- Bao ve anchor khoi piston, explosion, fluid va nguoi khong phai chu so huu.
- Ho tro resource pack de hien thi model Soul Anchor rieng.

## Thanh Phan Du An

| Thanh phan | Mo ta |
| --- | --- |
| Plugin | Ma nguon Paper/Purpur plugin trong `src/`, xu ly item, anchor, GUI, teleport, command va config. |
| Resource pack (`rsp`) | Source resource pack dung de hien thi model va texture Soul Anchor custom. |

## Yeu Cau

- Paper hoac Purpur `1.21.11`.
- Java `21`.
- Maven neu build tu source.
- Resource pack Soul Anchor cho model custom.

## Cai Dat

1. Build plugin hoac lay file jar.
2. Copy jar vao thu muc `plugins/` cua server.
3. Cai resource pack Soul Anchor cho client hoac khai bao tren server.
4. Restart server.
5. Dung recipe hoac lenh `/soulanchor give <player> [amount]` de lay Soul Anchor.

## Resource Pack

Resource pack can thiet de Soul Anchor hien thi model rieng. Neu khong cai pack, item/block co the hien sai model hoac texture.

File pack local sau khi build:

```text
target/anchor_spawn_point_fixed.zip
```

Neu thay texture tim-den, hay thay dung zip moi va reload resource pack bang `F3 + T` hoac restart game.

## Recipe

![Soul Anchor recipe](asset/image/recipe.png)

Cong thuc tao `1x Soul Anchor` su dung Soul Lantern, Soul Sand, Ender Pearl, Deepslate va Obsidian. Recipe co the bat/tat trong `config.yml` bang `recipe.enabled`.

## Cach Su Dung

1. Dat Soul Anchor trong the gioi.
2. Right click vao Soul Anchor de mo GUI.
3. Chon anchor dich den.
4. Doi warmup hoan tat.
5. Plugin kiem tra lai anchor, vi tri an toan va chi phi.
6. Nguoi choi duoc teleport den vi tri dung canh anchor dich.

Teleport se bi huy neu nguoi choi di chuyen qua nguong cho phep, nhan damage, gay damage, chet hoac logout trong thoi gian warmup.

## Lenh

| Lenh | Mo ta |
| --- | --- |
| `/soulanchor` | Hien danh sach anchor cua ban. |
| `/soulanchor list` | Hien danh sach anchor cua ban. |
| `/soulanchor list <player>` | Admin xem anchor cua nguoi choi khac. |
| `/soulanchor give <player> [amount]` | Dua Soul Anchor cho nguoi choi. |
| `/soulanchor rename <anchor> <new-name>` | Doi ten anchor cua ban. |
| `/soulanchor remove <anchor>` | Xoa anchor cua ban. |
| `/soulanchor reload` | Reload config va recipe. |

Alias:

```text
/sa
```

## Permission

| Permission | Mac dinh | Mo ta |
| --- | --- | --- |
| `soulanchor.use` | true | Cho phep dung GUI va teleport. |
| `soulanchor.place` | true | Cho phep dat Soul Anchor. |
| `soulanchor.break.own` | true | Cho phep pha anchor cua minh. |
| `soulanchor.rename` | true | Cho phep doi ten anchor. |
| `soulanchor.admin` | op | Quyen admin tong quat. |
| `soulanchor.admin.give` | op | Cho phep dung lenh give. |
| `soulanchor.admin.remove` | op | Cho phep xoa/pha anchor cua nguoi khac. |
| `soulanchor.admin.reload` | op | Cho phep reload plugin config. |
| `soulanchor.bypass.cost` | op | Bo qua chi phi teleport. |
| `soulanchor.bypass.cooldown` | op | Bo qua cooldown. |
| `soulanchor.bypass.warmup` | op | Bo qua warmup. |
| `soulanchor.limit.unlimited` | false | Khong gioi han so anchor. |

Plugin cung ho tro permission gioi han nhu `soulanchor.limit.5` hoac `soulanchor.limit.10`.

## Cau Hinh

File config duoc tao tai:

```text
plugins/SoulAnchor/config.yml
```

Mot so key quan trong:

| Key | Mac dinh | Mo ta |
| --- | --- | --- |
| `limits.default` | `3` | So anchor mac dinh moi nguoi choi. |
| `item.id` | `haohansmp:soul_anchor` | ID noi bo va item model. |
| `item.material` | `GRINDSTONE` | Item nen de craft/give. |
| `item.placed-block` | `BARRIER` | Block placeholder khi dat anchor. |
| `distance.blocks-per-tier` | `1000` | Moi tier khoang cach. |
| `distance.levels-per-tier` | `10` | Level moi tier. |
| `teleport.echo-shard-cost` | `1` | Echo Shard moi lan teleport. |
| `teleport.warmup-seconds` | `3` | Thoi gian warmup. |
| `teleport.cooldown-seconds` | `30` | Cooldown sau teleport. |
| `cross-dimension.level-cost` | `30` | Phi level khi khac dimension. |

## Build Tu Source

```bash
mvn clean package
```

Jar Maven nam trong:

```text
target/soul-anchor-1.0.1.jar
```

Trong workspace local co the co them file build san:

```text
target/SoulAnchor-1.0.1.jar
target/anchor_spawn_point_fixed.zip
```

## Ghi Chu

- Khi cap nhat model, hay thay ca plugin jar va resource pack.
- Neu item cu van hien sai, lay item moi bang `/soulanchor give` sau khi server da chay jar moi.
- Thu muc `rsp/` la source resource pack local va khong nen push neu khong can.
