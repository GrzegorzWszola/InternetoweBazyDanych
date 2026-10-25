# --- ETAP 1: Budowanie aplikacji (Maven + JDK) ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Kopiujemy plik pom.xml, żeby Docker mógł pobrać i zapisać w cache zależności projektu
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kopiujemy kod źródłowy aplikacji
COPY src ./src

# Budujemy plik .jar (pomijając testy jednostkowe w trakcie budowy obrazu)
RUN mvn clean package -DskipTests

# --- ETAP 2: Uruchomienie aplikacji (Lekki obraz JRE) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Kopiujemy wygenerowany plik .jar z pierwszego etapu
COPY --from=build /app/target/*.jar app.jar

# Informujemy Dockera, na jakim porcie działa aplikacja
EXPOSE 8080

# Uruchomienie aplikacji Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]