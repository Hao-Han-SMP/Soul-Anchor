# Soul Anchor

Soul Anchor la plugin teleport ca nhan cho HaoHan SMP, lay cam hung tu Waystones nhung duoc can bang cho survival: moi nguoi choi co gioi han anchor, moi lan dich chuyen ton Echo Shard va level theo khoang cach.

![Soul Anchor preview](asset/image/anchor.png)

## Tong Quan

Soul Anchor cho phep nguoi choi dat cac diem dich chuyen rieng cua minh trong the gioi. Khi tuong tac voi anchor, plugin mo GUI mang Soul Anchor de chon diem den. He thong mac dinh chi hien anchor cua chinh nguoi choi, giup tranh lo toa do can cu va tranh bien plugin thanh mang teleport cong cong qua manh.

Plugin duoc viet cho Paper/Purpur `1.21.11`, Java `21`, va di kem resource pack de hien thi model Soul Anchor rieng ma khong thay texture grindstone vanilla.

## Tinh Nang

- Soul Anchor item dung `GRINDSTONE` lam item nen, nhan dien bang PersistentDataContainer.
- Model custom hien thi bang resource pack va `item_model` rieng `haohansmp:soul_anchor`.
- Dat anchor trong the gioi, gioi han mac dinh `3` anchor moi nguoi choi.
- GUI 27 slot, 3 anchor duoc can giua o hang giua.
- Dich chuyen giua anchor ca nhan, co warmup va cooldown.
- Chi phi mac dinh: `1 Echo Shard` va `10 level / 1000 block`.
- Khac dimension: `30 level + 1 Echo Shard`.
- Diem den uu tien vi tri dung canh anchor, khong dung de len tren anchor.
- Luu anchor vao `plugins/SoulAnchor/anchors.yml`.
- Bao ve anchor khoi piston, explosion, fluid va nguoi khong phai chu so huu.

## Yeu Cau

- Paper hoac Purpur `1.21.11`.
- Java `21`.
- Maven neu muon build tu source.
- Resource pack Soul Anchor da duoc cai cho client/server.

## Cai Dat

1. Build plugin hoac lay file jar tu `target/SoulAnchor-1.0.1.jar`.
2. Copy jar vao thu muc `plugins/` cua server.
3. Cai resource pack Soul Anchor cho client hoac khai bao trong `server.properties`.
4. Restart server.
5. Vao game, lay item bang recipe hoac lenh admin `/soulanchor give <player> [amount]`.

Neu cap nhat tu ban cu co visual bi loi, hay restart server de plugin refresh lai `ItemDisplay` cua cac anchor da ton tai.

## Resource Pack

Resource pack la bat buoc neu muon thay model Soul Anchor thay vi item/block mac dinh. Ban local build ra file:

```text
target/anchor_spawn_point_fixed.zip
```

Pack su dung:

```text
assets/haohansmp/items/soul_anchor.json
assets/haohansmp/models/item/soul_anchor.json
```

Client can reload pack bang `F3 + T` hoac restart game sau khi thay zip moi.

## Cong Thuc Che Tao

![Soul Anchor recipe](asset/image/recipe.png)

Recipe shaped 3x3:

```text
 S 
#o#
DOD
```

Trong do:

| Ky tu | Item |
| --- | --- |
| `S` | Soul Lantern |
| `#` | Soul Sand |
| `o` | Ender Pearl |
| `D` | Deepslate |
| `O` | Obsidian |

Ket qua la `1x Soul Anchor`.

## Cach Su Dung

1. Cam Soul Anchor va dat xuong.
2. Right click vao Soul Anchor de mo GUI.
3. Chon anchor dich den trong GUI.
4. Doi warmup ket thuc.
5. Plugin tru Echo Shard va level ngay truoc khi teleport.
6. Nguoi choi duoc dich chuyen den vi tri an toan canh anchor dich.

Teleport se bi huy neu nguoi choi di chuyen qua muc cho phep, nhan damage, gay damage, chet hoac logout trong thoi gian warmup.

## Lenh

| Lenh | Mo ta |
| --- | --- |
| `/soulanchor` | Hien danh sach anchor cua ban. |
| `/soulanchor list` | Hien danh sach anchor cua ban. |
| `/soulanchor list <player>` | Admin xem anchor cua nguoi choi khac. |
| `/soulanchor give <player> [amount]` | Dua Soul Anchor cho nguoi choi. |
| `/soulanchor rename <anchor> <new-name>` | Doi ten anchor cua ban. |
| `/soulanchor remove <anchor>` | Xoa anchor cua ban khoi mang. |
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

Plugin cung ho tro permission gioi han dang:

```text
soulanchor.limit.5
soulanchor.limit.10
```

Gia tri cao nhat se duoc dung neu `limits.permission-based` bat.

## Cau Hinh Chinh

File config nam tai:

```text
plugins/SoulAnchor/config.yml
```

Cac gia tri quan trong:

| Key | Mac dinh | Mo ta |
| --- | --- | --- |
| `limits.default` | `3` | So anchor mac dinh moi nguoi choi. |
| `item.id` | `haohansmp:soul_anchor` | ID noi bo va item model. |
| `item.material` | `GRINDSTONE` | Item nen de craft/give. |
| `item.placed-block` | `BARRIER` | Block placeholder khi dat anchor. |
| `distance.blocks-per-tier` | `1000` | Moi tier khoang cach. |
| `distance.levels-per-tier` | `10` | So level moi tier. |
| `teleport.echo-shard-cost` | `1` | Echo Shard moi lan teleport. |
| `teleport.warmup-seconds` | `3` | Thoi gian warmup. |
| `teleport.cooldown-seconds` | `30` | Cooldown sau teleport. |
| `cross-dimension.level-cost` | `30` | Phi level khi khac dimension. |

## Build Tu Source

Chay tai thu muc goc repo:

```bash
mvn clean package
```

File jar nam trong:

```text
target/soul-anchor-1.0.1.jar
```

Trong workspace local hien tai, ban build san ra:

```text
target/SoulAnchor-1.0.1.jar
target/anchor_spawn_point_fixed.zip
```

## Cau Truc Du An

```text
src/main/java/dev/haohansmp/soulanchor/SoulAnchorPlugin.java
src/main/resources/config.yml
src/main/resources/messages.yml
src/main/resources/plugin.yml
asset/image/anchor.png
```

## Ghi Chu Van Hanh

- Luon thay ca plugin jar va resource pack khi cap nhat model.
- Neu thay texture tim-den, client dang khong doc duoc model/texture trong resource pack.
- Neu item cu van hien sai, lay item moi bang `/soulanchor give` sau khi server da chay jar moi.
- Thu muc `rsp/` la source resource pack local va dang duoc ignore de tranh push nham.
