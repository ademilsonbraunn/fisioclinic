# Procedimentos de Correção — FisioClinic
> Gerado pela revisão de código de 06/06/2026.  
> Atualizar o campo **Status** conforme cada item for resolvido.

---

## 🔴 Críticos — resolver antes de qualquer deploy em produção

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| 1 | XSS via `innerHTML` com `nome` do usuário | `frontend/js/utils/auth.js:83` | ✅ Concluído |
| 2 | `/api/pacientes/**` sem autenticação | `backend/.../config/SecurityConfig.java:43` | ✅ Concluído |
| 3 | Botão "Acessar demonstração (sem login)" | `frontend/index.html:116` | ✅ Concluído |
| 4 | JWT secret padrão em texto simples | `backend/src/main/resources/application.properties:22` | ✅ Concluído |
| 5 | Credenciais do BD em texto simples no repositório | `backend/src/main/resources/application.properties:2–5` | ✅ Concluído |
| 6 | Zero testes automatizados | Projeto inteiro | ⬜ Pendente |
| 7 | N+1 queries ao listar pacientes | `backend/.../service/PacienteService.java:189` | ✅ Concluído |

---

## ⚠️ Possivelmente preocupantes — resolver em curto prazo

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| 8 | CORS com wildcard `*` | `backend/.../config/SecurityConfig.java:81` | ✅ Concluído |
| 9 | Token com fallback para `localStorage` | `frontend/js/utils/auth.js:22` | ✅ Concluído |
| 10 | Inconsistência de token entre módulos API (pacientes.js usa localStorage) | `frontend/js/api/pacientes.js:21` | ✅ Concluído |
| 11 | Logs em nível DEBUG em produção (risco LGPD) | `backend/src/main/resources/application.properties:35` | ✅ Concluído |
| 12 | Ausência de paginação nas listagens de fisioterapeutas/salas | `FisioterapeutaService.java`, `SalaService.java` | ✅ Concluído |
| 13 | Memory leak — event listeners acumulados a cada renderização | `frontend/js/pages/pacientes.js:163` | ✅ Concluído |
| 14 | Erro de busca de CEP silenciado sem feedback ao usuário | `frontend/js/pages/pacientes.js` (buscarCEP) | ✅ Concluído |
| 15 | Sem logging de contexto nos erros de chamadas API | Todos os `frontend/js/api/*.js` | ✅ Concluído |
| 16 | Sem rotação de logs no backend (disco pode esgotar) | Ausência de `logback-spring.xml` | ✅ Concluído |
| 17 | URLs do backend hardcoded em 5+ arquivos JS | `frontend/js/api/pacientes.js`, `sessoes.js`, etc. | ✅ Concluído |
| 18 | Sem pipeline de CI/CD | Ausência de `.github/workflows/` | ✅ Concluído |

---

## 💡 Sugestões — melhorias de qualidade

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| 19 | Validação de status do PlanoTratamento sem enum | `backend/.../controller/PlanoTratamentoController.java:75` | ✅ Concluído |
| 20 | Senha padrão `Fisio@123` hardcoded para novos fisioterapeutas | `backend/.../service/FisioterapeutaService.java:73` | ✅ Concluído |
| 21 | Sem rate limiting no endpoint de login (brute force) | `backend/.../controller/AuthController.java:40` | ✅ Concluído |
| 22 | Sem README de deploy com instruções e variáveis obrigatórias | Ausência de `README.md` | ✅ Concluído |

---

## Legenda de status
| Símbolo | Significado |
|---------|-------------|
| ✅ Concluído | Problema corrigido e validado |
| 🔄 Em andamento | Correção sendo implementada |
| ⬜ Pendente | Ainda não iniciado |
| ⏸️ Adiado | Decidido adiar intencionalmente (registrar motivo) |
