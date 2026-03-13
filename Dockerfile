# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.net.preferIPv4Stack=true","-jar","app.jar","--server.address=0.0.0.0","--server.port=8080"]
