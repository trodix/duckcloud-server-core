FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=core/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
