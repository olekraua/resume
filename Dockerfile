# Minimaler und schneller Java 21 Container
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Maven Wrapper + pom.xml kopieren
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Abhängigkeiten vorbereiten
RUN ./mvnw dependency:go-offline -B

# Quellcode kopieren
COPY src/ src/

# Projekt bauen
RUN ./mvnw clean package -Dskiptests -B

# Starte die JAR-Datei
CMD [ "java",  "-jar", "target/resume-0.0.1-SNAPSHOT.jar"]

# Temporärer Ordner für Spring Boot
VOLUME /tmp


