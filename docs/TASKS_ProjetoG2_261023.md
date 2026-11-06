# Tarefas futuras do Projeto G2 da aula de 23/10/2026

> **Situação em 06/11/2026:** as tarefas T01 a T05 foram implementadas: back, migrations V6–V8, página `/admin` e integração do app. Ficam pendentes a demonstração da T06 com dois celulares e o HTTPS de um servidor compartilhado. O progresso detalhado está em [CHECKLIST.md](CHECKLIST.md). As caixas abaixo mostram o plano original.

Este plano complementa a Parte 3 do relatório ProjetoG2_261023. As tarefas abaixo ainda não foram implementadas nesta atualização. A revisão considerou o código Android e o README disponíveis no repositório.

## Situação encontrada

- Existem telas de cadastro, login e perfil, além de repositórios remotos preparados para Retrofit.
- O README informa que este projeto não implementa backend e que os dados do modo mock ficam em memória.
- `MockAuthRepository` autentica somente a conta fixa de demonstração. O cadastro simulado não guarda a senha para permitir o login da nova conta.
- `RemoteAuthRepository.logout()` apenas limpa o token local. `AuthApi` ainda não declara uma operação de logout.
- `SharedPreferencesSessionStorage` conserva o token no aparelho. `GetInitialAppDestinationUseCase` verifica a presença desse token, sem validar sua sessão no servidor.
- Não foram encontrados modelo de histórico de acessos, perfil administrativo, API administrativa ou página para o administrador neste repositório.

Os trechos Python e SQLite do relatório anterior são uma proposta de persistência e não um backend integrado ao aplicativo. O armazenamento local do token, sozinho, não comprova acesso à mesma conta em aparelhos diferentes.

## T01 Implementar a conta e os dados compartilhados no backend

Prioridade: alta. Atende ao cadastro, login e RF10. Dependências: nenhuma.

- [ ] Criar um módulo ou serviço de backend com banco persistente central. O SQLite inicial deve ficar no servidor, conforme a proposta da Parte 2.
- [ ] Implementar `POST /api/v1/auth/register` e `POST /api/v1/auth/login` compatíveis com os DTOs atuais; normalizar e-mail e CPF e impedir duplicação.
- [ ] Armazenar apenas hash de senha com salt e verificar a senha no servidor. O login de uma conta recém-cadastrada deve funcionar com as próprias credenciais.
- [ ] Implementar `GET /api/v1/users/me` e integrar as consultas de reservas à mesma identidade de usuário, usando os contratos de `UserApi` e `ReservationApi`.
- [ ] Configurar uma URL do backend acessível aos dispositivos de demonstração. A URL `10.0.2.2` atual atende ao emulador local e não resolve, por si, o acesso de dois celulares físicos.
- [ ] Usar HTTPS no ambiente compartilhado e impedir o registro de senhas e tokens nos logs HTTP. Revisar o nível BODY de `OkHttpClientFactory` no fluxo de autenticação.

Critério de conclusão: cadastrar uma conta em um dispositivo, autenticar a mesma conta em outro Android compatível e recuperar o mesmo perfil e as mesmas reservas. Os registros devem continuar disponíveis após reiniciar o backend.

## T02 Criar o modelo e as migrações de acesso

Prioridade: alta. Atende ao RF11 e à modelagem de acesso. Depende de T01.

- [ ] Adicionar `usuario.perfil`, com valores `CLIENTE` e `ADMIN`, mantendo `CLIENTE` como padrão do cadastro público.
- [ ] Criar `acesso_usuario` com `id`, `usuario_id`, `nome_usuario`, `dispositivo`, `login_em`, `expira_em`, `logout_em`, `encerrado_em` e `motivo_encerramento`, conforme a Parte 3.
- [ ] Vincular cada acesso a uma conta por chave estrangeira. Permitir várias sessões simultâneas por usuário, com identificadores de sessão únicos.
- [ ] Tornar obrigatórios usuário, nome no acesso, login e validade. Permitir dispositivo nulo e saída/encerramento nulos enquanto a sessão estiver aberta.
- [ ] Guardar instantes em UTC e converter para `America/Sao_Paulo` na apresentação. Usar o relógio do servidor como referência.
- [ ] Preservar o histórico: logout ou expiração devem atualizar o registro, sem excluí-lo. Não guardar senha nem token completo no log.

Critério de conclusão: o banco permite dois acessos simultâneos da mesma conta e recupera seus dados de entrada e saída após ser reaberto, sem afetar as tabelas de reservas existentes.

## T03 Registrar login, logout e validade da sessão no servidor

Prioridade: alta. Atende ao RF11 e amplia o RF09. Depende de T02.

- [ ] Em cada login bem-sucedido, criar o registro de acesso antes de liberar a nova sessão. Não emitir o token se a gravação do log falhar.
- [ ] Emitir token assinado com usuário, id da sessão e expiração; rejeitar tokens vencidos ou vinculados a sessões encerradas nas rotas protegidas.
- [ ] Implementar `POST /api/v1/auth/logout`, autenticado. Preencher `logout_em`, `encerrado_em` e motivo `LOGOUT` e revogar a sessão correspondente em uma operação atômica.
- [ ] Tornar o logout idempotente: repetir a solicitação não cria outro log nem altera o horário de saída já confirmado.
- [ ] Tratar expiração com motivo `EXPIRACAO` e `encerrado_em` correspondente à validade. Manter `logout_em` nulo quando não houver logout confirmado.
- [ ] Rejeitar credenciais incorretas sem criar sessão autenticada. Reutilizar uma sessão válida ao reabrir o app, sem registrar um novo login.

Critério de conclusão: os acessos A1 e A2 da mesma conta têm horários independentes; encerrar A1 não invalida A2. A1 deixa de acessar as rotas protegidas, e uma sessão expirada não ganha um horário fictício de logout.

## T04 Integrar a sessão remota ao aplicativo Android

Prioridade: alta. Atende aos RF10 e RF11 e à saída da conta. Depende de T03.

- [ ] Incluir logout remoto em `AuthApi` e ajustar `AuthRepository`, `RemoteAuthRepository` e `LogoutUseCase` para tratar o resultado da operação.
- [ ] Executar a chamada de logout fora da thread principal, atualizando `ProfileViewModel` e `ProfileFragment` com estados de carregamento, sucesso e falha.
- [ ] Validar o token com o servidor ao retomar a sessão, reaproveitando `GET /api/v1/users/me` ou um contrato específico. A presença do token local não deve ser a única verificação.
- [ ] Limpar a sessão local e retornar ao login quando ela for inválida. Distinguir a mensagem de sessão expirada da mensagem de credenciais incorretas em `HttpErrorMapper`.
- [ ] Tratar logout sem conexão: permitir saída local, avisar que o encerramento remoto não foi confirmado e manter `logout_em` nulo no servidor até confirmação ou expiração, sem fabricar horários.
- [ ] Atualizar os dublês e o modo mock para os novos contratos e para representar cadastro seguido de login e sessões distintas durante a demonstração.

Critério de conclusão: o app reabre com uma sessão válida sem duplicar logs; volta ao login quando a sessão vence; e informa corretamente o resultado do logout, inclusive com falha de rede.

## T05 Implementar o acesso e a consulta administrativa

Prioridade: alta. Atende ao RF12 e ao layout solicitado. Depende de T02 e T03.

- [ ] Criar uma página web para desktop seguindo o layout da Parte 3, com e-mail, senha, entrada e saída.
- [ ] Disponibilizar uma forma controlada de atribuir o perfil `ADMIN`. O cliente não pode escolher esse perfil no cadastro nem alterá-lo em uma requisição própria.
- [ ] Criar a rota administrativa de consulta do histórico, por exemplo `GET /api/v1/admin/access-logs`, com filtros por período e usuário e paginação.
- [ ] Autorizar a consulta no backend: ausência de autenticação deve retornar 401 e perfil sem permissão deve retornar 403. Ocultar a página não substitui essa verificação.
- [ ] Exibir nome, data/hora de login, data/hora de logout e situação da sessão, distinguindo saída confirmada, sessão em aberto e expiração. Usar traço quando o logout não estiver registrado.
- [ ] Manter a consulta somente para leitura, com mensagem para ausência de resultados. Não expor hash de senha ou token na API ou na interface.
- [ ] Registrar o acesso e a saída do próprio administrador pelo mesmo mecanismo das demais sessões.

Critério de conclusão: um administrador consulta e filtra os registros; um cliente recebe 403 na API mesmo tentando acessá-la diretamente; uma sessão administrativa também aparece no histórico.

## T06 Validar os fluxos e preparar a demonstração da etapa

Prioridade: alta. Atende à verificação das entregas de 23/10. Depende de T01 a T05.

- [ ] Testar cadastro e login da nova conta, e-mail/CPF duplicados e senha incorreta.
- [ ] Demonstrar dois dispositivos com a mesma conta e diferentes sessões; confirmar acesso aos mesmos dados e preservação da outra sessão após logout.
- [ ] Conferir nome, data e hora no log, inclusive quando login e logout ocorrem em dias diferentes.
- [ ] Testar reabertura sem novo log, expiração do token, falha de gravação do log, repetição do logout e indisponibilidade da rede.
- [ ] Testar a persistência após reiniciar o serviço e o banco.
- [ ] Validar o bloqueio da consulta administrativa para clientes e usuários sem autenticação.
- [ ] Atualizar o README com o início do backend, migrações, URL de demonstração e preparação de uma conta administrativa de teste.
- [ ] Apresentar as telas, o modelo de dados e o fluxograma ao professor e realizar o envio do documento após a discussão.

Critério de conclusão: registrar evidências dos cenários acima e confirmar que o comportamento corresponde à Parte 3 do relatório. A execução dessas tarefas não faz parte da atualização documental entregue agora.
