# Estágio 1: Builder (Compilação)
FROM eclipse-temurin:25.0.3_9-jdk-jammy as builder

WORKDIR /app

# Instala o Maven dentro deste estágio para podermos compilar
RUN apt-get update && apt-get install -y maven

# Copia as configurações do pom.xml e baixa as dependências (bom para cache)
COPY ./pom.xml /app/
RUN mvn dependency:go-offline

# Copia o código-fonte restante e gera o pacote .jar
COPY ./ /app/
RUN mvn package -DskipTests

# Estágio 2: Application (Execução leve)
FROM eclipse-temurin:25.0.3_9-jre-jammy as application

WORKDIR /app

# Em vez de rodar o Maven de novo, COPIAMOS apenas o .jar gerado no estágio "builder"
COPY --from=builder /app/target/websocket-0.0.1-SNAPSHOT.jar /app/app.jar

# Expõe a porta lógica do container
EXPOSE 8080

# Executa o jar que foi renomeado para facilitar o caminho
ENTRYPOINT ["java", "-jar", "app.jar"]
