# Prints das telas

Capturas geradas em 06/11/2026.

- **App (`app/`):** modo mock, com os dados de demonstração, renderizado com Robolectric (tela de 393×851 dp).
- **Área do administrador (`admin/`):** API ligada a um Oracle de teste com dados de exemplo, capturada no Chrome.

## App Android

| Arquivo | Tela | Requisitos |
|---|---|---|
| [01-login.png](app/01-login.png) | Login | RF02 |
| [02-cadastro.png](app/02-cadastro.png) | Cadastro | RF01 |
| [03-inicio.png](app/03-inicio.png) | Início com a próxima reserva | RF05 |
| [04-nova-reserva.png](app/04-nova-reserva.png) | Nova reserva: calendário e horários | RF03 |
| [05-nova-reserva-resumo.png](app/05-nova-reserva-resumo.png) | Nova reserva: resumo e confirmação | RF04 |
| [06-reservas-futuras.png](app/06-reservas-futuras.png) | Reservas futuras | RF06 |
| [07-confirmar-cancelamento.png](app/07-confirmar-cancelamento.png) | Confirmação de cancelamento | RF07 |
| [08-reservas-passadas.png](app/08-reservas-passadas.png) | Reservas passadas (situação "Concluída") | RF06 |
| [09-perfil.png](app/09-perfil.png) | Perfil e "Sair da conta" | RF08, RF09 |
| [10-sessao-expirada.png](app/10-sessao-expirada.png) | Volta ao login com "Sessão expirada. Entre novamente." | RF11 |

## Área do administrador (web)

| Arquivo | Tela | Requisitos |
|---|---|---|
| [11-admin-entrar.png](admin/11-admin-entrar.png) | Entrada do administrador | RF12 |
| [12-admin-acesso-restrito.png](admin/12-admin-acesso-restrito.png) | Cliente tentando entrar: "Acesso restrito ao administrador." | RF12 |
| [13-admin-historico.png](admin/13-admin-historico.png) | Histórico com as três situações: em aberto, saída confirmada e expirada | RF10, RF11, RF12 |
| [14-admin-filtro-usuario.png](admin/14-admin-filtro-usuario.png) | Histórico filtrado por usuário (Ana Silva em dois aparelhos) | RF10, RF11, RF12 |

Os nomes, e-mails e CPFs são fictícios.
