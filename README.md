<p align="center"><img src="docs/logo.png" alt="Complexo Esportivo Santos e Schein" width="140"></p>

# Complexo Esportivo Santos e Schein

Projeto G2 de Desenvolvimento de Dispositivos Móveis: aplicativo Android para consultar horários e reservar os campos de futebol do Complexo Esportivo Santos e Schein (Canoas), com uma API própria e banco central.

Equipe: Davi Campanaro, Pedro Augusto, Vitor Schein e Mateus Reis.

## Estrutura do repositório

```text
ComplexoSantosSchein/
├── back/    API REST em Java 21 + Spring Boot 4, Oracle (Docker) + Flyway, JWT
├── app/     App Android em Java 21 (Clean Architecture, Retrofit, modos mock e prod)
└── docs/    Relatório do Projeto G2, tarefas pendentes e checklist de progresso
    ├── Relatorio_ProjetoG2_Santos_e_Schein_261023.docx   versão atual do relatório
    ├── CHECKLIST.md                                      o que está pronto e o que falta
    ├── AJUSTES_RELATORIO.md                              o que mudar no texto do relatório
    ├── TASKS_ProjetoG2_261023.md                         plano detalhado da Parte 3 (T01–T06)
    └── versoes-do-repo-app/                              cópias antigas que estavam dentro do app
```

`back/` e `app/` continuam sendo dois projetos Gradle independentes: cada um tem o seu próprio `gradlew`, e cada um abre na sua própria IDE.

| Pasta | Abrir com | Documentação |
|---|---|---|
| `back/` | IntelliJ IDEA (ou VS Code) | [back/README.md](back/README.md) |
| `app/` | Android Studio, abrindo a pasta `app/` (não a raiz) | [app/README.md](app/README.md) |

> No Android, a pasta `app/` é o projeto e `app/app/` é o módulo do aplicativo. Esse é o padrão do Android Studio.

## Como rodar tudo junto (app conectado ao back)

Pré-requisitos: JDK 21, Docker Desktop e Android Studio com um emulador.

1. **Banco de dados**, dentro de `back/`:
   - O Flyway precisa do driver `back/docker/flyway/drivers/ojdbc11.jar`. Ele já está nesta cópia; em um clone novo do repositório, copie o arquivo para lá (veja [o README dessa pasta](back/docker/flyway/drivers/README.md)). O `.jar` não vai para o Git.
   - Suba o Oracle e aplique as migrations. Se o seu banco já existia, o comando aplica só as novas (V6 a V8):

   ```powershell
   cd back
   docker compose up -d oracle
   docker compose run --rm flyway migrate
   ```

2. **API**, ainda em `back/`:

   ```powershell
   .\gradlew.bat bootRun
   ```

   A API sobe em `http://localhost:8080/`.

3. **App**: abra `app/` no Android Studio, selecione o Build Variant **`prodDebug`** e rode no emulador. O emulador acessa a API por `http://10.0.2.2:8080/`.

Para testar só o app, sem backend, use o Build Variant **`mockDebug`** (login `usuario@teste.com` / `123456`). No modo mock, uma conta criada no cadastro também consegue entrar.

### Usar celulares físicos (mesma conta em dois aparelhos)

O endereço `10.0.2.2` só funciona no emulador. Para celulares na mesma rede Wi-Fi do computador que roda o back:

1. Descubra o IP do computador (`ipconfig`, por exemplo `192.168.0.10`) e libere a porta 8080 no firewall.
2. Em `app/local.properties` (arquivo que não vai para o Git), adicione `api.base.url=http://192.168.0.10:8080/`.
3. Gere e instale o `prodDebug`. Os builds de debug aceitam HTTP na rede local; o de release não aceita.

## Área do administrador (Parte 3)

A página web do administrador é servida pelo próprio back: **http://localhost:8080/admin**. Ela mostra o histórico de acessos (login, logout e situação de cada sessão), com filtro por período e por usuário.

O cadastro público sempre cria clientes. Para criar um administrador:

1. Cadastre a conta normalmente pelo app (ou por `POST /api/v1/auth/register`).
2. Promova a conta no banco:

   ```powershell
   docker exec -it complexo-santos-schein-oracle sqlplus complexo/complexo@//localhost:1521/FREEPDB1
   ```

   No prompt `SQL>`, digite:

   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'email.do.admin@exemplo.com';
   COMMIT;
   EXIT
   ```

3. Entre na página `/admin` com esse e-mail e senha. Uma conta sem perfil ADMIN recebe "Acesso restrito ao administrador".

## Testes

```powershell
cd back; .\gradlew.bat test; cd ..
cd app;  .\gradlew.bat test; cd ..
```

## Andamento

O progresso em relação ao relatório (Partes 1, 2 e 3) está em [docs/CHECKLIST.md](docs/CHECKLIST.md).
