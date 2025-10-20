# Giai đoạn 1: Build ứng dụng bằng Maven
# Sử dụng một image có sẵn Java 17 và Maven để biên dịch code
FROM maven:3.8.5-openjdk-17 AS build

# Đặt thư mục làm việc bên trong container là /app
WORKDIR /app

# Sao chép file pom.xml vào trước để tận dụng cache của Docker, giúp build nhanh hơn ở các lần sau
COPY pom.xml .

# Tải tất cả các thư viện cần thiết
RUN mvn dependency:go-offline

# Sao chép toàn bộ mã nguồn còn lại của bạn vào
COPY src ./src

# Build ứng dụng, tạo ra file .jar và bỏ qua các bài test
RUN mvn clean package -DskipTests

# Giai đoạn 2: Tạo image cuối cùng để chạy ứng dụng
# Sử dụng một image Java 17 nhẹ hơn (slim) để tiết kiệm dung lượng
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc
WORKDIR /app

# Sao chép file .jar đã được build ở giai đoạn trên vào image này
COPY --from=build /app/target/project_chat-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080 để bên ngoài có thể giao tiếp với ứng dụng của bạn
EXPOSE 8080

# Lệnh mặc định sẽ được chạy khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]