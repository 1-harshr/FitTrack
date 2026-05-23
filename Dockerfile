FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/server/build/install/server .
EXPOSE 8080
CMD ["bin/server"]
