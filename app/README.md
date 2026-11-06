# Complexo Esportivo Santos e Schein

Aplicativo Android em Java para agendamento de horarios em campos de futebol do Complexo Esportivo Santos e Schein. O projeto esta preparado para rodar sem backend no modo mock/dev e para usar chamadas HTTP reais no modo remoto/prod.

## Tecnologias

- Java 21
- Gradle e Android Gradle Plugin
- AndroidX, Fragment, RecyclerView, ViewModel e LiveData
- Material Components
- Retrofit, OkHttp e Gson
- Lombok
- JUnit, Mockito e AndroidX Arch Core Testing

## Arquitetura

O app segue Clean Architecture com separacao entre apresentacao, dominio, dados e infraestrutura compartilhada.

- `core`: configuracao, rede, sessao, erros e utilitarios.
- `domain`: modelos, contratos de repositorio e casos de uso. Nao depende de Android, Retrofit, DTOs ou UI.
- `data`: APIs Retrofit, DTOs, mappers, repositorios remotos e repositorios mockados em memoria.
- `presentation`: Fragments, ViewModels, adapters, estado de tela e navegacao.

Os use cases dependem apenas das interfaces em `domain.repository`. Os ViewModels chamam use cases e expoem `LiveData<UiState<T>>`. Os repositorios remotos convertem DTOs para modelos de dominio e usam um mapeamento centralizado de erros amigaveis.

## Estrutura

```text
com.santosschein.app
|-- core
|   |-- config
|   |-- error
|   |-- network
|   |-- session
|   `-- util
|-- data
|   |-- mock
|   |-- remote
|   |   |-- api
|   |   |-- dto
|   |   `-- mapper
|   `-- repository
|-- domain
|   |-- model
|   |-- repository
|   `-- usecase
`-- presentation
    |-- auth
    |-- home
    |-- navigation
    |-- profile
    `-- reservation
```

## Como abrir no Android Studio

1. No Android Studio, abra a pasta `app/` deste repositorio (a que contem `settings.gradle`), e nao a raiz `ComplexoSantosSchein/`.
2. Aguarde a sincronizacao do Gradle.
3. Abra `Build Variants`.
4. Selecione `mockDebug` para rodar sem backend ou `prodDebug` para chamadas HTTP locais.
5. Execute o app em um emulador ou dispositivo.

Se o Android Studio mostrar a mensagem `Can't find module entity`, feche e reabra o projeto depois da sincronizacao Gradle. O modulo esperado e `Complexo_Santos_e_Schein.app`.

## Modo mock/dev

Use o Build Variant `mockDebug`. Nesse modo, os repositorios usam dados em memoria e nao dependem de servidor real.

Credenciais mock:

```text
E-mail: usuario@teste.com
Senha: 123456
```

Fluxos cobertos no mock: login, cadastro (a conta criada também consegue entrar), home, nova reserva, reservas futuras, reservas passadas, cancelamento, perfil e logout.

## Modo remoto/prod

Use o Build Variant `prodDebug` ou `prodRelease`. Nesse modo, os repositorios usam Retrofit e OkHttp para chamar o backend configurado.

A URL padrao e:

```text
http://10.0.2.2:8080/
```

No emulador Android, `10.0.2.2` aponta para o `localhost` da maquina host. Em dispositivo fisico, use o IP da maquina que executa o backend (veja abaixo).

## Configurar URL base

O flavor `prod` le a URL, nesta ordem:

1. `-PapiBaseUrl=...` na linha de comando do Gradle;
2. `api.base.url=...` no `local.properties` desta pasta (nao vai para o Git);
3. o padrao `http://10.0.2.2:8080/` (emulador).

Exemplo para celular fisico na mesma rede do computador do back:

```properties
# app/local.properties
api.base.url=http://192.168.0.10:8080/
```

Os builds de **debug** aceitam HTTP sem TLS em qualquer endereco, por causa de `src/debug/res/xml/network_security_config.xml`. O build de release continua aceitando HTTP apenas para `10.0.2.2` e `localhost`. O flavor `mock` nao faz chamadas HTTP.

## Sessao e saida da conta (Parte 3)

- **Login:** o app envia o nome do aparelho no cabecalho `X-Device-Name`, que aparece no historico de acessos do administrador.
- **Sair da conta:** chama `POST /api/v1/auth/logout` fora da thread principal e sempre apaga o token local. Sem conexao, o app sai mesmo assim e avisa que o servidor nao confirmou a saida; a sessao expira sozinha no servidor.
- **Sessao recusada:** se o servidor recusar o token (sessao vencida ou encerrada), o app apaga o token, volta ao login e mostra "Sessão expirada. Entre novamente.". Credenciais erradas continuam mostrando "E-mail ou senha invalidos.".
- **Log HTTP em debug:** registra so os cabecalhos, com o `Authorization` oculto; a senha do login nao aparece no log.

## Testes

Rodar testes unitarios:

```powershell
.\gradlew.bat test --console=plain
```

Rodar build completo com lint, testes e assemble:

```powershell
.\gradlew.bat clean build --console=plain
```

Gerar APK de desenvolvimento:

```powershell
.\gradlew.bat assembleMockDebug --console=plain
```

## Endpoints esperados

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me/reservations/next`
- `GET /api/v1/reservations/available-days?year=2026&month=7`
- `GET /api/v1/reservations/available-slots?date=2026-07-10`
- `POST /api/v1/reservations`
- `GET /api/v1/users/me/reservations?status=upcoming`
- `GET /api/v1/users/me/reservations?status=past`
- `DELETE /api/v1/users/me/reservations/{reservationId}`
- `GET /api/v1/users/me`

## Observacoes importantes

- Esta pasta contem apenas o app Android. O backend fica em [`../back`](../back/README.md).
- Este projeto nao usa Room, SQLite, Realm ou qualquer banco local para regras de negocio.
- O modo mock usa dados simulados em memoria.
- A sessao usa `SharedPreferences` apenas para token JWT e dados minimos de sessao.
- O modo remoto usa HTTP na rede local; em um servidor compartilhado, use HTTPS.

## Troubleshooting

O ambiente local pode ter inspecao HTTPS por antivirus/proxy, gerando falhas como `PKIX path building failed` ou `certificate_unknown`. Nesse caso, aponte o Gradle para um truststore local no `gradle.properties` do **seu usuario** (`%USERPROFILE%\.gradle\gradle.properties`), e nao no `gradle.properties` do projeto, para nao quebrar o build dos outros integrantes. O comentario em [gradle.properties](gradle.properties) mostra o formato.

Se os certificados locais forem regenerados, recrie esse truststore antes de sincronizar ou rodar os testes novamente.
