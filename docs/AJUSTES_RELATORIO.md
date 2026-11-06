# Ajustes de texto no relatório

O código implementa o que o relatório pede, mas algumas decisões de implementação são diferentes do texto das Partes 2 e 3. Esta lista mostra o que mudar no `.docx` para que relatório e código digam a mesma coisa. Cada item traz o trecho atual e uma sugestão de texto.

## 1. Banco e tecnologia do backend (Partes 2 e 3)

- **No relatório:** "Para a persistência inicial, utilizaremos um arquivo SQLite no protótipo do backend" e os trechos de `UsuarioDAO`/`ReservaDAO` em Python.
- **No código:** Java 21 + Spring Boot, banco Oracle 23 Free em Docker e tabelas criadas pelas migrations Flyway (`back/src/main/resources/db/migration`). O acesso aos dados usa Spring Data JPA.
- **Sugestão:** "O backend foi implementado em Java com Spring Boot. Os dados ficam em um banco Oracle central, executado em contêiner Docker, com o esquema versionado por migrations Flyway. O aplicativo acessa esses dados somente pela API REST."

## 2. Nomes das tabelas e colunas

As tabelas usam nomes em inglês. Vale incluir esta correspondência no dicionário de dados:

| Relatório | Banco | Observação |
|---|---|---|
| `usuario` | `users` | `nome`/`sobrenome` → `first_name`/`last_name`; `nascimento` → `birth_date`; `cpf` → `document`; `senha_hash` → `password_hash` |
| `usuario.perfil` | `users.role` | Valores `CLIENTE` ou `ADMIN` |
| `horario` | `soccer_fields` + `reservation_slots` | Ver item 3 |
| `reserva` | `reservations` | `usuario_id` → `user_id`; `horario_id` → `slot_id` + `reservation_date`; `valor_centavos` → `price`; `criada_em` → `created_at`; também tem `canceled_at` |
| `acesso_usuario` | `user_sessions` | `nome_usuario` → `user_name`; `dispositivo` → `device`; `login_em` → `login_at`; `expira_em` → `expires_at`; `logout_em` → `logout_at`; `encerrado_em` → `closed_at`; `motivo_encerramento` → `close_reason` |

## 3. Entidade `horario`

- **No relatório:** cada registro de `horario` é uma faixa com data e hora (`inicio`/`fim` no formato AAAA-MM-DD HH:MM:SS).
- **No código:** `soccer_fields` guarda os campos e `reservation_slots` guarda uma **grade diária** por campo (por exemplo, 19:00–20:00 em todos os dias). A data fica na reserva (`reservation_date`). A regra de "uma ocupação válida por faixa" usa um índice único sobre `(campo, data, faixa)` das reservas ativas, que equivale ao índice parcial do relatório.
- **Sugestão:** "Os horários são cadastrados como uma grade diária de faixas de uma hora para cada campo. A data escolhida pelo cliente fica registrada na reserva, e um índice único impede duas reservas ativas para o mesmo campo, data e faixa."

## 4. Valores em centavos

- **No relatório:** "Os valores serão armazenados em centavos; R$ 150,00 corresponderá a 15000."
- **No código:** `NUMBER(10,2)`, ou seja, reais com duas casas decimais (R$ 150,00 = `150.00`). A reserva guarda uma cópia do valor no momento da confirmação, como o relatório pede.
- **Sugestão:** "Os valores são armazenados em reais com duas casas decimais (NUMBER(10,2))."

## 5. Situações da reserva

- **No relatório:** `ACTIVE`, `CANCELLED` ou `COMPLETED`, todas gravadas.
- **No código:**
  - **Cancelada:** fica gravada como `CANCELED`, com um L só.
  - **Concluída (`COMPLETED`):** não é gravada. A API calcula essa situação para uma reserva ativa cujo horário já terminou.
  - **App:** aceita as duas grafias de cancelada.
- **Sugestão:** "No banco, a reserva fica `ACTIVE` ou `CANCELED`. A situação `COMPLETED` é calculada pela API quando o horário de uma reserva ativa já terminou."

## 6. Prazo de cancelamento

- Igual ao relatório: **24 horas** de antecedência. O valor fica em `app.reservation.minimum-cancel-hours` no `application.properties`.

## 7. Sessão e token (Parte 3)

- O token é um JWT assinado com o id do usuário, o e-mail, o id da sessão (`sid`) e a validade. A validade padrão é de **1 hora**, configurada em `app.jwt.expiration-seconds`.
- Toda chamada protegida confere no banco se a sessão continua aberta. Depois do logout, aquele token deixa de valer; os outros aparelhos não são afetados.
- Uma rotina do servidor encerra a cada minuto as sessões vencidas com motivo `EXPIRACAO`. `logout_at` continua nulo nesses casos.

## 8. Área do administrador (Parte 3)

- **Endereço:** a página web é servida pelo próprio backend em `http://localhost:8080/admin`.
- **Dados:** vêm da rota `GET /api/v1/admin/access-logs`, que aceita os filtros `from`, `to` e `userId` e é paginada.
- **Permissão:** é verificada no servidor. Sem login a API responde 401; um cliente recebe 403 com a mensagem "Acesso restrito ao administrador."
- **Como criar um administrador:** a promoção para `ADMIN` é uma operação controlada no banco, com o comando descrito no `README.md` da raiz.

## 9. Versões do relatório

Há duas versões diferentes de `Relatorio_ProjetoG2_Santos_e_Schein_261023.docx`:

- **`docs/Relatorio_ProjetoG2_Santos_e_Schein_261023.docx`:** a versão principal, com quatro integrantes.
- **`docs/versoes-do-repo-app/Relatorio_ProjetoG2_Santos_e_Schein_261023.docx`:** inclui **César Schroeder** na equipe e uma seção final "Continuidade da implementação".

O grupo precisa decidir qual versão vale e, se for o caso, incluir o integrante e a seção na versão principal.
