# ----------------------------------------------------
# GIAI ĐOẠN 1: BUILDER (Dùng JDK để Build file JAR)
# ----------------------------------------------------
# Sử dụng Eclipse Temurin (Java 17 JDK) trên Ubuntu Jammy (22.04 LTS)
FROM eclipse-temurin:17-jdk-jammy AS builder

# Thiết lập thư mục làm việc trong container
WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Sao chép file cấu hình Maven (pom.xml)
COPY pom.xml .

# Lấy dependencies (sử dụng cache để build nhanh hơn ở các lần sau)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

# Sao chép toàn bộ mã nguồn
COPY src /app/src

# Build ứng dụng, tạo file JAR (bỏ qua tests)
RUN mvn clean install -DskipTests

# ----------------------------------------------------
# GIAI ĐOẠN 2: RUNTIME (Dùng JRE để Chạy file JAR)
# ----------------------------------------------------
# Sử dụng JRE (Java 17 JRE) trên Ubuntu Jammy (nhỏ gọn hơn và chỉ chạy)
FROM eclipse-temurin:17-jre-jammy

# Thiết lập múi giờ
ENV TZ Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Sao chép file JAR đã build từ giai đoạn builder
# Tên file JAR được giả định là tên project (vd: project_chat-0.0.1-SNAPSHOT.jar)
COPY --from=builder /app/target/*.jar app.jar

# Cổng TCP mà Spring Boot sử dụng
EXPOSE 8080

# Chỉ định lệnh chạy ứng dụng (ENTRYPOINT)
ENTRYPOINT ["java", "-jar", "/app.jar"]