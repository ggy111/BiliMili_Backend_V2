# 使用官方的OpenJDK镜像作为基础镜像
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /bilimili

# 将项目的jar包复制到容器中
COPY target/bilimili.jar /bilimili/bilimili.jar

# 暴露应用运行所需的端口
EXPOSE 7070

# 启动应用
CMD ["java", "-jar", "bilimili.jar"]
