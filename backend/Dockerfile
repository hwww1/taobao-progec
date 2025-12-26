# 多阶段构建 - 后端Dockerfile
# 阶段1: 构建阶段
# 使用阿里云镜像（如果Docker镜像加速器不可用，可以取消注释使用）
# FROM registry.cn-hangzhou.aliyuncs.com/acs/maven:3.9-eclipse-temurin-17 AS build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 配置Maven使用阿里云镜像（加速依赖下载）
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 阶段2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装wget用于健康检查
RUN apk add --no-cache wget

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

# 从构建阶段复制JAR文件
COPY --from=build /app/target/taobao-backend-1.0.0.jar app.jar

# 创建上传目录并设置权限
RUN mkdir -p /app/uploads/images/products && \
    chmod -R 777 /app/uploads

# 注意：由于volume映射，实际权限取决于宿主机目录权限
# 切换到非root用户（但volume映射的目录需要777权限）
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/product/list || exit 1

# 启动应用（添加JVM参数确保UTF-8编码）
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Shanghai", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

