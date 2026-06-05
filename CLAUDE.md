# FisioClinic — Contexto do Projeto

## Visão geral
Sistema de gestão clínica para fisioterapia. Porte: clínica média (6–20 fisioterapeutas).
Módulos: Cadastro, Anamnese, Plano de Tratamento, Agendamento, Evolução Clínica, Alta.

---

## Stack

### Frontend
- HTML5 semântico
- CSS3 (sem framework — classes utilitárias próprias)
- JavaScript puro (ES6+, sem framework)
- Fetch API para chamadas ao backend
- Organização em módulos ES6 (`import/export`)

### Backend
- Java 17+
- Spring Boot 3
- Spring Web (REST Controllers)
- Spring Data JPA (ORM)
- Spring Security (autenticação JWT)
- Maven (gerenciador de dependências)

### Banco de dados
- PostgreSQL 11 (principal)
- Transações ACID para prontuário
- JSONB para campos dinâmicos (avaliação física, técnicas, aparelhos)
- Localhost em desenvolvimento

---

## Trilha de módulos

| # | Módulo                                   | Status              |
|---|------------------------------------------|---------------------|
| 1 | Cadastro do paciente                     | 🔨 Em Andamento     |
| 2 | Anamnese e avaliação inicial             | 🔲 pendente         |
| 3 | Diagnóstico e plano de tratamento        | 🔲 pendente         |
| 4 | Agendamento / Recepção                   | 🔲 Pendente         |
| 5 | Evolução clínica (SOAP)                  | 🔲 pendente         |
| 6 | Alta e relatórios                        | 🔲 pendente         |

---

## 🔗 Integração entre módulos (OBRIGATÓRIO)

### Princípio geral
Todos os módulos fazem parte de um sistema único e devem funcionar de forma integrada. Dados gerados em um módulo são consumidos pelos módulos seguintes — não há módulo isolado. A implementação de qualquer módulo deve respeitar contratos estabelecidos pelos módulos anteriores e antecipar os contratos exigidos pelos módulos seguintes.

### Fluxo de dados entre módulos

```
M1 Cadastro → M2 Anamnese → M3 Plano de tratamento
                                      ↓
M6 Alta ← M5 Evolução ← M4 Agendamento (sessao_id)
```

| De | Para | Dado compartilhado |
|----|------|--------------------|
| M1 | M2, M3, M4 | `paciente.id` (UUID) — chave estrangeira em todas as entidades clínicas |
| M2 | M3 | Diagnóstico clínico e dados da avaliação informam o plano |
| M3 | M4 | `plano_tratamento.id`, frequência e nº de sessões orientam o agendamento |
| M4 | M5 | `sessao.id` — cada evolução pertence a exatamente uma sessão |
| M5 | M6 | Histórico de evoluções sustenta o relatório e critérios da alta |

### Regras obrigatórias de concordância

1. **Antes de implementar qualquer módulo**, verificar se os modelos, DTOs, endpoints e convenções de nomeação são compatíveis com os módulos já existentes.
2. **Campos compartilhados** (ex.: `paciente_id`, `sessao_id`, status de sessão) devem usar exatamente os mesmos tipos, nomes e formatos definidos no módulo de origem.
3. **Enums e listas de opções** (ex.: status de sessão, sexo, tipo de sessão) devem ser centralizados e reutilizados — nunca redefinidos localmente em cada módulo.
4. **Endpoints** que cruzam módulos (ex.: buscar sessões de um paciente) devem seguir a convenção REST já definida na seção "Endpoints REST (padrão)".

### Procedimento obrigatório ao detectar discordância

Sempre que for identificada uma discordância entre módulos (dados incompatíveis, convenções divergentes, contratos quebrados), **a alteração NÃO deve ser feita silenciosamente**. O procedimento é:

1. **Sinalizar a discordância antes de qualquer código** — descrever:
   - O que está em conflito (ex.: "O campo `tipo_sessao` no M4 usa string livre, mas o M5 espera um enum")
   - Quais módulos são afetados
   - O impacto se não for resolvido
2. **Propor a resolução** — apresentar a opção de harmonização recomendada
3. **Aguardar confirmação** antes de implementar a correção

**Formato obrigatório de reporte de discordância:**

```
⚠️ DISCORDÂNCIA DETECTADA
Módulos afetados: [M# e M#]
Problema: [descrição clara do conflito]
Impacto: [o que quebra ou fica inconsistente]
Resolução proposta: [abordagem recomendada]
Aguardando confirmação para prosseguir.
```

---

## Estrutura de pastas

```
fisioclinic/
│
├── backend/                          # Projeto Spring Boot
│   └── src/main/java/com/fisioclinic/
│       ├── controller/               # REST Controllers (@RestController)
│       │   ├── PacienteController.java
│       │   ├── SessaoController.java
│       │   ├── EvolucaoController.java
│       │   └── AltaController.java
│       ├── service/                  # Regras de negócio (@Service)
│       │   ├── PacienteService.java
│       │   ├── SessaoService.java
│       │   └── ...
│       ├── repository/               # Interfaces JPA (@Repository)
│       │   ├── PacienteRepository.java
│       │   └── ...
│       ├── model/                    # Entidades JPA (@Entity)
│       │   ├── Paciente.java
│       │   ├── Sessao.java
│       │   ├── Evolucao.java
│       │   └── ...
│       ├── dto/                      # Objetos de transferência (request/response)
│       │   ├── PacienteDTO.java
│       │   └── ...
│       ├── config/                   # Configurações (Security, CORS, etc.)
│       └── FisioclinicApplication.java
│
├── frontend/                         # HTML/CSS/JS puro
│   ├── index.html                    # Página raiz / login
│   ├── pages/
│   │   ├── agenda.html               # Módulo 4
│   │   ├── pacientes.html            # Módulo 1
│   │   ├── prontuario.html           # Módulos 2, 3, 5
│   │   └── alta.html                 # Módulo 6
│   ├── css/
│   │   ├── base.css                  # Reset + variáveis CSS
│   │   ├── components.css            # Botões, inputs, cards, modais
│   │   └── pages/                   # CSS específico por página
│   └── js/
│       ├── api/                      # Fetch wrappers por entidade
│       │   ├── pacientes.js
│       │   ├── sessoes.js
│       │   └── ...
│       ├── components/               # Componentes JS reutilizáveis
│       │   ├── modal.js
│       │   ├── calendar.js
│       │   └── toast.js
│       └── pages/                   # Lógica específica por página
│           ├── agenda.js
│           └── ...
│
├── database/
│   └── schema.sql                    # Schema PostgreSQL completo
│
├── docker-compose.yml                # Postgres + pgAdmin local
├── CLAUDE.md                         # Este arquivo
└── --atualizações/
    └── log.md                        # Registro diário de alterações
```

---

## Convenções de código

### JavaScript (frontend)
- **Arquivos**: kebab-case (`paciente-form.js`, `agenda-calendar.js`)
- **Funções**: camelCase (`fetchPacientes`, `renderCalendar`)
- **Constantes globais**: UPPER_SNAKE_CASE (`API_BASE_URL`)
- **Chamadas à API**: sempre em `js/api/`, nunca inline no HTML
- **DOM**: selecionar elementos via `id` ou `data-*`; nunca classes CSS
- **Datas**: sempre formatar em `pt-BR` (`toLocaleDateString('pt-BR')`)
- **IDs**: sempre UUID, nunca auto-increment exposto no frontend
- **Sem jQuery** — fetch API nativo

### Java (backend)
- **Pacote base**: `com.fisioclinic`
- **Controllers**: sufixo `Controller`, retornam `ResponseEntity<>`
- **Services**: sufixo `Service`, contêm toda a regra de negócio
- **DTOs**: sufixo `DTO` para request, sufixo `Response` para response
- **Entidades**: sem sufixo, mapeadas com anotações JPA
- **IDs**: `UUID` em todas as entidades (`@GeneratedValue` com `UUID`)
- **Datas**: sempre `LocalDateTime` com timezone — nunca `Date` legado
- **Validação**: Bean Validation (`@NotBlank`, `@NotNull`, `@Size`) nos DTOs
- **Exceções**: handler global com `@ControllerAdvice`
- **CORS**: configurado globalmente no `SecurityConfig` para `localhost`

### Geral
- Commits em português, descritivos
- Endpoints REST em português snake_case: `/api/pacientes`, `/api/sessoes`
- Respostas de erro padronizadas: `{ "erro": "mensagem", "status": 400 }`

---

## Endpoints REST (padrão)

| Método | Endpoint                        | Descrição                        |
|--------|---------------------------------|----------------------------------|
| GET    | `/api/pacientes`                | Listar pacientes                 |
| POST   | `/api/pacientes`                | Criar paciente                   |
| GET    | `/api/pacientes/{id}`           | Buscar paciente por ID           |
| PATCH  | `/api/pacientes/{id}`           | Atualizar paciente               |
| GET    | `/api/sessoes`                  | Listar sessões (com filtros)     |
| POST   | `/api/sessoes`                  | Criar sessão                     |
| PATCH  | `/api/sessoes/{id}/status`      | Atualizar status da sessão       |
| GET    | `/api/sessoes/semana`           | Sessões da semana atual          |
| POST   | `/api/evolucoes`                | Registrar evolução SOAP          |
| GET    | `/api/evolucoes/sessao/{id}`    | Evolução de uma sessão           |
| POST   | `/api/altas`                    | Registrar alta                   |

---

## Paleta visual (tema neutro profissional)

```css
--bg:       #F0F0F0;   /* fundo geral — cinza neutro claro */
--surface:  #FFFFFF;   /* cards, modais — branco */
--surface2: #E8E8E8;   /* fundo secundário — cinza um tom abaixo */
--border:   #DCDCDC;   /* bordas */
--text:     #1A1A1A;   /* texto principal */
--text2:    #666666;   /* texto secundário */
--text3:    #9E9E9E;   /* texto terciário / placeholders */
--accent:   #2D5BE3;   /* azul primário */
--green:    #1A7F4B;   /* status realizado */
--amber:    #9B6A00;   /* status aguardando */
--red:      #C0392B;   /* status cancelado */
```

Fonte principal: **DM Sans** (Google Fonts). Fonte mono: **DM Mono**.

---

## Entidades principais (resumo do ERD)

| Entidade            | Descrição                                                        |
|---------------------|------------------------------------------------------------------|
| `pacientes`         | Dados pessoais, contato, convênio, contato de emergência         |
| `contatos_emergencia` | Contato de emergência do paciente                              |
| `convenios_paciente`| Convênios e planos vinculados ao paciente                        |
| `fisioterapeutas`   | Profissionais da clínica                                         |
| `salas`             | Salas e boxes de atendimento                                     |
| `anamneses`         | Queixa, histórico de saúde, avaliação física (JSONB)             |
| `arquivos_anamnese` | Exames, laudos e encaminhamentos                                 |
| `planos_tratamento` | CIF, objetivos, técnicas, frequência, nº sessões                 |
| `termos_consentimento` | TCLE e termos assinados                                       |
| `sessoes`           | Agendamento: data/hora, fisioterapeuta, sala, status             |
| `evolucoes`         | SOAP completo, técnicas, aparelhos, EVA, código TUSS             |
| `fotos_evolucao`    | Fotos comparativas da evolução                                   |
| `altas`             | Motivo, resultado vs objetivos, relatórios, pesquisa satisfação  |

---

## Regras de negócio importantes

- Prontuário eletrônico segue a **Resolução CFM 1.821/07**:
  imutabilidade após assinatura, registro de autoria e data
- Dados de saúde são **sensíveis pela LGPD art. 11** — nunca logar dados clínicos
- Backups com **retenção mínima de 20 anos** (exigência CFM para prontuários)
- Status de sessão: `agendado | confirmado | realizado | faltou | cancelado`
- Não pode haver **dois agendamentos na mesma sala no mesmo horário**
- Evolução clínica: **apenas uma por sessão**

---

## ✅ Validação funcional do backend (OBRIGATÓRIO)

### Princípio geral
Criar código backend não é suficiente — é obrigatório verificar que o código criado está realmente funcional antes de considerar qualquer tarefa concluída. Todo backend criado ou alterado deve passar pelas etapas de validação abaixo, nesta ordem.

### Etapas obrigatórias de validação

#### 1. Compilação sem erros
```bash
cd backend
mvn compile -q
```
- Se falhar: corrigir os erros de compilação antes de prosseguir. Não entregar código que não compila.

#### 2. Inicialização da aplicação
```bash
mvn spring-boot:run
```
- Aguardar a mensagem `Started FisioclinicApplication` no log.
- Se falhar: investigar e corrigir antes de prosseguir (erros comuns: schema incompatível, propriedades ausentes, porta em uso).

#### 3. Teste dos endpoints criados/alterados
Para cada endpoint novo ou modificado, executar uma chamada real e registrar a resposta:

```bash
# Exemplo — listar pacientes
curl -s -X GET http://localhost:8080/api/pacientes \
  -H "Authorization: Bearer <token>" | jq .

# Exemplo — criar sessão
curl -s -X POST http://localhost:8080/api/sessoes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{ "pacienteId": "...", "data": "2026-06-10T09:00:00" }'
```

- Verificar: código HTTP correto (200, 201, 400, 409), corpo da resposta válido, dados persistidos no banco quando aplicável.
- Se retornar erro inesperado: investigar e corrigir antes de concluir.

#### 4. Reporte obrigatório dos resultados
Ao finalizar a implementação de qualquer backend, incluir na resposta:

```
✅ VALIDAÇÃO DO BACKEND
Compilação: OK / FALHOU
Inicialização: OK / FALHOU
Endpoints testados:
  - [MÉTODO] /api/endpoint → HTTP [status] ✅ / ❌ ([descrição do problema se ❌])
  - ...
Banco de dados: dados persistidos corretamente? Sim / Não
```

Se qualquer item estiver ❌, **a tarefa não está concluída** — corrigir e repetir a validação.

### Quando aplicar
- Ao criar qualquer Controller, Service, Repository ou Model novo
- Ao alterar a assinatura de um endpoint existente
- Ao adicionar ou remover campos de um DTO
- Ao alterar queries JPA ou configurações de mapeamento
- Ao modificar regras de segurança (`SecurityConfig`)

### Quando NÃO é necessário
- Alterações apenas de documentação ou `log.md`
- Alterações exclusivamente no frontend (sem toque no backend)
- Refatorações internas sem mudança de comportamento externo (desde que `mvn compile` passe)

---

## Credenciais de acesso (ambiente local)

### PostgreSQL
| Campo    | Valor        |
|----------|--------------|
| Host     | localhost    |
| Porta    | **5411**     |
| Banco    | fisioclinic  |
| Usuário  | postgres     |
| Senha    | PnCdEL       |

Conexão psql:
```bash
$env:PGPASSWORD = "PnCdEL"
& "C:\Program Files\PostgreSQL\11\bin\psql.exe" -U postgres -p 5411 -d fisioclinic
```

### Login da aplicação (admin padrão)
| Campo | Valor                    |
|-------|--------------------------|
| Email | admin@fisioclinic.com    |
| Senha | admin123                 |
| Perfil | ADMIN                   |

---

## Variáveis de ambiente (application.properties)

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5411/fisioclinic
spring.datasource.username=fisio
spring.datasource.password=fisio123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Servidor
server.port=8080

# JWT
jwt.secret=troque-esta-chave-em-producao
jwt.expiration=28800000

# CORS (frontend local)
cors.allowed-origins=http://localhost:3000,http://127.0.0.1:5500
```

---

## Campos por módulo (referência rápida)

### Módulo 1 — Cadastro do paciente
**Obrigatórios:** Nome completo, CPF, Data de nascimento, Sexo, Telefone/WhatsApp, Convênio ou particular
**Opcionais:** Estado civil, Profissão, Foto, E-mail, Endereço, Nº carteirinha, Validade plano, Responsável financeiro, Contato de emergência (nome, parentesco, telefone)
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos

### Módulo 2 — Anamnese e avaliação inicial
**Obrigatórios:** Queixa principal, Histórico da doença atual
**Opcionais:** Tempo de início dos sintomas, Doenças preexistentes, Cirurgias anteriores, Medicamentos em uso, Alergias, Histórico familiar, Postura, ADM, Força muscular, Dor EVA 0–10, Testes especiais, Goniometria, Exames/laudos/encaminhamentos
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos

### Módulo 3 — Diagnóstico e plano de tratamento
**Obrigatórios:** Diagnóstico fisioterapêutico (CIF), Objetivos de curto prazo, Objetivos de longo prazo, Técnicas e recursos, Frequência semanal, Nº sessões estimado, TCLE + Assinatura
**Opcionais:** CID-10, Hipóteses de tratamento
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos

### Módulo 4 — Agendamento
**Obrigatórios:** Paciente, Data e hora, Fisioterapeuta, Sala/box, Tipo de sessão, Status
**Opcionais:** Convênio para faturar, Motivo cancelamento, Reagendamento automático, Lembretes WhatsApp/SMS/e-mail
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos

### Módulo 5 — Evolução clínica (SOAP)
**Obrigatórios:** Data/hora, Nº sessão, Fisioterapeuta, S/O/A/P, Técnicas realizadas, Aparelhos + parâmetros, Tempo de atendimento
**Opcionais:** Código TUSS/CBHPM, EVA antes/depois, Fotos comparativas, Reavaliação a cada N sessões
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos
 especifico para não ser possivel digitar informacoes incompativeis nos campos
### Módulo 6 — Alta e relatórios
**Obrigatórios:** Data da alta, Motivo da alta, Resultado vs objetivos
**Opcionais:** Orientações domiciliares, Relatório de evolução, Relatório para médico, Estatísticas do tratamento, Agendamento de retorno, Pesquisa de satisfação
**RegraObrigatório**: sempre que tiver campos que sejam possíveis marcar checkbox ou escolher uma opção, criar internamente padrão especifico para não ser possivel digitar informacoes incompativeis nos campos


---

## 🔀 Versionamento Git (OBRIGATÓRIO)

### Inicialização do repositório
No início de cada sessão, verificar se o diretório `.git` existe na raiz do projeto.
- **Se não existir:** executar `git init`, criar/verificar `.gitignore`, e fazer o commit inicial com todos os arquivos existentes: `git commit -m "chore: commit inicial do projeto"`
- **Se já existir:** prosseguir normalmente

### Regras obrigatórias de commit

| Momento | Ação obrigatória |
|---|---|
| **Antes de iniciar qualquer alteração de código** | Commit de snapshot: `snapshot: estado antes das alterações de DD/MM/AAAA` |
| **Após concluir as alterações da sessão** | Commit descritivo com tipo e resumo do que foi feito |
| **Ao atualizar o `log.md`** | Commit separado: `docs: atualiza log.md DD/MM/AAAA` |
| **Início de sessão sem `.git`** | `git init` + commit inicial com todos os arquivos |

O commit de snapshot garante que **sempre haverá uma versão anterior recuperável** antes de qualquer mudança.

### Convenção de commits (em português)

```
tipo: descrição objetiva do que foi feito
```

| Tipo | Quando usar |
|------|-------------|
| `snapshot` | Commit automático antes de iniciar alterações — preserva versão anterior |
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `refactor` | Refatoração sem mudança de comportamento |
| `style` | Ajustes visuais (CSS, formatação) |
| `docs` | Atualização de documentação ou `log.md` |
| `config` | Alterações em configurações |
| `chore` | Tarefas de manutenção (dependências, build, etc.) |

**Exemplos:**
```
snapshot: estado antes das alterações de 05/06/2026
feat: adiciona cadastro de pacientes com validação de CPF
fix: corrige conflito de horário no agendamento
refactor: extrai lógica de autenticação para AuthService
style: ajusta paleta de cores da tela de login
docs: atualiza log.md 05/06/2026
```

### O que NUNCA commitar
- Arquivos `.env` ou qualquer arquivo com credenciais/senhas/chaves
- Diretório `target/` e binários compilados (`.class`, `.jar`)
- Pastas de IDE (`.idea/`, `.vscode/`)
- Arquivos de sistema (`Thumbs.db`, `.DS_Store`)

Esses itens devem estar no `.gitignore` na raiz do projeto.

---

## 📋 Registro de Alterações (OBRIGATÓRIO)

### Regra principal
Existe uma pasta chamada `--atualizações/` na raiz do projeto.
Dentro dela há o arquivo `log.md`, que deve ser mantido atualizado com **todas as alterações realizadas**.

### Como atualizar o log

- **Frequência:** 1 vez por dia, **sempre ao final do dia ou da sessão de trabalho**.
- **Com alterações:** registrar cada mudança feita, separando por `Backend` e `Frontend`, com data, horário e descrição objetiva.
- **Sem alterações:** registrar a data e informar explicitamente que não houve alterações.

### Formato obrigatório do log

```
---

## 📅 DD/MM/AAAA — Dia da semana

### ⏰ HH:MM — [Backend | Frontend | Geral]
- Descrição objetiva da alteração realizada

### ⏰ HH:MM — [Backend | Frontend | Geral]
- Descrição objetiva da alteração realizada

```

### Quando não houver alterações

```
---

## 📅 DD/MM/AAAA — Dia da semana

Não houve alterações.

```

### Ordem
As entradas devem ser ordenadas do **mais recente para o mais antigo** (última alteração no topo).

---

## 📁 Estrutura da pasta de atualizações

```
--atualizações/
└── log.md      ← arquivo único de registro de todas as alterações
```

---

## ✅ Resumo das obrigações do Claude

| Situação | Ação |
|---|---|
| Início de sessão — `.git` inexistente | `git init` + commit inicial com todos os arquivos do projeto |
| Início de sessão — `.git` existente | Verificar se há registro do dia anterior pendente no `log.md` |
| **Antes de qualquer alteração de código** | **Commit de snapshot: `snapshot: estado antes das alterações de DD/MM/AAAA`** |
| Realizou alterações no backend ou frontend | Registrar no `log.md` com data, horário e descrição |
| Realizou alterações no backend ou frontend | Commit descritivo com tipo adequado após concluir |
| Não realizou nenhuma alteração no dia | Registrar a data com "Não houve alterações" |
| Encerramento da sessão | Atualizar o `log.md` e criar commit `docs: atualiza log.md DD/MM/AAAA` |
| Criou ou alterou código backend | Executar as 4 etapas de validação e reportar com o formato `✅ VALIDAÇÃO DO BACKEND` |




