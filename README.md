# Hướng dẫn chạy ứng dụng trên Windows

## Chạy ứng dụng

java -jar target/gk-designpattern-1.0.0-SNAPSHOT.jar

## Nếu chạy ứng dụng không được thì làm theo các bước sau:

1. Biên dịch mã nguồn bằng lệnh:
   ./mvnw clean package -DskipTests # nếu dùng power shell
   hoặc
   mvnw.cmd clean package -DskipTests # nếu dùng cmd
2. Chạy ứng dụng bằng lệnh:
   java -jar target/gk-designpattern-1.0.0-SNAPSHOT.jar

## Lỗi thường gặp và cách fix

1. Lỗi: `java: command not found` hoặc `'java' is not recognized`
   Cách fix:
   - Cài JDK (khuyến nghị JDK 17).
   - Kiểm tra bằng lệnh `java -version`.
   - Trên Windows, nếu chưa nhận Java, thêm `JAVA_HOME` và cập nhật `PATH`.
2. Lỗi: `Unable to access jarfile target/gk-designpattern-1.0.0-SNAPSHOT.jar`
   Cách fix:
   - Chạy build trước: `./mvnw clean package -DskipTests` (PowerShell) hoặc `mvnw.cmd clean package -DskipTests` (cmd).
   - Đảm bảo đang đứng đúng thư mục dự án.
3. Lỗi: `UnsupportedClassVersionError`
   Cách fix:
   - Java đang chạy thấp hơn version được dùng để build.
   - Dùng JDK 17 hoặc phiên bản phù hợp rồi build lại.

# Hướng dẫn chạy ứng dụng trên macOS

## Chạy ứng dụng

java -jar target/gk-designpattern-1.0.0-SNAPSHOT.jar

## Nếu chạy ứng dụng không được thì làm theo các bước sau:

1. Biên dịch mã nguồn bằng lệnh:
   ./mvnw clean package -DskipTests
2. Chạy ứng dụng bằng lệnh:
   java -jar target/gk-designpattern-1.0.0-SNAPSHOT.jar

## Lỗi thường gặp và cách fix

1. Lỗi: `zsh: command not found: java`
   Cách fix:
   - Cài JDK (khuyến nghị JDK 17).
   - Kiểm tra bằng lệnh `java -version`.
2. Lỗi: `Permission denied` khi chạy `./mvnw`
   Cách fix:
   - Cấp quyền thực thi: `chmod +x mvnw`.
3. Lỗi: `Unable to access jarfile target/gk-designpattern-1.0.0-SNAPSHOT.jar`
   Cách fix:
   - Chạy build trước: `./mvnw clean package -DskipTests`.
   - Đảm bảo đang đứng đúng thư mục dự án.
4. Lỗi: `UnsupportedClassVersionError`
   Cách fix:
   - Java đang chạy thấp hơn version được dùng để build.
   - Dùng JDK 17 hoặc phiên bản phù hợp rồi build lại.
