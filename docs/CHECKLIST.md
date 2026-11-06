# Checklist do Projeto G2: relatório × implementação

Comparação entre o relatório `Relatorio_ProjetoG2_Santos_e_Schein_261023.docx` e o código de `back/` e `app/`. Revisão de 06/11/2026, depois dos ajustes das Partes 1–2 e da implementação da Parte 3.

Legenda: ✅ pronto · ⚠️ pronto, mas com pendência · ❌ falta fazer · — não se aplica

## Resumo

| Parte do relatório | Situação | Progresso |
|---|---|---|
| Parte 1: requisitos RF01–RF09 | Todos implementados e verificados no back (testes + roteiro no Oracle) | ███████████ 100% |
| Parte 2: telas e modelo de dados | Telas e modelo prontos; falta só alinhar o texto do relatório (M2, M5) | ██████████░ ~95% |
| Parte 3: vários aparelhos, log de acesso e área admin | Back, página admin e app prontos e testados; falta rodar o app num aparelho e fazer a demonstração com dois celulares | ██████████░ ~95% |

## Como foi verificado

- **Back, testes unitários:** 95 testes, todos passando (JDK 21 em Docker).
- **Back no Oracle de verdade (Oracle 23 Free + Flyway):**
  - **Atualização:** migrations V6–V8 aplicadas sobre um banco que estava em V5 com dados antigos (e-mail com maiúsculas, CPF com máscara, reserva sem valor).
  - **Instalação nova:** V1–V8 aplicadas do zero.
  - **Schema:** o Hibernate validou o schema ao subir a API (`ddl-auto=validate`).
  - **Roteiro ponta a ponta:** 40 verificações passando, cobrindo cadastro, login em dois aparelhos, reserva, logout, área admin, 401/403 e expiração.
- **Página do administrador:** aberta no navegador; login, tabela, filtros e situações conferidos.
- **App:** compilado com o Android SDK (JDK 21 em Docker). Os testes unitários passaram: 57 testes em `mockDebug` e 57 em `prodDebug`, todos passando. O APK `prodDebug` também foi gerado. Ainda não rodou num emulador ou celular contra o back.

## Tabela de progresso

| # | Item do relatório | App | Back | Situação | Observação |
|---|---|---|---|---|---|
| **Parte 1** | **Requisitos funcionais** | | | | |
| RF01 | Cadastro com dados pessoais e senha | ✅ | ✅ | ✅ | O back exige senha forte (8+, letra, número e símbolo), grava o e-mail em minúsculas e o CPF só com dígitos, e recusa CPF sem 11 dígitos |
| RF02 | Login por e-mail e senha, sessão por token | ✅ | ✅ | ✅ | O e-mail é aceito com qualquer combinação de maiúsculas |
| RF03 | Consultar datas e horários disponíveis | ✅ | ✅ | ✅ | — |
| RF04 | Confirmar reserva sem duplicar o horário | ✅ | ✅ | ✅ | — |
| RF05 | Próxima reserva na tela inicial | ✅ | ✅ | ✅ | — |
| RF06 | Reservas futuras e passadas | ✅ | ✅ | ✅ | Status em português no app; cancelada aparece como "Cancelada"; reserva já realizada aparece como "Concluída" |
| RF07 | Cancelar com 24 h de antecedência | ✅ | ✅ | ✅ | 24 h no back (`app.reservation.minimum-cancel-hours`); o app mostra a mensagem de prazo |
| RF08 | Consultar dados do perfil | ✅ | ✅ | ✅ | — |
| RF09 | Sair da conta | ✅ | ✅ | ✅ | Agora também encerra a sessão no servidor (ver RF11) |
| **Parte 2** | **Telas e persistência** | | | | |
| T1–T8 | 8 telas do protótipo, com as mensagens do relatório | ✅ | — | ✅ | Prints em [prints/](prints/README.md). Corrigido: os títulos "Nova reserva", "Cadastro" e "Dados cadastrais" não apareciam (os layouts usavam `android:title` em vez de `app:title`) |
| M1 | Entidade `usuario` | ✅ | ✅ | ✅ | V6: e-mail em minúsculas e CPF com 11 dígitos (CHECK no banco) |
| M2 | Entidade `horario` | ✅ | ✅ | ⚠️ | Feita como grade diária por campo e não como horário com data. Funciona; falta **ajustar o texto do relatório** ([AJUSTES_RELATORIO.md](AJUSTES_RELATORIO.md), item 3) |
| M3 | Entidade `reserva` | ✅ | ✅ | ✅ | V7: a reserva guarda a cópia do valor; `COMPLETED` é calculado pela API |
| M4 | Uma única ocupação válida por horário | — | ✅ | ✅ | Índice único sobre colunas virtuais, equivalente ao índice parcial do relatório |
| M5 | Banco do protótipo | — | ✅ | ⚠️ | O código usa Oracle + Flyway + Java; o relatório ainda fala em SQLite + Python. **Ajustar o texto** ([AJUSTES_RELATORIO.md](AJUSTES_RELATORIO.md), itens 1, 2 e 4) |
| **Parte 3** | **Acesso em vários aparelhos e log** | | | | |
| RF10 | Mesma conta em aparelhos diferentes | ✅ | ✅ | ⚠️ | Banco central e sessões independentes verificados. A URL da API agora é configurável (`api.base.url` no `local.properties`). Falta testar com dois celulares físicos; HTTPS só é necessário num servidor compartilhado |
| M6 | Campo `usuario.perfil` (CLIENTE/ADMIN) | — | ✅ | ✅ | V8: coluna `role`. O cadastro sempre cria CLIENTE; enviar `role` no cadastro é ignorado |
| M7 | Tabela `acesso_usuario` | — | ✅ | ✅ | V8: tabela `user_sessions`, com instantes em UTC e CHECKs de consistência |
| RF11a | Registrar o login | ✅ | ✅ | ✅ | O log é gravado antes de o token ser emitido; se a gravação falhar, não há token. O app envia o nome do aparelho (`X-Device-Name`) |
| RF11b | Logout encerrado no servidor | ✅ | ✅ | ✅ | `POST /api/v1/auth/logout` encerra só a sessão do token e é idempotente. App: logout em segundo plano, com aviso quando estiver sem conexão (coberto por testes) |
| RF11c | Expiração e validação da sessão | ✅ | ✅ | ✅ | A sessão vencida vira `EXPIRACAO` sem horário de logout; o token vale só com a sessão aberta. App: 401 leva ao login com "Sessão expirada. Entre novamente." |
| RF12 | Área administrativa (página web + API com 401/403) | — | ✅ | ✅ | Página em `/admin` com período, usuário, paginação, situação e "—" sem logout. Cliente recebe 403 "Acesso restrito ao administrador." O acesso do próprio admin também entra no log |
| **Geral** | **Infraestrutura e qualidade** | | | | |
| G1 | Projeto unificado (`back/` + `app/` + `docs/`) | ✅ | ✅ | ✅ | — |
| G2 | Build funcionando em qualquer máquina | ✅ | ✅ | ✅ | O `gradle.properties` do app tinha caminhos do PC de outro integrante |
| G3 | Testes automatizados | ✅ | ✅ | ✅ | Back: 95 unitários + 40 verificações no Oracle. App: 57 unitários por variante (`mockDebug` e `prodDebug`). Todos passando |
| G4 | `GET /api/v1/health` público | — | ✅ | ✅ | — |
| G5 | Logs sem senha/token | ✅ | ✅ | ✅ | O app registra só os cabeçalhos (sem corpo) e oculta o `Authorization`; o back nunca devolve hash nem token na API |
| G6 | Relatório único | — | — | ⚠️ | Há duas versões do `_261023` (com e sem César Schroeder). Decisão do grupo ([AJUSTES_RELATORIO.md](AJUSTES_RELATORIO.md), item 9) |

## Cronograma

| Sprint | Período | Previsto | Situação |
|---|---|---|---|
| 1 | 09–15/10 | Problema, público, requisitos, PB e cronograma | ✅ |
| 2 | 16–22/10 | Telas, DER e persistência inicial | ✅ |
| 3 | 23–29/10 | Cadastro, login, disponibilidade e confirmação | ✅ |
| 4 | 30/10–05/11 | Consultas, cancelamento, perfil e apresentação | ✅ (ajustes concluídos em 06/11) |
| Parte 3 | a partir de 23/10 | Sessões, log de acesso e área administrativa (T01–T05) | ✅ implementado · ⚠️ falta a demonstração (T06) |

## Próximos passos

1. **Rodar o app de verdade:** abrir `app/` no Android Studio e rodar `prodDebug` no emulador com o back ligado. Conferir login, reserva, "Sair da conta" e a mensagem de sessão expirada. A compilação e os testes unitários já passaram.
2. **Demonstração (T06):** configurar `api.base.url` com o IP da máquina do back e usar dois celulares com a mesma conta: fazer logout em um, conferir que o outro continua e consultar o histórico em `/admin`.
3. **Relatório:** aplicar os ajustes de [AJUSTES_RELATORIO.md](AJUSTES_RELATORIO.md) e decidir qual versão do `_261023` vale.
4. **Opcional:** publicar o back num servidor com HTTPS para os celulares acessarem fora da rede local.
