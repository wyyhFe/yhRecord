# lifeRecord 后端运行镜像
# 不在镜像里跑 Maven,使用本地已打好的 target/*.jar
# 构建: 在 record/ 目录下执行
#   docker build -t myproject-record-backend:latest .
# 运行(示例): 与 mysql / redis 同网络,挂载日志目录
#   docker run -d --name record-backend \
#     --network myproject_default \
#     -p 8080:8080 \
#     -e SPRING_PROFILES_ACTIVE=prod \
#     -e TZ=Asia/Shanghai \
#     -v /var/java/lifeRecord/logs:/app/logs \
#     myproject-record-backend:latest

FROM eclipse-temurin:21-jre

# 时区 + 字符集,日志和数据库时间一致
ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

WORKDIR /app

# JAR 路径可通过 --build-arg JAR_FILE=xxx 覆盖
# 远端构建上下文里 jar 放在 recordJar/ 下（与本地 mvn 的 target/ 区分）
ARG JAR_FILE=recordJar/record-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 日志目录（logback-spring.xml 的 ${LOG_PATH:-./logs} 从这里读）
# 宿主机挂载 -v /var/java/lifeRecord/logs:/app/logs 持久化
ENV LOG_PATH=/app/logs

# 运行时 JVM 参数,部署时可通过 -e JAVA_OPTS=... 覆盖
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# exec 形式,让 java 接管 PID 1,正确处理 SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
