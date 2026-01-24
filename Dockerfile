# Minimaler und schneller Java 21 Container
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Maven Wrapper + pom.xml kopieren
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .
COPY app/pom.xml app/pom.xml
COPY web/pom.xml web/pom.xml
COPY profile/pom.xml profile/pom.xml
COPY staticdata/pom.xml staticdata/pom.xml
COPY auth/pom.xml auth/pom.xml
COPY search/pom.xml search/pom.xml
COPY media/pom.xml media/pom.xml
COPY notification/pom.xml notification/pom.xml
COPY shared/pom.xml shared/pom.xml

# Abhängigkeiten vorbereiten
RUN ./mvnw -pl app -am dependency:go-offline -B

# Quellcode kopieren
COPY app/ app/
COPY web/ web/
COPY profile/ profile/
COPY staticdata/ staticdata/
COPY auth/ auth/
COPY search/ search/
COPY media/ media/
COPY notification/ notification/
COPY shared/ shared/

# Projekt bauen
RUN ./mvnw -pl app -am clean package -Dskiptests -B

# Starte die JAR-Datei
CMD [ "java",  "-jar", "app/target/resume-0.0.1-SNAPSHOT.jar"]

# Temporärer Ordner für Spring Boot
VOLUME /tmp

