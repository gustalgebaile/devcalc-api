# Escolhe uma imagem base com Java para rodar a aplicação
FROM eclipse-temurin:17-alpine

# Define diretório de trabalho no container
WORKDIR /app

# Copia o arquivo .jar compilado para dentro do container
COPY target/devcalc-api-1.0-SNAPSHOT.jar devcalc.jar

# Expõe a porta que sua aplicação usará (exemplo 7000)
EXPOSE 7000

# Comando para iniciar sua aplicação quando o container iniciar
CMD ["java", "-jar", "devcalc.jar"]