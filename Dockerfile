FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/ktor-sample /app/ktor-sample
EXPOSE 8080
ENTRYPOINT ["/app/ktor-sample/bin/ktor-sample"]
