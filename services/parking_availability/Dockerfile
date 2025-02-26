FROM amazoncorretto:21-alpine

WORKDIR /availability-service

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 9083

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]