<div align="center">

<img src="image.png" alt="Soul Anchor banner" width="100%">

# Soul Anchor

Plugin du lịch nhanh cho HaoHan SMP, cung cấp cách thức nhanh chóng để di chuyển trong thế giới.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-API-222222?style=for-the-badge&logo=paper&logoColor=white)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-8A4FFF?style=for-the-badge)](https://purpurmc.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Gson](https://img.shields.io/badge/Gson-JSON-2E7D32?style=for-the-badge&logo=google&logoColor=white)](https://github.com/google/gson)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Ngôn ngữ: Tiếng Việt | [English](README.en.md)

</div>

## Tổng quan

Soul Anchor là plugin Minecraft dành cho HaoHan SMP. Plugin cung cấp hệ thống teleportation tùy chỉnh, quản lý điểm dừng chân (waypoint), giao diện thân thiện, và hỗ trợ [...]

## Công nghệ sử dụng

| Toolkit | Vai trò |
| --- | --- |
| Paper API | Nền tảng API chính để phát triển plugin server. |
| Purpur | Môi trường server khuyến nghị để triển khai. |
| Java 21 | Ngôn ngữ và runtime chính của plugin. |
| Maven | Quản lý dependency và build file `.jar`. |
| Gson | Hỗ trợ xử lý dữ liệu JSON cho waypoint và cấu hình. |
| JUnit 5 | Viết và chạy unit test. |

## Thành phần dự án

| Thành phần | Mô tả |
| --- | --- |
| `Soul-Anchor` | Plugin server, xử lý logic teleportation, command, và quản lý waypoint. |
| `Soul-Anchor_Datapack` | Datapack chứa advancement, loot table và tag liên quan đến teleportation. |
| `Soul-Anchor_Resourcepack` | Resource pack chứa texture và model cần thiết cho vật phẩm hoặc khối tùy chỉnh. |

## Yêu cầu

- Minecraft server chạy Paper hoặc Purpur.
- Java 21 trở lên.
- Maven 3.9 trở lên nếu cần build từ mã nguồn.
- Datapack và resource pack đi kèm để hệ thống hoạt động đầy đủ.

## Cài đặt

1. Build hoặc tải file `.jar` của plugin.
2. Copy file `.jar` vào thư mục `plugins/` của server.
3. Copy thư mục hoặc file `.zip` của datapack vào `world/datapacks/`.
4. Cài resource pack cho client hoặc cấu hình server để người chơi tải resource pack khi tham gia.
5. Khởi động lại server.
6. Chạy `/reload` nếu cần nạp lại datapack trong quá trình phát triển.

Sau lần chạy đầu tiên, plugin sẽ tạo file cấu hình tại `plugins/Soul-Anchor/config.yml`.

## Build từ mã nguồn

Chạy lệnh sau tại thư mục gốc của dự án plugin:

```bash
mvn clean package
```

File `.jar` sau khi build nằm trong thư mục `target/`.

Nếu chỉ cần build nhanh mà không chạy test:

```bash
mvn clean package -DskipTests
```

## Script phát triển

Dự án có file `build_and_start.ps1` để hỗ trợ build và khởi động server trong môi trường phát triển cục bộ.

Trước khi dùng, kiểm tra và chỉnh lại các đường dẫn trong script cho phù hợp với máy của bạn, đặc biệt là đường dẫn dự án, thư mục `plugins/` và thư mục server.

```powershell
.\build_and_start.ps1
```

## Lệnh

Các lệnh quản trị dùng permission `haohansmp.soulanchor.admin`. Người chơi OP có permission này theo mặc định.

| Lệnh | Mô tả |
| --- | --- |
| `/soulanchor info` | Hiển thị thông tin plugin. |
| `/soulanchor reload` | Nạp lại cấu hình và waypoint. |
| `/soulanchor debug` | Bật hoặc tắt chế độ debug. |
| `/soulanchor list` | Hiển thị danh sách waypoint được quản lý. |
| `/soulanchor set <tên> [mô tả]` | Tạo waypoint mới tại vị trí hiện tại. |
| `/soulanchor remove <tên>` | Xóa waypoint hiện có. |
| `/soulanchor tp <tên>` | Teleport đến waypoint. |

Alias của lệnh chính: `/anchor`, `/tp`.

## Permission

| Permission | Mặc định | Mô tả |
| --- | --- | --- |
| `haohansmp.soulanchor.admin` | OP | Cho phép sử dụng các lệnh quản trị. |
| `haohansmp.soulanchor.use` | Tất cả người chơi | Cho phép tương tác với teleportation anchor. |
| `haohansmp.soulanchor.set` | Tất cả người chơi | Cho phép tạo waypoint mới. |

## Cấu trúc Waypoint

Các waypoint du lịch được lưu trong `src/main/resources/waypoints/`. Mỗi waypoint định nghĩa tọa độ đích, thế giới, và permission truy cập.

Ví dụ tham khảo cấu trúc waypoint:

```text
src/main/resources/waypoints/example_waypoint.json
```

Waypoint gồm các thông tin:

- `id`: Định danh duy nhất của waypoint.
- `name`: Tên hiển thị cho người chơi.
- `world`: Thế giới (tên folder world).
- `x`, `y`, `z`: Tọa độ đích.
- `yaw`, `pitch`: Hướng nhìn khi teleport.
- `description`: Mô tả waypoint.
- `public`: Cho phép tất cả người chơi sử dụng (true) hay riêng tư (false).
- `owner`: Chủ sở hữu waypoint.

Ví dụ JSON:

```json
{
  "id": "spawn_point",
  "name": "Main Spawn",
  "world": "world",
  "x": 100.5,
  "y": 64.0,
  "z": -200.5,
  "yaw": 0.0,
  "pitch": 0.0,
  "description": "Điểm spawn chính",
  "public": true,
  "owner": "admin"
}
```

Chạy `/soulanchor reload` để áp dụng thay đổi mà không cần restart server.

## Công thức Recipe

Soul Anchor cung cấp các công thức crafting để tạo ra các vật phẩm liên quan đến teleportation. Các recipe được định nghĩa trong datapack.

### Soul Anchor (Vật phẩm chính)

Công thức crafting Soul Anchor:

```
A A A
A B A
A A A
```

- `A`: Amethyst Shard
- `B`: Ender Pearl

**Kết quả**: 1x Soul Anchor

### Waypoint Crystal

Công thức crafting Waypoint Crystal (được sử dụng để tạo waypoint):

```
  A
A B A
  A
```

- `A`: Glowstone Dust
- `B`: Soul Anchor

**Kết quả**: 1x Waypoint Crystal

### Teleport Scroll

Công thức crafting Teleport Scroll (vật phẩm để teleport nhanh):

```
A B A
A C A
```

- `A`: Paper
- `B`: Waypoint Crystal
- `C`: Ink Sac

**Kết quả**: 3x Teleport Scroll

Các recipe có thể được tùy chỉnh bằng cách chỉnh sửa file recipe trong datapack nằm tại `Soul-Anchor_Datapack/data/soulanchor/recipes/`.

## Ghi chú vận hành

- Luôn cài plugin, datapack và resource pack cùng nhau để tránh thiếu waypoint, texture hoặc dữ liệu progression.
- Không nên chỉnh trực tiếp dữ liệu trong thư mục runtime của server nếu thay đổi có thể được quản lý từ mã nguồn.
- Khi cập nhật waypoint hoặc cấu hình trong môi trường đang chạy, kiểm tra lại bằng `/reload` và `/soulanchor reload`.
