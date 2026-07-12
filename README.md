<div align="center">

<img src="image.png" alt="Soul Anchor banner" width="100%">

# Soul Anchor

Plugin du lịch nhanh chóng dành cho HaoHan SMP, xây dựng quanh hệ thống teleport.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-API-222222?style=for-the-badge&logo=paper&logoColor=white)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-8A4FFF?style=for-the-badge)](https://purpurmc.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

Ngôn ngữ: Tiếng Việt | [English](README.en.md)

</div>

## Tổng quan

Soul Anchor là plugin Minecraft dành cho HaoHan SMP. Plugin cung cấp hệ thống...

## Công nghệ sử dụng

| Toolkit | Vai trò |
| --- | --- |
| Paper API | Nền tảng API chính để phát triển plugin server. |
| Purpur | Môi trường server khuyến nghị để triển khai. |
| Java 21 | Ngôn ngữ và runtime chính của plugin. |
| Maven | Quản lý dependency và build file `.jar`. |

## Thành phần dự án

| Thành phần | Mô tả |
| --- | --- |
| `Soul-Anchor` | Plugin server, xử lý logic teleport. |

## Yêu cầu

- Minecraft server chạy Paper hoặc Purpur.
- Java 21 trở lên.
- Maven 3.9 trở lên nếu cần build từ mã nguồn.

## Cài đặt

1. Build hoặc tải file `.jar` của plugin.
2. Copy file `.jar` vào thư mục `plugins/` của server.
3. Khởi động lại server.

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

```powershell
.\build_and_start.ps1
```

## Lệnh

Các lệnh quản trị dùng permission `haohansmp.soulanchor.admin`. Người chơi OP có permission này theo mặc định.

| Lệnh | Mô tả |
| --- | --- |
| `/soulanchor info` | Hiển thị thông tin plugin. |
| `/soulanchor reload` | Nạp lại cấu hình. |
| `/soulanchor debug` | Bật hoặc tắt chế độ debug. |

## Permission

| Permission | Mặc định | Mô tả |
| --- | --- | --- |
| `haohansmp.soulanchor.admin` | OP | Cho phép sử dụng các lệnh quản trị. |
| `haohansmp.soulanchor.use` | Tất cả người chơi | Cho phép sử dụng hệ thống teleport. |

## Ghi chú vận hành

- Luôn kiểm tra cấu hình trước khi cập nhật.
- Khi cập nhật plugin trong môi trường đang chạy, kiểm tra lại bằng `/reload` và `/soulanchor reload`.
