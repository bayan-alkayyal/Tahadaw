# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# fontconfig + freetype let headless AWT rasterize TrueType fonts (the bundled
# Amiri Arabic fonts under src/main/resources/fonts ship inside the jar).
# Without these native libraries the JVM cannot render any font on a slim Linux image.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/Tahadaw-0.0.1-SNAPSHOT.jar app.jar

# Force headless mode so AWT never tries to reach an X server.
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
