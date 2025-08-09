# Usamos una imagen base con JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos los archivos necesarios para compilar
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Compilamos la aplicación (ignoramos tests para agilizar)
RUN ./mvnw package -DskipTests

# Comando que se ejecutará al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "target/central-service-0.0.1-SNAPSHOT.jar"]