# Projeto Docker - DevCalc API

## Visão Geral

Este projeto demonstra práticas de Infraestrutura como Código (IaC) usando Docker, Docker Compose, integração contínua com testes e orquestração de múltiplos serviços.

### Tecnologias Utilizadas
- Java 17 - Linguagem principal da aplicação
- Maven - Gerenciador de dependências e build
- Javalin - Framework web para a API
- PostgreSQL - Banco de dados relacional
- Redis - Banco de dados em memória
- Docker - Containerização
- Docker Compose - Orquestração de containers

---

## Objetivos do Projeto

### Integração Contínua com Testes
Criar um Dockerfile com multistage build que executa testes unitários automaticamente. Se algum teste falhar, o build é interrompido e a imagem não é criada. Isso garante que apenas versões com testes passando sejam produzidas.

### Infraestrutura as Code
Criar um arquivo docker-compose.yml que orquestra três serviços: PostgreSQL como banco relacional, Redis como banco não relacional e busybox como container de teste. Todos os containers devem estar na mesma rede Docker e compartilhar um volume.

### Validação e Monitoramento
Validar a conectividade entre containers dentro da mesma rede, verificar que o volume compartilhado é acessível por múltiplos serviços e confirmar que os health checks estão monitorando a saúde dos serviços.

---

## Estrutura de Arquivos

```
projeto/
├── Dockerfile                 Arquivo de build multistage com testes
├── docker-compose.yml         Arquivo de orquestração de serviços
├── pom.xml                    Configuração Maven do projeto
├── src/
│   ├── main/java/com/devcalc/
│   │   └── App.java           Classe principal da aplicação
│   └── test/java/com/devcalc/
│       └── AppTest.java       Testes unitários
└── README.md                  Este arquivo
```

---

## Build com Multistage e Testes

### Criar o Dockerfile

Crie um arquivo chamado `Dockerfile` na raiz do projeto:

```dockerfile
FROM maven:3.9.1-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean verify

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/devcalc-api-1.0-SNAPSHOT.jar devcalc.jar
EXPOSE 7000
CMD ["java", "-jar", "devcalc.jar"]
```

A primeira etapa (builder) compila e executa os testes. A segunda etapa cria a imagem final apenas com o jar gerado. Se os testes falharem na primeira etapa, o build é parado.

### Executar o Build

```powershell
docker build -t devcalc-api:test .
```

Este comando inicia o build do Dockerfile. Durante a execução, todos os testes unitários serão rodados. Se algum teste falhar, o comando exibirá a mensagem de erro e o build será interrompido sem criar a imagem final.

### Verificar Imagens Criadas

```powershell
docker images
```

Este comando lista todas as imagens Docker disponíveis. Se o build foi bem-sucedido, a imagem `devcalc-api:test` aparecerá na lista.

---

## Docker Compose com Múltiplos Serviços

### Criar o arquivo docker-compose.yml

Crie um arquivo chamado `docker-compose.yml` na raiz do projeto:

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:15
    container_name: pg_db
    environment:
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
      POSTGRES_DB: devcalc
    volumes:
      - shared-data:/var/lib/postgresql/data
    networks:
      - dev_network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7
    container_name: redis_db
    volumes:
      - shared-data:/data
    networks:
      - dev_network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  busybox:
    image: busybox:latest
    container_name: test_container
    networks:
      - dev_network
    tty: true
    stdin_open: true
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

volumes:
  shared-data:

networks:
  dev_network:
```

Este arquivo define três serviços. O PostgreSQL e Redis compartilham um volume chamado `shared-data`. O busybox é um container leve para testes. Todos estão conectados à rede `dev_network` e possuem health checks configurados.

### Iniciar os Containers

```powershell
docker-compose up -d
```

Este comando inicia todos os containers definidos no docker-compose.yml em background. O Docker criará a rede, os volumes e iniciará os serviços. A opção `-d` faz com que o comando retorne imediatamente sem bloquear o terminal.

### Verificar Containers em Execução

```powershell
docker ps
```

Este comando lista todos os containers em execução. Você verá três containers: pg_db, redis_db e test_container. A coluna STATUS mostrará se os health checks estão passando (healthy) ou falhando (unhealthy).

---

## Testes de Conectividade, Volume Compartilhado e Health Checks

### Testar Conectividade entre Containers

#### Acessar o Container de Teste

```powershell
docker exec -it test_container sh
```

Este comando entra no container de teste (busybox) em modo interativo. Você agora pode executar comandos dentro do container.

#### Testar Conexão com PostgreSQL

Dentro do container de teste, execute:

```sh
nc -zv pg_db 5432
```

Este comando verifica se a porta 5432 do PostgreSQL está acessível. O resultado esperado é uma mensagem informando que a porta está aberta (open). O `pg_db` é resolvido pelo Docker DNS interno da rede.

#### Testar Conexão com Redis

```sh
nc -zv redis_db 6379
```

Este comando verifica se a porta 6379 do Redis está acessível. O resultado esperado também é uma mensagem de porta aberta (open).

#### Sair do Container

```sh
exit
```

---

### Testar Volume Compartilhado

#### Criar Arquivo no PostgreSQL

Execute no PowerShell:

```powershell
docker exec -it pg_db sh
```

Agora dentro do container PostgreSQL, crie um arquivo:

```sh
echo "hello shared volume" > /var/lib/postgresql/data/test.txt
exit
```

Este arquivo foi criado em `/var/lib/postgresql/data/`, que está mapeado para o volume `shared-data`.

#### Verificar Arquivo no Redis

Execute no PowerShell:

```powershell
docker exec -it redis_db sh
```

Dentro do container Redis, tente ler o arquivo:

```sh
cat /data/test.txt
```

Se o arquivo for encontrado e exibir "hello shared volume", isso confirma que o volume é compartilhado entre PostgreSQL e Redis.

#### Sair do Container

```sh
exit
```

---

### Verificar Health Checks

#### Ver Status de Saúde dos Containers

```powershell
docker ps
```

Na coluna STATUS, você verá informações como `Up 5 minutes (healthy)`. Isso indica que os health checks estão passando.

#### Ver Detalhes do Health Check do PostgreSQL

```powershell
docker inspect pg_db | Select-String "Health" -A 10
```

Este comando exibe informações detalhadas sobre os testes de saúde do PostgreSQL, incluindo quantas vezes passou e falhou.

#### Ver Detalhes do Health Check do Redis

```powershell
docker inspect redis_db | Select-String "Health" -A 10
```

Este comando exibe informações detalhadas sobre os testes de saúde do Redis.

---

## Parar e Remover os Serviços

### Parar Containers Mantendo Dados

```powershell
docker-compose stop
```

Este comando para todos os containers sem removê-los.

### Remover Containers e Redes

```powershell
docker-compose down
```

Este comando remove os containers e a rede criada, mas mantém os volumes com dados.

### Remover Containers, Redes e Volumes

```powershell
docker-compose down -v
```

Este comando remove containers, redes e volumes. Use com cuidado pois os dados serão deletados.

---

## Resumo de Comandos

| Comando | Descrição |
|---------|-----------|
| `docker build -t devcalc-api:test .` | Constrói imagem, executa testes, falha se teste falhar |
| `docker images` | Lista imagens Docker |
| `docker-compose up -d` | Inicia PostgreSQL, Redis e busybox em background |
| `docker ps` | Lista containers em execução e status |
| `docker exec -it test_container sh` | Acessa container de teste |
| `nc -zv pg_db 5432` | Verifica conectividade com PostgreSQL |
| `nc -zv redis_db 6379` | Verifica conectividade com Redis |
| `docker-compose down` | Para todos os serviços |
| `docker-compose down -v` | Para serviços e remove volumes |

---

## Conceitos Aprendidos

O projeto aborda os seguintes conceitos:

1. Dockerfile multistage - Separar etapas de build e produção para gerar imagens menores e mais seguras.

2. Integração contínua - Automatizar testes durante o build, falhando se testes falharem.

3. Docker Compose - Definir e executar múltiplos containers com uma única configuração.

4. Networking Docker - Containers se comunicarem por nomes dentro da mesma rede.

5. Volumes Docker - Compartilhar dados entre containers de forma persistente.

6. Health Checks - Monitorar automaticamente a saúde dos serviços.

7. Infraestrutura como Código - Descrever infraestrutura em arquivos de configuração versionáveis.

---

## Notas Importantes

Os dados do PostgreSQL e Redis são persistidos no volume `shared-data`. Se o volume for removido, todos os dados serão perdidos.

Os health checks verificam a saúde dos serviços a cada 10 segundos. Alterações no docker-compose.yml permitem ajustar este intervalo.

Se o build falhar nos testes, nenhuma imagem será criada. O erro será exibido no terminal indicando qual teste falhou.

A rede `dev_network` é criada automaticamente pelo Docker Compose e permite que containers se comuniquem usando nomes de containers como hostnames.

As senhas e credenciais configuradas aqui são apenas para desenvolvimento local e nunca devem ser usadas em produção.
