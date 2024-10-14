FROM eclipse-temurin:17
RUN apt-get update

EXPOSE 8080

WORKDIR /app
COPY target/kakao-auth-example-0.0.1-jar-with-dependencies.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar", "-port=8080"]