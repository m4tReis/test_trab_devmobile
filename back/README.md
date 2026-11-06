# ComplexoSantosScheinBackend

Backend Java com Spring Boot para o aplicativo Android do Complexo Esportivo Santos e Schein.

## Stack inicial

- Java 21
- Spring Boot
- Gradle
- Spring Web
- Spring Security
- Spring Data JPA
- Oracle JDBC Driver
- Flyway via Docker
- Bean Validation
- JWT
- Lombok
- JUnit 5
- Mockito

## Estrutura principal

```text
src/main/java/br/com/santosschein/complexo
├── domain
├── application
├── infrastructure
├── interfaces
├── security
└── shared/exception
```

## Subir o Oracle local

Na raiz do projeto, suba o Oracle:

```powershell
docker compose up -d oracle
```

O banco fica disponível em:

```text
jdbc:oracle:thin:@//localhost:1521/FREEPDB1
user: complexo
password: complexo
```

As tabelas são criadas exclusivamente pelas migrations do Flyway em:

```text
src/main/resources/db/migration
```

O Flyway roda em um container dedicado, usando a imagem `flyway/flyway:9.22.3`, e monta esse diretório como `/flyway/sql`.
Execute as migrations antes de iniciar a aplicação:

```powershell
docker compose run --rm flyway migrate
```

A aplicação Spring Boot não executa migrations ao iniciar; ela apenas valida o schema com:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Para o container Flyway acessar Oracle, coloque o driver JDBC em:

```text
docker/flyway/drivers/ojdbc11.jar
```

O Compose monta somente esse arquivo em `/flyway/drivers/ojdbc11.jar`.
Nao monte a pasta inteira em `/flyway/drivers`, porque isso pode ocultar jars internos da imagem do Flyway.
Sem esse arquivo, o Flyway container não consegue abrir conexão JDBC com Oracle.

Novas alterações de banco devem ser adicionadas como novos arquivos `V__*.sql`.

Se o Flyway falhar com:

```text
No database found to handle jdbc:oracle:thin:@//oracle:1521/FREEPDB1
```

verifique se o `docker-compose.yml` está usando a imagem `flyway/flyway:9.22.3`.
Imagens Flyway 10+ modularizam o suporte a bancos e podem exigir módulos extras para Oracle.

Se a aplicação falhar com uma mensagem parecida com:

```text
Schema validation: missing table [reservation_slots]
```

isso significa que a aplicação conectou no Oracle, mas as migrations ainda não foram aplicadas nesse banco.
Execute o container do Flyway e tente iniciar a aplicação novamente:

```powershell
docker compose run --rm flyway migrate
.\gradlew.bat bootRun
```

## Executar a aplicação

Com o Oracle já iniciado:

```powershell
.\gradlew.bat bootRun
```

A API sobe em:

```text
http://localhost:8080/
```

Para o emulador Android, use:

```text
http://10.0.2.2:8080/
```

## Rodar os testes

```powershell
.\gradlew.bat test
```

Os testes são unitários e não dependem do Oracle.

## Testar no Postman ou Insomnia

Endpoint técnico (público):

```http
GET http://localhost:8080/api/v1/health
```

Endpoints principais:

```http
POST   /api/v1/auth/register
POST   /api/v1/auth/login                 (cabeçalho opcional X-Device-Name: "Android A")
POST   /api/v1/auth/logout                (encerra a sessão do token; 204, idempotente)
GET    /api/v1/users/me
GET    /api/v1/reservations/available-days?year=2026&month=7
GET    /api/v1/reservations/available-slots?date=2026-07-10
POST   /api/v1/reservations
GET    /api/v1/users/me/reservations/next
GET    /api/v1/users/me/reservations?status=upcoming
GET    /api/v1/users/me/reservations?status=past
DELETE /api/v1/users/me/reservations/{reservationId}
GET    /api/v1/admin/access-logs?from=2026-10-23&to=2026-10-24&userId=7&page=0&size=20   (somente ADMIN)
GET    /api/v1/admin/users                (somente ADMIN)
GET    /api/v1/health
```

Login, cadastro, logout e health são públicos. Os demais endpoints exigem o JWT no cabeçalho:

```http
Authorization: Bearer <token>
```

## Sessões e histórico de acessos (Parte 3)

- **Cada login cria uma sessão própria:** uma linha em `user_sessions` (o `acesso_usuario` do relatório). O token carrega o id dessa sessão e a mesma validade (`app.jwt.expiration-seconds`, padrão 1 h).
- **O token só vale com a sessão aberta:** toda chamada protegida confere no banco. Depois do `POST /api/v1/auth/logout`, aquele token passa a receber 401; as outras sessões da conta continuam valendo.
- **Expiração:** sessões vencidas são encerradas a cada minuto com motivo `EXPIRACAO`, e `logout_at` fica nulo (`app.session.cleanup-interval-ms`).
- **Fuso dos horários:** os instantes ficam em UTC no banco. A API do administrador também devolve data e hora no fuso `America/Sao_Paulo`.
- **Rotas `/api/v1/admin/**`:** sem login respondem 401; um cliente recebe 403 com "Acesso restrito ao administrador.".
- **Página do administrador:** é estática e fica em `src/main/resources/static/admin/index.html`, aberta por `http://localhost:8080/admin`.
- **Criar um administrador:** está no README da raiz do repositório.
