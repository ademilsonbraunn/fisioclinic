# 📋 FisioClinic — Registro de Alterações

---

## 📅 10/06/2026 — Terça-feira

### ⏰ 16:35 — Backend + Frontend (Prioridade 2 — Funcionalidades Clínicas)

**Feature 1 — Gráfico de Evolução do EVA (Chart.js)**
- `pages/prontuario.html`: adicionado CDN Chart.js 4.4.0 no `<head>`
- `pages/prontuario.html`: adicionado `#grafico-eva-section` com `<canvas id="grafico-eva">` na aba Evolução
- `js/pages/prontuario.js`: adicionada função `renderGraficoEva()` — plota linha dupla EVA Antes / EVA Após, ordenados por data, com cleanup de instância anterior via `window._graficoEvaInstance`
- `js/pages/prontuario.js`: `carregarEvolucoes()` chama `renderGraficoEva()` ao final
- `css/pages/prontuario.css`: adicionados estilos `.grafico-eva-section`, `.grafico-eva-wrap`

**Feature 2 — Reavaliação Comparativa**
- `pages/prontuario.html`: adicionada aba "Reavaliação" e painel `#tab-reavaliacao` com grid comparativo
- `js/pages/prontuario.js`: adicionada função `renderReavaliacao()` — compara primeira vs última anamnese, exibe indicadores ▼/=/▲ para EVA, postura, ADM, força muscular, goniometria
- `js/pages/prontuario.js`: `bindTabs()` — lazy-load da aba Reavaliação
- `css/pages/prontuario.css`: adicionados estilos `.reav-tabela-wrap`, `.reav-cabecalho`, `.reav-linha`, `.reav-indicador`, `.reav-melhora`, `.reav-piora`, `.reav-estavel`

**Feature 3 — Relatório PDF da Alta**
- `pages/prontuario.html`: adicionados `#btn-imprimir-wrap`, botão "Imprimir Relatório" e `#relatorio-print` (div oculta para impressão)
- `js/pages/prontuario.js`: adicionada função `montarRelatorioPrint(alta)` — popula o relatório com dados do paciente, diagnóstico, sessões realizadas, objetivos e orientações
- `css/pages/prontuario.css`: adicionado bloco `@media print` que oculta todo o layout e exibe apenas `.relatorio-print-view`

**Feature 4 — Fotos Comparativas na Evolução**
- `database/setup.sql`: colunas `nome_arquivo` e `tamanho_bytes` adicionadas à tabela `fotos_evolucao`
- `model/FotoEvolucao.java` (novo): entidade JPA mapeando `fotos_evolucao`
- `repository/FotoEvolucaoRepository.java` (novo): query por `evolucao_id` ordenada por `created_at`
- `dto/FotoEvolucaoResponse.java` (novo): DTO de resposta com metadados da foto
- `service/FotoEvolucaoService.java` (novo): upload/download/listagem/remoção de fotos em `uploads/evolucoes/{id}/`; valida tipo `image/*`
- `controller/FotoEvolucaoController.java` (novo): endpoints `POST/GET /api/evolucoes/{id}/fotos`, `GET .../arquivo`, `DELETE .../fotos/{id}`
- `js/api/evolucoes.js`: adicionadas funções `listarFotosEvolucao`, `uploadFotoEvolucao`, `deletarFotoEvolucao`, `urlFotoEvolucao`
- `js/pages/prontuario.js`: galeria de fotos por card de evolução — upload por tipo (antes/depois), exibição autenticada via Blob URL, remoção individual
- `css/pages/prontuario.css`: adicionados estilos `.evol-fotos-section`, `.evol-foto-label`, `.evol-foto-item`, `.evol-foto-thumb`, `.btn-deletar-foto`

**Feature 5 — Histórico de Alterações (Auditoria CFM 1.821/07)**
- `database/setup.sql`: tabela `auditoria_prontuario` adicionada com índice `idx_auditoria_paciente`
- `model/AuditoriaProntuario.java` (novo): entidade imutável (sem setters); campo `dados_novos` com `@JdbcTypeCode(JSON)` para compatibilidade Hibernate 6 + PostgreSQL JSONB
- `repository/AuditoriaRepository.java` (novo): query `findByPacienteId` com LEFT JOIN FETCH fisioterapeuta
- `dto/AuditoriaResponse.java` (novo): DTO sem `dados_novos` (LGPD — dados sensíveis não expostos)
- `service/AuditoriaService.java` (novo): `registrar()` fail-silent (try/catch); `listarPorPaciente()`
- `controller/AuditoriaController.java` (novo): `GET /api/auditoria/paciente/{id}`
- `service/EvolucaoService.java`: `criar()` — chama `auditoriaService.registrar("EVOLUCAO", ...)` após save
- `service/AnamneseService.java`: `criar()` e `atualizar()` — chamam `auditoriaService.registrar("ANAMNESE", ...)` após save
- `service/PlanoTratamentoService.java`: `criar()` — chama `auditoriaService.registrar("PLANO", ...)` após save
- `service/AltaService.java`: `registrar()` — chama `auditoriaService.registrar("ALTA", ...)` após save
- `js/api/auditoria.js` (novo): wrapper `listarAuditoriaPaciente(pacienteId)`
- `pages/prontuario.html`: adicionada aba "Histórico" e painel `#tab-historico` com timeline de auditoria
- `js/pages/prontuario.js`: funções `carregarAuditoria()` (lazy-load) e `renderAuditoria(eventos)` — timeline com ícone por tipo, data/hora pt-BR, nome do fisioterapeuta
- `css/pages/prontuario.css`: adicionados estilos `.auditoria-timeline`, `.auditoria-item`, `.auditoria-icone`, `.auditoria-detalhe`, `.auditoria-tipo`, `.auditoria-acao`, `.auditoria-fisio`, `.auditoria-data`

---

## 📅 13/06/2026 — Sábado

### ⏰ 10:00 — Frontend (M2 + M3 — Prioridade 1)

**M2 — Anamnese: Upload e gerenciamento de arquivos**
- `js/pages/prontuario.js`: adicionados imports `listarArquivos`, `uploadArquivo`, `deletarArquivo` de `api/anamneses.js`
- `js/pages/prontuario.js`: adicionado estado `arquivosAnamnese = {}` para cache por anamnese
- `js/pages/prontuario.js`: `cartaoAnamnese()` — adicionado `data-anamnese-id` no card e seção "Arquivos / Exames" com input de upload oculto
- `js/pages/prontuario.js`: `renderAnamneses()` — lazy-load de arquivos na primeira abertura do card + delegação de eventos para upload e remoção
- `js/pages/prontuario.js`: adicionadas funções `carregarArquivosCard()`, `renderArquivosCard()`, `uploadArquivosAnamnese()`, `deletarArquivoAnamnese()`, `downloadArquivo()`, `formatBytes()`
- `css/pages/prontuario.css`: adicionados estilos das classes `arq-*` (toolbar, item, ícone, nome, tamanho, botões de download e remoção)

**M3 — Plano de Tratamento: TCLE pós-salvar + badge**
- `js/pages/prontuario.js`: adicionados imports `registrarTcle`, `listarTclesPlano` de `api/planos.js`
- `js/pages/prontuario.js`: `carregarPlanos()` — verifica TCLE de cada plano em paralelo e seta `p._tcle = true/false`
- `js/pages/prontuario.js`: `cartaoPlano()` — exibe badge "TCLE ✓" quando `p._tcle === true`
- `js/pages/prontuario.js`: `salvarPlano()` — após criar o plano, se checkbox TCLE marcado, chama `registrarTcle()` e marca `novo._tcle = true`

**M3 — Plano de Tratamento: Dropdown de plano na agenda**
- `pages/agenda.html`: adicionado campo `sel-plano-sessao` na seção "Informações adicionais" do modal de agendamento
- `js/pages/agenda.js`: adicionado import `listarPlanos` de `api/planos.js`
- `js/pages/agenda.js`: adicionado estado `planosCache = {}` para evitar requisições repetidas
- `js/pages/agenda.js`: adicionada função `carregarPlanosModal(pacienteId, selecionarId)` — popula dropdown de planos ao selecionar paciente
- `js/pages/agenda.js`: `limparModal()` — reseta o select de plano ao abrir novo agendamento
- `js/pages/agenda.js`: `abrirModalEditar()` — carrega planos do paciente e pré-seleciona o plano vinculado
- `js/pages/agenda.js`: `coletarDados()` — inclui campo `plano_id` no payload enviado ao backend
- `js/pages/agenda.js`: `bindEvents()` — listener `change` em `sel-paciente` para recarregar os planos ao trocar o paciente

---

## 📅 11/06/2026 — Quarta-feira

### ⏰ 11:00 — Frontend
- `pages/prontuario.html`: adicionada 4ª aba "Alta e Relatórios" integrada ao prontuário (Tab button + painel completo Módulo 6)
- `pages/prontuario.html`: importado `alta.css` para reutilizar estilos de seções colapsáveis, chips e estrelas
- `js/pages/prontuario.js`: adicionado import de `criarAlta` e `listarAltasPaciente` de `api/altas.js`
- `js/pages/prontuario.js`: adicionadas funções `carregarAltaTab`, `preencherResumoTab`, `popularSelectPlanosTab`, `popularSelectFisioTab`, `mostrarAltaTabRegistrada`, `bindSectionsTab`, `bindSatisfacaoStarsTab`, `bindFormAlta`, `registrarAlta`
- `js/pages/prontuario.js`: aba Alta usa lazy-load na primeira ativação; após submit exibe card de sucesso inline sem redirecionamento
- `js/pages/pacientes.js`: adicionado botão "Alta" verde em cada linha de paciente (link direto para `alta.html?paciente_id=UUID`)
- `css/pages/pacientes.css`: adicionado estilo `.btn-action-alta` (verde suave, segue padrão dos outros botões de ação)
- `css/pages/prontuario.css`: adicionado override `#tab-alta .alta-footer { position: relative }` para evitar que o rodapé fique fixo dentro da aba

---

## 📅 09/06/2026 — Terça-feira

### ⏰ 17:00 — Backend
- Criado `Alta.java` — entidade JPA mapeada para a tabela `altas` (M6)
- Criado `AltaDTO.java` — DTO de request com validação (motivo, resultado, satisfacao_nota 1–5)
- Criado `AltaResponse.java` — DTO de response com nested PacienteResumo, PlanoResumo, FisioterapeutaResumo
- Criado `AltaRepository.java` — consultas JPA com JOIN FETCH; `existsByPacienteIdAndPlanoTratamentoId` previne duplicata por plano
- `SessaoRepository`: adicionado `countByPacienteIdAndStatus` usado pelo AltaService para calcular sessões realizadas (M6)
- Criado `AltaService.java` — valida paciente, plano e fisioterapeuta; calcula `num_sessoes_realizadas` automaticamente; marca plano como "concluido" ao registrar alta com plano vinculado
- Criado `AltaController.java` — endpoints: `POST /api/altas`, `GET /api/altas/paciente/{id}`, `GET /api/altas/{id}`
- `SecurityConfig`: adicionado `/api/altas/**` ao bloco autenticado

### ⏰ 17:30 — Frontend
- Criado `js/api/altas.js` — fetch wrapper: `criarAlta`, `listarAltasPaciente`, `buscarAlta`
- Criado `pages/alta.html` — página standalone com formulário de alta (motivo, resultado, relatórios, retorno, satisfação)
- Criado `js/pages/alta.js` — lógica completa: carrega paciente + planos + resumo do tratamento + altas existentes; valida e envia formulário
- Criado `css/pages/alta.css` — estilos: seções colapsáveis, chips de motivo com semântica, estrelas de satisfação, rodapé fixo
- `pages/prontuario.html`: adicionado botão "Registrar Alta" na toolbar da aba Evolução (M5 → M6)
- `js/pages/prontuario.js`: botão "Registrar Alta" recebe href com paciente_id dinâmico

### ⏰ 18:00 — Geral
- `atualizacoes_sistema`: inserido card "Alta e Relatórios disponível" (v1.4, NOVO_RECURSO)
- `CLAUDE.md`: Módulo 6 marcado como ✅ Concluído; versão atualizada v1.3 → v1.4
- 🔢 Versão: v1.3 → v1.4 — conclusão do Módulo 6 (Alta e Relatórios)

---

## 📅 08/06/2026 — Segunda-feira

### ⏰ 10:00 — Backend
- Criado `ResetSenhaAdminDTO.java` — DTO para redefinição de senha por administrador
- `FisioterapeutaService`: adicionado método `resetarSenha(UUID, ResetSenhaAdminDTO)` com codificação BCrypt
- `FisioterapeutaController`: adicionado endpoint `PATCH /api/fisioterapeutas/{id}/senha` (ADMIN-only via SecurityConfig)

### ⏰ 10:00 — Frontend
- Criado `pages/usuarios.html` — página dedicada de gestão de usuários (ADMIN-only)
  - Tabela com colunas: nome, e-mail, CRF, perfil (badge), status (ativo/inativo), ações
  - Filtros em tempo real: busca por nome/e-mail, perfil e status
  - Estatísticas: total de usuários, administradores e ativos
- Criado `js/pages/usuarios.js` — lógica completa: listar, criar, editar, redefinir senha, ativar/desativar
- Criado `css/pages/usuarios.css` — estilos da página de usuários
- `js/utils/auth.js`: `initTopbar()` atualizado para exibir o botão "Usuários" para perfil ADMIN
- `js/pages/login.js`: implementado "Lembrar de mim" — salva em `localStorage` (marcado) ou `sessionStorage` (padrão)
- Todas as páginas protegidas (`dashboard`, `admin`, `pacientes`, `agenda`, `prontuario`): adicionado `topbar-users-btn` no topbar (ADMIN-only)
- `atualizacoes_sistema`: registro inserido — "Módulo de Gestão de Usuários disponível" (v1.3, NOVO_RECURSO)

---

## 📅 07/06/2026 — Domingo

### ⏰ 15:40 — Geral — Conclusão do Módulo 5 (Evolução Clínica SOAP)

- **Validação obrigatória do backend executada (CLAUDE.md — 4 etapas):**
  - Compilação: `mvn compile -q` — OK, sem erros
  - Inicialização: `mvn spring-boot:run` — servidor iniciou corretamente (`Started FisioclinicApplication`)
  - Endpoints testados com token JWT:
    - `POST /api/evolucoes` → HTTP 201 ✅ (SOAP + EVA + técnicas JSONB persistidos)
    - `GET /api/evolucoes/paciente/{id}` → HTTP 200 ✅ (histórico com 3 evoluções)
    - `GET /api/evolucoes/sessao/{id}` → HTTP 200 ✅ (evolução por sessão retornada)
    - `POST /api/evolucoes` (duplicado) → HTTP 409 ✅ (unicidade por sessão validada)
  - Banco de dados: dados persistidos corretamente
- **CLAUDE.md:** M5 marcado como ✅ Concluído; versão bumped v1.2 → v1.3
- **`atualizacoes_sistema`:** registro inserido — "Evolução Clínica (SOAP) disponível" (v1.3, NOVO_RECURSO)
- 🔢 Versão: v1.2 → v1.3 — conclusão do Módulo 5 (Evolução Clínica SOAP)

### ⏰ Frontend — Melhorias visuais e de UX (redesign de UI)

- **`css/base.css` — Design tokens e shell global**
  - Cor de fundo `--bg` atualizada de `#F0F0F0` para `#F4F6FB` (azul-acinzentado sutil)
  - Tons de `--surface2`, `--border`, `--text`, `--text2`, `--text3` refinados para paleta mais fria e sofisticada
  - `--radius-sm` ampliado de 4px para 6px (cantos mais suaves em toda a UI)
  - Topbar: `border-bottom` substituído por `box-shadow` duplo (linha + névoa leve)
  - Sidebar: mesmo tratamento de sombra lateral
  - Nav item ativo: `border-left: 3px` removido; substituído por estilo de pílula com `border-radius` e padding lateral
  - Adicionado `::selection` com cor accent; `*:focus-visible` com outline suave; `scroll-behavior: smooth`

- **`css/components.css` — Componentes reutilizáveis**
  - Botão primário: gradiente `linear-gradient(135deg, #3366F0, #2D5BE3)` + elevação e sombra colorida no hover + retorno ao plano no active
  - Inputs: padding de `8px 12px` → `9px 14px`
  - Tabela: linhas pares com fundo `#FAFBFE`; hover com `--accent-bg` em vez de cinza
  - Modal backdrop: `backdrop-filter: blur(4px)` adicionado para efeito profissional de desfoque
  - Toast: redesenhado com fundo escuro temático por tipo (sucesso/erro/alerta), borda-left colorida, animação spring e barra de progresso CSS com expiração visual

- **`css/pages/login.css` + `index.html` — Redesign da tela de login**
  - Novo layout dois painéis em desktop (≥ 900px): painel esquerdo com gradiente azul profundo, logo, tagline, lista de 3 benefícios do sistema e círculos decorativos de fundo; painel direito com formulário em fundo branco
  - Mobile (< 900px): colapsa para coluna única com card centralizado — comportamento igual ao anterior
  - Botão de login: gradiente + hover elevado com sombra

- **`css/pages/dashboard.css` — Dashboard**
  - Saudação (`dash-greeting`): 1.5rem → 1.75rem, `letter-spacing` negativo; data com ponto decorativo accent
  - Stat chips: padding maior, número 1.75rem, ícones com gradiente sutil, hover com elevação leve
  - Module cards: barra superior de 3px → 4px com gradiente; ícone-wrap de 44px → 48px com gradiente radial e sombra colorida; hover com sombra intensa e borda temática
  - Quick actions: padding maior, hover com elevação e sombra azul

- **`css/pages/agenda.css` — Agenda**
  - Eventos no calendário: `border-left` 3px → 4px; sombra base; hover com escala leve (`scale(1.01)`) e sombra mais intensa
  - Dia atual (`is-today`): fundo `--accent-bg` no cabeçalho + sombra colorida no número circular
  - Botões de navegação de semana: envolvidos em container toggle com fundo cinza e borda

- **`css/pages/prontuario.css` — Prontuário**
  - Header do paciente: borda-left accent (4px) + avatar com gradiente e sombra colorida
  - Abas: redesenhadas em estilo segmented control (container pill com fundo cinza; tab ativo branco com sombra)
  - EVA slider: track 6px → 8px; 5 zonas de cor (verde → limão → âmbar → laranja → vermelho); thumb com sombra e hover scale; labels extremos coloridos ("Sem dor" em verde / "Dor máxima" em vermelho)
  - Letras SOAP: substituído fundo translúcido por gradientes coloridos (azul/verde/âmbar/vermelho) com texto branco e sombra

---

## 📅 06/06/2026 — Sábado

### ⏰ Backend + Frontend — Itens "Possivelmente preocupantes" (revisão de código)
- **#8 CORS restrito** (`SecurityConfig.java`): substituído `allowedOriginPatterns("*")` pela lista de origens lida de `cors.allowed-origins` em `application.properties`; wildcard removido
- **#9 Token localStorage documentado** (`auth.js`): adicionado comentário explicando que o fallback para `localStorage` é intencional para o recurso "lembrar de mim"; tradeoff vs. cookies httpOnly registrado
- **#10/#15/#17 API centralizada** (`frontend/js/`): criado `config.js` com `API_BASE_URL` único; todos os 6 módulos de API (`pacientes`, `sessoes`, `anamneses`, `planos`, `evolucoes`, `atualizacoes`) atualizados para importar de `config.js` e usar `getToken()` de `auth.js`; adicionado `console.error` com contexto de endpoint em todas as falhas de API
- **#11 Nível de log seguro** (`application.properties`): `logging.level.com.fisioclinic` alterado de `DEBUG` fixo para `${LOG_LEVEL:INFO}`; em desenvolvimento, exportar `LOG_LEVEL=DEBUG`
- **#12 Limite de registros** (`FisioterapeutaService`, `SalaService`): listagem passou a usar `PageRequest.of(0, 200, Sort.by("nome"))` — previne queries ilimitadas sem mudar o contrato da API (`List<>`)
- **#13 Memory leak corrigido** (`pacientes.js`): substituídos event listeners individuais adicionados a cada renderização por event delegation único no container `#listaPacientes` (listener adicionado uma só vez em `bindEvents`)
- **#14 Feedback de CEP** (`pacientes.js`): `buscarCEP()` agora exibe toast `warning` quando o CEP não é encontrado no ViaCEP e quando o serviço está indisponível; antes os erros eram silenciados
- **#16 Rotação de logs** (`backend/src/main/resources/logback-spring.xml`): criado arquivo de configuração Logback com rotação diária, retenção de 30 dias e cap de 500 MB em disco; console habilitado apenas fora do perfil `prod`
- **#18 Pipeline CI/CD** (`.github/workflows/ci.yml`): criado workflow GitHub Actions com dois jobs — backend (compilação + testes com PostgreSQL 11) e frontend (lint básico de arquivos JS)

---



### ⏰ Geral — Comentários no backend (Models, Repositories, DTOs, Config, Exceptions)
- **Comentários adicionados em 46 arquivos do backend** (continuação da sessão de documentação iniciada em 05/06):
  - **Models (9 arquivos)**: `Fisioterapeuta`, `Sala`, `Sessao`, `Anamnese`, `PlanoTratamento`, `Evolucao`, `ContatoEmergencia`, `ConvenioPaciente`, `AtualizacaoSistema` — blocos de cabeçalho com tabela, relacionamentos, regras de negócio e observações de LGPD onde aplicável
  - **Repositories (10 arquivos)**: documentação das queries customizadas, estratégias JOIN FETCH anti-N+1, lógica de intervalo de conflito de sessões e delete+insert de subentidades
  - **DTOs (19 arquivos)**: todos os records de request/response documentados com contexto de uso (POST vs PATCH, campos sensíveis, estrutura esperada de JSONB)
  - **Config (4 arquivos)**: `JwtUtil` (claims, algoritmo), `JwtFilter` (fluxo de autenticação), `SecurityConfig` (matriz de autorização), `DataInitializer` (seed idempotente)
  - **Exceptions (4 arquivos)**: `GlobalExceptionHandler` (mapeamento de erros), `ResourceNotFoundException`, `ConflictException`, `UnauthorizedException`
- **Atualizado** `--atualizações/estado-comentarios.md`: 100% dos arquivos comentados (96/96)

### ⏰ Geral — Correções de segurança e performance (revisão de código)
- **Segurança — XSS corrigido** (`frontend/js/utils/auth.js`): substituído `innerHTML` com template literal por `createElement` + `textContent` no dropdown do usuário; elimina vetor de XSS via campo `nome`
- **Segurança — Autenticação obrigatória em pacientes** (`backend/.../SecurityConfig.java`): removido `permitAll()` na rota `/api/pacientes/**`; agora exige token JWT; `frontend/js/api/pacientes.js` atualizado para importar `getToken()` de `auth.js` em vez de ler `localStorage` diretamente
- **Segurança — Botão demo removido** (`frontend/index.html`): removido link "Acessar demonstração (sem login)" que permitia acesso ao sistema sem credenciais
- **Segurança — Credenciais via variáveis de ambiente** (`backend/src/main/resources/application.properties`): credenciais do banco (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) e JWT secret (`JWT_SECRET`) agora usam `${VAR:default}` — dev continua funcionando com valores padrão, produção deve definir as variáveis
- **Performance — N+1 queries corrigido** (`backend/.../service/PacienteService.java`): listagem de pacientes passou de `1 + 2N` queries para `3` queries fixas; adicionados métodos `findByPacienteIdIn()` em `ContatoEmergenciaRepository` e `ConvenioPacienteRepository`; `toResponse` refatorado em dois overloads (individual para `buscarPorId`, batch para `listar`)
- **Criado** `--atualizações/corrigirprocedimentos.md`: tabela com os 22 problemas identificados na revisão e status de cada um

---

## 📅 05/06/2026 — Sexta-feira

### ⏰ 23:59 — Geral
- **Comentários em todo o frontend**: adicionados blocos de cabeçalho e comentários de seção em todos os 30 arquivos do frontend:
  - 8 arquivos CSS (`base.css`, `components.css`, 6 páginas)
  - 4 arquivos JS utilitários/componentes (`auth.js`, `modal.js`, `toast.js`, `notificacoes.js`)
  - 6 arquivos JS API wrappers (`pacientes.js`, `sessoes.js`, `anamneses.js`, `planos.js`, `evolucoes.js`, `atualizacoes.js`)
  - 6 arquivos JS pages (`login.js`, `pacientes.js`, `agenda.js`, `prontuario.js`, `dashboard.js`, `admin.js`)
  - 6 arquivos HTML (`index.html`, `dashboard.html`, `pacientes.html`, `agenda.html`, `prontuario.html`, `admin.html`)
- **Comentários no backend**: adicionados cabeçalhos e Javadocs em 20 arquivos Java:
  - `FisioclinicApplication.java`
  - 9 Controllers (`PacienteController`, `AuthController`, `FisioterapeutaController`, `SalaController`, `SessaoController`, `AnamneseController`, `PlanoTratamentoController`, `EvolucaoController`, `AtualizacaoSistemaController`)
  - 9 Services (`PacienteService`, `AuthService`, `FisioterapeutaService`, `SalaService`, `SessaoService`, `AnamneseService`, `PlanoTratamentoService`, `EvolucaoService`, `AtualizacaoSistemaService`)
  - 1 Model (`Paciente.java`)
- **Arquivo de estado**: criado `--atualizações/estado-comentarios.md` com tabela completa de todos os arquivos comentados e pendentes (50/94 concluídos)

### ⏰ 18:30 — Frontend
- **Dashboard — Card Prontuário**: removido badge "Em breve"; link atualizado de `prontuario.html` para `pacientes.html` (fluxo correto: pacientes → prontuário do paciente); descrição do card revisada; stat alterado para "Acessar prontuários"
- **Lista de pacientes — botões de ação**: substituídos botões icon-only (`btn-ghost btn-icon btn-sm`) por botões com ícone + texto — "Prontuário" (estilo primário em azul com borda) e "Editar Cadastro" (estilo ghost neutro); gap da `.row-actions` aumentado de 2px para 6px; adicionadas classes `.btn-action-prontuario` e `.btn-action-editar` em `pacientes.css`
- **Prontuário — correção menor**: removida referência morta `e.avaliacao_clinica` em `cartaoEvolucao()`; a propriedade nunca existiu na resposta do backend (o campo correto é `e.avaliacao`); fallback `?? e.avaliacao` sempre era acionado

### ⏰ 17:00 — Frontend
- **Navegação — Sidebar removido**: removido o `<aside class="sidebar">` de todas as páginas (dashboard, pacientes, agenda, prontuário, admin); conteúdo agora ocupa a largura total da tela
- **Botão "Início" no topbar**: adicionado em pacientes, agenda, prontuário e admin — ícone de seta + texto "Início" logo após o brand FisioClinic; ao clicar retorna para o dashboard (menu principal)
- **Brand clicável**: `topbar-brand` convertido de `<div>` para `<a>` nas sub-páginas, linkando para `dashboard.html`
- **CSS base.css**: `margin-left` do `.main` alterado de `var(--sidebar-w)` para `0`; adicionadas classes `.topbar-back-btn` e `.topbar-divider`
- **Correção**: botão admin no dashboard apontava para `dashboard.html` (erro); corrigido para `admin.html`

### ⏰ 16:30 — Frontend
- **Navegação — Sidebar simplificado**: removidos grupos "Principal" (Início) e "Sistema" (Administrador) do sidebar de todas as páginas; sidebar agora exibe apenas os 4 módulos clínicos (Pacientes, Agenda, Prontuário, Alta)
- **Menu administrador no topbar**: adicionado botão de engrenagem no topbar (oculto por padrão), visível apenas para usuários com perfil `ADMIN` — revelado via `initTopbar()` no `auth.js`; na página `admin.html` exibe estado ativo (azul)
- **Proteção da página admin**: `admin.js` redireciona para `dashboard.html` qualquer usuário sem perfil `ADMIN` que acesse `admin.html` diretamente
- **CSS**: adicionadas classes `.topbar-admin-btn` e `.topbar-admin-btn--active` no `base.css`

### ⏰ 14:40 — Backend + Frontend
- **Módulo 5 — Evolução Clínica (SOAP)**: aba de prontuário implementada e funcional

**Backend:**
- Criado `model/Evolucao.java`: entidade JPA com FK para `Sessao` (obrigatório), `Paciente` (derivado da sessão), `Fisioterapeuta` (opcional) e `PlanoTratamento` (opcional); campos SOAP: `subjetivo`, `objetivo`, `avaliacao_clinica`, `plano`; `tecnicas_realizadas` como JSONB; `eva_antes` e `eva_depois` com check 0–10; `num_sessao`, `tempo_atendimento_min`, `aparelhos` (TEXT), `codigo_tuss`, `observacoes`
- Criado `dto/EvolucaoDTO.java`: record de request com `@NotNull`, `@NotBlank`, `@Min/@Max` para EVA
- Criado `dto/EvolucaoResponse.java`: record de response com nested records `SessaoResumo`, `PacienteResumo`, `FisioterapeutaResumo`
- Criado `repository/EvolucaoRepository.java`: queries com JOIN FETCH; `findByPacienteId`, `findBySessaoId`, `existsBySessaoId`
- Criado `service/EvolucaoService.java`: regra de negócio — uma evolução por sessão (`ConflictException` se já existir); paciente derivado da sessão automaticamente
- Criado `controller/EvolucaoController.java`: `POST /api/evolucoes`, `GET /api/evolucoes/paciente/{id}`, `GET /api/evolucoes/sessao/{id}`
- Criada migration `database/migration_evolucoes_ajustes.sql`: tornou `fisioterapeuta_id` opcional, converteu `aparelhos` JSONB → TEXT, adicionou `num_sessao`, `observacoes`, `plano_tratamento_id`, `updated_at`

**Frontend:**
- Criado `js/api/evolucoes.js`: wrapper fetch com `listarEvolucoesPaciente`, `buscarEvolucaoSessao`, `criarEvolucao`
- Atualizado `prontuario.html`: aba "Evolução" habilitada (removido `disabled`); formulário SOAP completo com selects de sessão/fisioterapeuta/plano, duplo EVA (antes/depois), grid SOAP (S/O/A/P), chips de técnicas, campo de aparelhos e parâmetros, código TUSS
- Atualizado `prontuario.js`: lógica da aba — `carregarEvolucoes()`, `carregarSessoesPaciente()`; render de cards com accordion; SOAP colorido (S=azul, O=verde, A=âmbar, P=vermelho); EVA bidimensional; `popularSelectSessoes()` filtra apenas sessões REALIZADO sem evolução; validação de campos obrigatórios
- Atualizado `prontuario.css`: `.form-nova-evolucao`, `.soap-grid` (2 colunas), `.soap-field`, `.soap-letter` (4 variantes coloridas), `.evol-card`, `.soap-display`, `.empty-evolucao`, `.badge-amber`

**Validação:**
- Compilação: OK
- Inicialização: OK (`Started FisioclinicApplication in 6.704s`)
- `POST /api/evolucoes` → HTTP 201 ✅ (dados persistidos com SOAP e EVA)
- `GET /api/evolucoes/paciente/{id}` → HTTP 200 ✅ (1 evolução retornada)
- `GET /api/evolucoes/sessao/{id}` → HTTP 200 ✅
- Banco de dados: dados persistidos corretamente (num_sessao, eva_antes, eva_depois, subjetivo, objetivo) ✅

### ⏰ 13:30 — Backend + Frontend
- **Módulo 3 — Diagnóstico e Plano de Tratamento**: implementação completa

**Backend:**
- Criado `model/PlanoTratamento.java`: entidade JPA com `@ManyToOne(LAZY)` para Paciente (obrigatório), Anamnese (opcional) e Fisioterapeuta (opcional); campo `tecnicas` mapeado como `JSONB` (`List<String>`); campos obrigatórios: `diagnostico_cif`, `objetivos_curto_prazo`, `objetivos_longo_prazo`, `frequencia_semanal`, `num_sessoes_estimado`; campo `status` com valores: `ativo`, `concluido`, `cancelado`
- Criado `dto/PlanoTratamentoDTO.java`: record de request com validações Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Max`) e `@JsonProperty` snake_case; inclui `data_previsao_alta`, `cid10` e `hipoteses_tratamento` opcionais
- Criado `dto/PlanoTratamentoResponse.java`: record de response com nested records `PacienteResumo`, `AnamneseResumo` e `FisioterapeutaResumo`
- Criado `repository/PlanoTratamentoRepository.java`: queries JPQL com `LEFT JOIN FETCH` para fisioterapeuta e anamnese; métodos `findByPacienteId` e `findByPacienteIdAndStatus`
- Criado `service/PlanoTratamentoService.java`: CRUD completo com validação de status (`ativo/concluido/cancelado`); método `atualizarStatus` para transições de estado; PATCH parcial (só altera campos não-nulos)
- Criado `controller/PlanoTratamentoController.java`: endpoints `GET /api/planos`, `GET /api/planos/{id}`, `POST /api/planos`, `PATCH /api/planos/{id}`, `PATCH /api/planos/{id}/status`

**Frontend:**
- Criado `js/api/planos.js`: wrapper fetch com `listarPlanos`, `criarPlano`, `atualizarPlano`, `atualizarStatusPlano`
- Atualizado `prontuario.html`: aba "Plano de Tratamento" habilitada com formulário completo — chips multi-seleção para 16 técnicas fisioterapêuticas, selects para frequência (1–7×/semana), campos CIF, CID-10, objetivos curto/longo prazo, nº sessões estimado, previsão de alta, vínculo com anamnese e checkbox TCLE
- Atualizado `prontuario.js`: lógica da aba plano — carregamento inicial via API, render de cards com accordion (abrir/fechar), ações de concluir/cancelar plano, form com validação de campos obrigatórios, populate de selects (fisioterapeutas, anamneses do paciente)
- Atualizado `prontuario.css`: estilos para `.form-novo-plano`, `.tecnicas-chips`, `.chip-option` (multi-select visual), `.plano-card` com borda colorida por status, `.plano-acoes`, badges de status (verde/azul/vermelho), `.form-row.cols-3`

**Validação:**
- Compilação: OK
- Inicialização: OK (`Started FisioclinicApplication`)
- `GET /api/planos?paciente_id` → HTTP 200 ✅
- `POST /api/planos` → HTTP 201 ✅ (dados persistidos com JSONB de técnicas)
- `GET /api/planos/{id}` → HTTP 200 ✅
- `PATCH /api/planos/{id}/status` → HTTP 200 ✅
- Banco de dados: dados persistidos corretamente ✅

---

### ⏰ — Frontend
- **Dashboard — Abas Funcionais**: substituição do layout estático por um dashboard com 4 abas interativas
  - Aba **Visão Geral**: layout original preservado (stats + grid de módulos + acesso rápido)
  - Aba **Hoje**: lista de sessões do dia ordenadas por horário, com status colorido (agendado/confirmado/realizado/faltou/cancelado), paciente, fisioterapeuta e sala; exibe estado vazio se não houver sessões
  - Aba **Pacientes**: últimos 10 pacientes cadastrados com avatar (iniciais), CPF formatado, telefone e link direto ao prontuário
  - Aba **Agenda**: resumo semanal Seg–Sex com contagem de sessões por dia (card em destaque para hoje) + botão para a agenda completa
  - Stats "Sessões hoje" e "Realizadas" agora buscam dados reais via API (era placeholder fixo)
  - Saudação usa o nome real do usuário autenticado (via `getUsuarioNome()`) em vez de "João" fixo
  - Cada aba carrega dados apenas uma vez (sem re-fetch ao voltar para a aba)
  - Responsivo: colunas ocultas em telas menores (< 960 px)
- Arquivos alterados: `frontend/pages/dashboard.html`, `frontend/css/pages/dashboard.css`, `frontend/js/pages/dashboard.js`

---

## 📅 05/06/2026 — Quinta-feira

### ⏰ 02:50 — Backend + Frontend
- **Módulo 2 — Anamnese e Avaliação Inicial**: implementação completa

**Backend:**
- Criado `model/Anamnese.java`: entidade JPA com `@ManyToOne(LAZY)` para Paciente (obrigatório) e Fisioterapeuta (opcional); campo `avaliacao_fisica` mapeado como `JSONB` via `@JdbcTypeCode(SqlTypes.JSON)` e `Map<String, Object>`; campos de texto para queixa, histórico, antecedentes, medicamentos, alergias etc.
- Criado `dto/AnamneseDTO.java`: record de request com `@NotBlank` nos obrigatórios e `@JsonProperty` snake_case
- Criado `dto/AnamneseResponse.java`: record de response com nested records `PacienteResumo` e `FisioterapeutaResumo`
- Criado `repository/AnamneseRepository.java`: queries JPQL com `LEFT JOIN FETCH` para fisioterapeuta (nullable)
- Criado `service/AnamneseService.java`: `listarPorPaciente`, `buscarPorId`, `criar`, `atualizar` (PATCH parcial)
- Criado `controller/AnamneseController.java`: `GET /api/anamneses?paciente_id=UUID`, `GET /api/anamneses/{id}`, `POST /api/anamneses`, `PATCH /api/anamneses/{id}`
- Atualizado `SecurityConfig.java`: `/api/anamneses/**` requer autenticação
- Atualizado `GlobalExceptionHandler.java`: handler genérico agora loga a exceção antes de retornar 500

**Frontend:**
- Criado `frontend/pages/prontuario.html`: página de prontuário com cabeçalho do paciente, tabs (Anamnese ativa, Plano e Evolução em breve), formulário inline de nova avaliação com EVA slider (0-10), campos de antecedentes e avaliação física
- Criado `frontend/js/pages/prontuario.js`: carrega paciente por `?paciente_id=UUID`, lista anamneses em cards colapsáveis, formulário de criação com validação
- Criado `frontend/js/api/anamneses.js`: wrapper de API (`listarAnamneses`, `buscarAnamnese`, `criarAnamnese`, `atualizarAnamnese`)
- Criado `frontend/css/pages/prontuario.css`: estilos do cabeçalho do paciente, tabs, cards de anamnese, EVA slider com gradiente verde→âmbar→vermelho
- Atualizado `frontend/js/pages/pacientes.js`: botão ícone de prontuário na lista de pacientes → `prontuario.html?paciente_id=UUID`

**CLAUDE.md:**
- Módulo 1 atualizado para ✅ Concluído
- Módulo 2 atualizado para 🔨 Em Andamento
- Módulo 4 atualizado para ✅ Concluído

---

### ⏰ 02:30 — Frontend
- **Logout via topbar** — clicar no nome do usuário abre dropdown com botão "Sair"
- Criado `frontend/js/utils/auth.js`: módulo central de autenticação com `getToken()`, `getUsuarioNome()`, `getUsuarioPerfil()`, `logout()` e `initTopbar()`
- `login.js`: agora salva `usuario_nome` e `usuario_perfil` no `sessionStorage` após login bem-sucedido (antes só salvava o token)
- Todas as páginas protegidas atualizadas para usar `initTopbar()`:
  - `dashboard.js`, `pacientes.js`, `agenda.js`, `admin.js` — importam e chamam `initTopbar()`
  - `dashboard.html`, `pacientes.html`, `agenda.html`, `admin.html` — topbar padronizado com `id="topbar-user"`, `id="topbar-avatar"`, `id="topbar-nome"`
- `admin.js`: URL base corrigida de `/api` (relativa) para `http://localhost:8080/api` (absoluta)
- `css/base.css`: adicionados estilos do dropdown (`.user-dropdown`, `.user-dropdown-sair`, etc.) e hover no `.topbar-user`

---

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
