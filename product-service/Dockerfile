# 1. Aşama: Derleme (Builder)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Bulunduğumuz klasördeki (örneğin auth-service içindeki) her şeyi kopyala
COPY . .

# Projeyi derle
RUN mvn clean package -DskipTests

# 2. Aşama: Çalıştırma (Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Derlenen jar dosyasını builder aşamasından alıp kopyala
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]