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
