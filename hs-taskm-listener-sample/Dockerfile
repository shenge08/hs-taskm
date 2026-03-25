# TaskM Listener Sample Dockerfile
#
# 使用方法：
# 1. 先在本地构建 JAR: mvn clean package -DskipTests
# 2. 然后构建 Docker 镜像: docker build -t taskm-listener-sample:1.0.0 .

# 运行时镜像
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制本地已构建好的 JAR 文件
COPY target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/api/health || exit 1

# 运行应用
# SERVER_PORT 可通过环境变量设置（默认：8080）
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]

# 生产环境的可选 JVM 参数
# ENTRYPOINT ["java", \
#     "-XX:+UseContainerSupport", \
#     "-XX:MaxRAMPercentage=75.0", \
#     "-Djava.security.egd=file:/dev/./urandom", \
#     "-jar", \
#     "app.jar"]
