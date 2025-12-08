# Etapa 1: Builder - compila e roda testes
FROM maven:3.9.1-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiando pom.xml e código fonte para cachear download das dependências
COPY pom.xml .
COPY src ./src

# Roda o build eos testes unitários
RUN mvn clean verify

# Etapa 2: Imagem final - pega o jar gerado e roda a aplicação
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia o jar do estágio builder
COPY --from=builder /app/target/devcalc-api-1.0-SNAPSHOT.jar devcalc.jar

EXPOSE 7000

CMD ["java", "-jar", "devcalc.jar"]
