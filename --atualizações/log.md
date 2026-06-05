# 📋 FisioClinic — Registro de Alterações

---

## 📅 05/06/2026 — Quinta-feira

### ⏰ 02:05 — Backend / Banco de Dados
- **Correção crítica — Agenda não permitia inserção de sessões**
- Causa raiz: `@Enumerated(EnumType.STRING)` no Java persiste enums em MAIÚSCULAS, mas os CHECK constraints do PostgreSQL foram definidos com minúsculas — todo INSERT era rejeitado com violação de constraint
- Criado `database/migration_fix_sessoes_constraints.sql`: migração que corrige:
  - CHECK constraint de `sessoes.tipo_sessao`: atualizado para maiúsculas + adicionados `REAVALIACAO` e `ALTA` (que existiam no Java mas faltavam no SQL), removido `RETORNO` (que não existe no Java)
  - CHECK constraint de `sessoes.status`: atualizado para maiúsculas
  - DEFAULT values de `sessoes`: `'sessao'` → `'SESSAO'`, `'agendado'` → `'AGENDADO'`
  - Colunas `senha_hash` e `perfil` adicionadas à tabela `fisioterapeutas` (faltavam no schema, necessárias para autenticação JWT)
  - Fisioterapeuta admin padrão inserido: `admin@fisioclinic.com` / `admin123`
  - CHECK constraint e DEFAULT de `salas.tipo` corrigidos para maiúsculas; valores existentes atualizados de lowercase para UPPERCASE
- Atualizado `database/schema_modulos_2a7.sql`: todas as definições corrigidas para uso futuro
- Corrigido `SessaoRepository.java`: `JOIN FETCH s.sala` → `LEFT JOIN FETCH s.sala` (sala é nullable — JOIN quebrava listagem se sala fosse null)
- Corrigido `SessaoService.toResponse()`: null-check para sala (previne NPE latente)

---

## 📅 05/06/2026 — Sexta-feira

### ⏰ 23:00 — Backend
- **Integração entre tabelas** — sessões ↔ pacientes expostas na API:
- `SessaoRepository.java`: adicionado `findByPacienteId` com JOIN FETCH (evita N+1)
- `SessaoService.java`: adicionado `listarPorPaciente(UUID)`; `listar()` aceita `pacienteId` opcional como 3º parâmetro
- `SessaoController.java`: `GET /api/sessoes` aceita `?paciente_id=UUID` para filtrar a agenda por paciente
- `PacienteController.java`: adicionado `GET /api/pacientes/{id}/sessoes` — retorna histórico completo de sessões do paciente (404 se ID inválido)

### ⏰ 22:15 — Backend
- **Módulo 4 — Agendamento**: implementação completa da camada Java (model → controller)
- Criado `database/migrate_sessoes_enums.sql`: migração que normaliza CHECK constraints da tabela `sessoes` de lowercase para UPPERCASE, adiciona `REAVALIACAO` e `ALTA` ao enum `tipo_sessao`, e atualiza defaults
- Criado `backend/.../model/Sessao.java`: entidade JPA com `@ManyToOne(LAZY)` para `Paciente`, `Fisioterapeuta` e `Sala`; enums internos `TipoSessao` e `StatusSessao`; auditoria com `@CreatedDate` / `@LastModifiedDate`
- Criado `backend/.../dto/SessaoDTO.java`: record de request com `@JsonProperty` snake_case e `@NotNull` nos campos obrigatórios
- Criado `backend/.../dto/StatusDTO.java`: record mínimo para `PATCH /status`
- Criado `backend/.../dto/SessaoResponse.java`: record de response com nested records `PacienteResumo`, `FisioterapeutaResumo`, `SalaResumo` (campos mínimos que o frontend usa)
- Criado `backend/.../repository/SessaoRepository.java`: `findByPeriodo` (JOIN FETCH, evita N+1), `findConflitos` e `findConflitosComExclusao` para detecção de conflito de sala
- Criado `backend/.../service/SessaoService.java`: `listar`, `listarSemana`, `buscarPorId`, `criar`, `atualizar`, `atualizarStatus`, `excluir`; regra de conflito de sala enforced via `ConflictException` (409); cálculo automático de `duracao_minutos`
- Criado `backend/.../controller/SessaoController.java`: 7 endpoints REST (`GET /`, `GET /semana`, `GET /{id}`, `POST /`, `PATCH /{id}`, `PATCH /{id}/status`, `DELETE /{id}`); `@DateTimeFormat(iso=DATE)` nos parâmetros de data
- Compilação verificada: `mvn compile` sem erros

### ⏰ 20:30 — Frontend
- **Módulo 4 — Agendamento**: implementação completa do frontend (calendário semanal + lista)
- Criado `frontend/pages/agenda.html`:
  - Calendário semanal interativo (07h–20h, grade de 30 min, 7 colunas)
  - Evento com cor por status (agendado=azul, confirmado=verde, realizado=cinza, faltou=âmbar, cancelado=vermelho)
  - Navegação entre semanas (`< Semana anterior` / `Próxima semana >`) e botão `Hoje`
  - Toggle de visualização: **Semana** (calendário) e **Lista** (tabela)
  - Filtros por fisioterapeuta e por status
  - Modal "Nova Sessão" / "Editar Sessão" com 5 seções: paciente + horário, profissional + local, tipo de sessão (radio chips), status (radio chips coloridos), informações adicionais
  - Campo motivo de cancelamento condicional (aparece ao selecionar status = Cancelado)
  - Alerta de conflito de sala em tempo real (detecta sobreposição de horário + sala)
  - Clicar em slot vazio do calendário pré-preenche data/hora no modal
  - Clicar em evento abre modal de edição com todos os campos preenchidos
  - Botão "Cancelar Sessão" no modo edição
- Criado `frontend/css/pages/agenda.css`:
  - Layout CSS Grid para o calendário (64px + 7 colunas dinâmicas)
  - Cartões de evento posicionados absolutamente (top + height calculados pela duração)
  - Suporte a eventos sobrepostos (distribuição lateral automática)
  - Badges e chips de status com paleta de cores semântica
- Criado `frontend/js/api/sessoes.js`: wrapper de API para `listarSessoes`, `listarSessoesSemana`, `criarSessao`, `atualizarSessao`, `atualizarStatusSessao`, `excluirSessao`
- Criado `frontend/js/pages/agenda.js`:
  - Geração dinâmica da grade do calendário via JS (26 slots × 7 colunas)
  - Algoritmo de agrupamento de sessões sobrepostas no mesmo dia/sala
  - Fallback completo para mock data quando backend não disponível
  - Chips de status com cores semânticas por variante CSS
  - Validação de formulário com feedback inline
  - Detecção de conflito: mesma sala + sobreposição de horário

---

## 📅 04/06/2026 — Quarta-feira

### ⏰ 21:30 — Backend
- Estruturação completa do backend para as páginas de Login e Administrador:
- **SQL (`database/schema_auth.sql`)**:
  - `ALTER TABLE fisioterapeutas` — adicionadas colunas `senha_hash` e `perfil` (FISIOTERAPEUTA | ADMIN)
  - `UPDATE salas` — normalização de `tipo` para MAIÚSCULAS + atualização do CHECK constraint
- **Models**: `Fisioterapeuta.java` (com `senhaHash`, `perfil`, auditoria) e `Sala.java` (enum `TipoSala`)
- **Exceptions**: `UnauthorizedException` (401) + handler em `GlobalExceptionHandler`
- **DTOs**: `FisioterapeutaDTO/Response`, `SalaDTO/Response`, `LoginDTO`, `TokenResponse`, `SenhaDTO`
- **Repositories**: `FisioterapeutaRepository` e `SalaRepository` com queries derivadas
- **Services**: `FisioterapeutaService` (CRUD + status), `SalaService` (CRUD + listarAtivas), `AuthService` (login + troca de senha)
- **Controllers**:
  - `AuthController` — `POST /api/auth/login`, `PATCH /api/auth/senha`
  - `FisioterapeutaController` — GET, POST, PATCH, PATCH /status
  - `SalaController` — GET, GET /ativas, POST, PATCH
- **Config JWT**: `JwtUtil` (geração/validação com JJWT 0.12.5), `JwtFilter` (intercepta Bearer token)
- **`SecurityConfig`** reescrito: JWT filter ativo, `PasswordEncoder` BCrypt, rotas protegidas por perfil
- **`DataInitializer`**: cria admin padrão (`admin@fisioclinic.com` / `Admin@123`) se nenhum ADMIN existir
- **Frontend (`admin.js`)**: requests passam `Authorization: Bearer <token>` via `sessionStorage`
- **Frontend (`admin.html`)**: select de tipo de sala corrigido para valores `BOX | SALA_INDIVIDUAL | SALA_GRUPO`

### ⏰ 20:30 — Frontend
- Criada página de login (`index.html`) redesenhada com CSS externo:
  - Migrado de estilos inline para `css/base.css` + `css/pages/login.css`
  - Toggle mostrar/ocultar senha com troca de ícone
  - Checkbox "Lembrar de mim" + link "Esqueci minha senha"
  - Banner de erro com ícone e estado de loading no botão
- Criado `js/pages/login.js` com toggle de senha e submit com fetch real (`POST /api/auth/login`)
- Criada página `pages/admin.html` — painel do administrador:
  - Row de stats: Fisioterapeutas, Salas, Pacientes, Versão do sistema
  - Seção "Perfil da clínica" com formulário completo (nome, CNPJ, telefone, endereço)
  - Seção "Fisioterapeutas" com tabela, busca e modal de cadastro/edição
  - Seção "Salas e boxes" com tabela e modal de cadastro/edição
  - Seção "Segurança" com formulário de alteração de senha
- Criado `css/pages/admin.css` e `js/pages/admin.js`
- Sidebar de `dashboard.html` e `pacientes.html` atualizada com grupo "Sistema" → link "Administrador"

### ⏰ 20:05 — Frontend
- Paleta de cores ajustada para tons cinza neutros em `css/base.css` e `CLAUDE.md`:
  - `--bg` `#E4E2DD` → `#F0F0F0` (fundo geral — cinza claro neutro)
  - `--surface2` `#EDECE8` → `#E8E8E8` (fundo secundário — cinza um tom abaixo)
  - `--border` `#D8D6D0` → `#DCDCDC` (borda neutral sem tom quente)
  - `--text2` `#6B6A65` → `#666666` | `--text3` `#A09E99` → `#9E9E9E` (textos neutros)
  - Sombras ajustadas de `rgba(28,27,25,…)` para `rgba(0,0,0,…)` (sem tonalidade quente)

### ⏰ 19:50 — Geral
- Módulo 6 (Faturamento) removido do escopo do projeto conforme decisão registrada no CLAUDE.md
- **Frontend:** removido link "Faturamento" da sidebar em `dashboard.html` e `pacientes.html`
- **Frontend:** removido chip de estatística "Faturado hoje" do `dashboard.html`
- **Frontend:** removido card do módulo Faturamento da grade de módulos em `dashboard.html`
- **Frontend:** removida referência a `statFaturamento` em `dashboard.js`
- **Banco de dados:** removida tabela `faturamentos` e seus índices de `schema_modulos_2a7.sql` e `schema_fix_sessoes.sql`
- **CLAUDE.md:** removidas todas as referências ao módulo de Faturamento (visão geral, estrutura de pastas, endpoints, entidades e regras de negócio)

---

## 📅 04/06/2026 — Quarta-feira

### ⏰ 19:30 — Frontend
- Criada página inicial `pages/dashboard.html` — Portal de módulos:
  - Saudação dinâmica (Bom dia / Boa tarde / Boa noite) + data atual
  - 4 chips de estatísticas: Pacientes (real da API), Sessões, Realizadas, Faturamento
  - 6 cards de módulos com ícone, cor, descrição e badge de status
  - Seção "Acesso rápido" com botões de atalho
- Criado `css/pages/dashboard.css` com layout responsivo e animações de hover nos cards
- Criado `js/pages/dashboard.js` com saudação dinâmica e busca de stats na API
- `index.html` agora redireciona para `dashboard.html` ao clicar em "Demonstração"
- Sidebar de `pacientes.html` atualizado com link "Início" para o dashboard
- Corrigido tratamento de erro em `handleSubmit`: diferencia erro de rede (TypeError) de erro real da API (400/409)
- Corrigida config CORS: `allowCredentials(false)` e `allowedOriginPatterns("*")` para desenvolvimento

### ⏰ 18:30 — Frontend
- Substituídos 3 `<select>` do formulário de cadastro de paciente por chips de radio visuais:
  - **Sexo**: Feminino | Masculino | Outro | Não informado
  - **Estado civil**: Solteiro(a) | Casado(a) | Divorciado(a) | Viúvo(a) | União estável
  - **Parentesco** (contato de emergência): Cônjuge | Pai/Mãe | Filho(a) | Irmão/Irmã | Amigo(a) | Outro
- Adicionado estilo `.radio-chips` / `.chip` / `.chip-label` em `components.css`
- Adicionadas funções em `pacientes.js`: `getRadioValue`, `setRadioValue`, `syncChip`, `resetChips`, `bindChipEvents`
- Atualizado `validar()`, `preencherForm()`, `abrirModalNovo()`, `abrirModalEditar()`, `limparErros()`

### ⏰ 17:00 — Geral
- Criado `database/schema_modulos_2a7.sql` — schema completo dos módulos 2 a 7
- Criado `database/schema_fix_sessoes.sql` — fix para tabelas que dependem de `sessoes`
- 14 tabelas aplicadas no banco `fisioclinic`:
  - `fisioterapeutas`, `salas` (cadastros de apoio)
  - `anamneses`, `arquivos_anamnese` (Módulo 2)
  - `planos_tratamento`, `termos_consentimento` (Módulo 3)
  - `sessoes` (Módulo 4 — regra de sobreposição sala/horário aplicada no service)
  - `evolucoes`, `fotos_evolucao` (Módulo 5)
  - `faturamentos` (Módulo 6)
  - `altas` (Módulo 7)
- `GRANT ALL PRIVILEGES` ao usuário `fisio` nas novas tabelas e sequences
- Dados de exemplo inseridos: 2 fisioterapeutas, 5 salas

### ⏰ 16:15 — Geral
- Ajuste de `ddl-auto` de `validate` → `none` no `application.properties` para evitar conflito de tipos CHAR/VARCHAR entre schema SQL e Hibernate 6
- Concedido `GRANT ALL PRIVILEGES ON ALL TABLES` e `SEQUENCES` ao usuário `fisio` no banco `fisioclinic`

### ⏰ 16:05 — Geral
- Instalação do Maven 3.9.6 em `C:\maven` via download direto do Maven Central
- Maven adicionado ao PATH do usuário
- Primeira execução do `mvn spring-boot:run` — diagnóstico de erros de schema e permissão

### ⏰ 15:30 — Geral
- Banco de dados `fisioclinic` criado localmente (PostgreSQL 11, porta 5411)
- Usuário `fisio` criado com senha `fisio123`
- Schema aplicado via `database/setup.ps1`
- Atualizada porta de `5432` → `5411` em `setup.ps1` e `application.properties`
- Corrigido `setup.ps1` para usar apenas caracteres ASCII (encoding Windows-1252)

### ⏰ 14:30 — Backend
- Criação da estrutura completa do backend Spring Boot (Módulo 1 — Cadastro de Paciente):
  - `backend/pom.xml` — Spring Boot 3.2.5, JPA, Security, Validation, Lombok, JJWT
  - `FisioclinicApplication.java` — entry point com `@EnableJpaAuditing`
  - `model/Paciente.java` — entidade principal com auditoria JPA
  - `model/ContatoEmergencia.java` — contato de emergência do paciente
  - `model/ConvenioPaciente.java` — convênio/pagamento vinculado ao paciente
  - `dto/PacienteDTO.java` — record de entrada com Bean Validation
  - `dto/PacienteResponse.java` — record de saída (estrutura plana)
  - `repository/PacienteRepository.java` — com query JPQL de busca por nome/CPF
  - `repository/ContatoEmergenciaRepository.java`
  - `repository/ConvenioPacienteRepository.java`
  - `service/PacienteService.java` — CRUD completo com validação de CPF duplicado
  - `controller/PacienteController.java` — endpoints GET, POST, PATCH
  - `exception/ResourceNotFoundException.java` — 404
  - `exception/ConflictException.java` — 409 (CPF duplicado)
  - `exception/GlobalExceptionHandler.java` — `@RestControllerAdvice` com respostas padronizadas
  - `config/SecurityConfig.java` — CORS configurado, autenticação desativada para desenvolvimento
  - `src/main/resources/application.properties` — datasource, JPA, JWT, CORS, Jackson

### ⏰ 13:30 — Geral
- Criado `database/schema.sql` — DDL completo para Módulo 1:
  - Tabelas: `pacientes`, `contatos_emergencia`, `convenios_paciente`
  - Índices em `nome_completo`, `cpf` e `paciente_id`
  - Dados de exemplo para desenvolvimento
- Criado `database/setup.ps1` — script PowerShell de setup local:
  - Cria usuário `fisio` e banco `fisioclinic` automaticamente
  - Aplica o `schema.sql`
  - Suporte a porta customizada (5411)

### ⏰ 12:00 — Frontend
- Criação da estrutura completa do frontend (Módulo 1 — Cadastro de Paciente):
  - `frontend/css/base.css` — reset, variáveis CSS, layout (sidebar + topbar fixos)
  - `frontend/css/components.css` — design system: botões, inputs, modal, tabela, badges, avatar, toast, spinner, toggle group, photo upload
  - `frontend/css/pages/pacientes.css` — estilos específicos da listagem de pacientes
  - `frontend/index.html` — tela de login com link de acesso à demonstração
  - `frontend/pages/pacientes.html` — tela completa: sidebar, listagem, modal de cadastro com 4 seções (Dados pessoais, Contato, Convênio, Emergência)
  - `frontend/js/components/toast.js` — notificações flutuantes com ícones e auto-dismiss
  - `frontend/js/components/modal.js` — controle de abertura/fechamento com foco acessível
  - `frontend/js/api/pacientes.js` — fetch wrappers com autenticação JWT (Bearer token)
  - `frontend/js/pages/pacientes.js` — lógica completa: busca, filtros, máscaras CPF/telefone/CEP, auto-preenchimento via ViaCEP, preview de foto, validação de CPF, mock data com fallback automático

---
