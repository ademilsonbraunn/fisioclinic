# 📋 Melhorias do Sistema FisioClinic

Registro de todas as sugestões de melhoria identificadas na revisão do sistema (v1.4).
Cada item contém: descrição, impacto clínico, esforço estimado e status de implementação.

---

## Status possíveis
- ✅ **Implementado** — disponível no sistema
- 🔨 **Em andamento** — sendo implementado
- 🔲 **Pendente** — não iniciado
- ❌ **Descartado** — fora do escopo

---

## 🔴 Prioridade 1 — Módulos em Andamento (base do sistema clínico)

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 1 | **Concluir M2 — Anamnese** | Validação de campos obrigatórios ✅ · Backend upload multipart ✅ · Frontend de upload/listagem/remoção de arquivos no card da anamnese ✅ | Baixo | ✅ Implementado |
| 2 | **Concluir M3 — Plano de Tratamento** | Integração `anamneseId` com M2 ✅ · Backend TCLE completo ✅ · `plano_id` em Sessao ✅ · Frontend: TCLE pós-salvar + badge no cartão ✅ · Frontend: dropdown de plano na agenda ✅ | Baixo | ✅ Implementado |

---

## 🟠 Prioridade 2 — Funcionalidades Clínicas de Alto Valor

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 3 | **Gráfico de Evolução do EVA** | Linha do tempo visual mostrando como a dor do paciente evoluiu ao longo das sessões. Endpoint `GET /api/evolucoes/paciente/{id}` já existe — só precisa renderizar gráfico com Chart.js | Baixo | 🔲 Pendente |
| 4 | **Reavaliação Comparativa** | Ao criar nova Anamnese, exibir lado a lado os valores da anterior (ADM, força muscular, EVA, postura) para o fisioterapeuta comparar a evolução dos parâmetros físicos | Médio | 🔲 Pendente |
| 5 | **Relatório PDF da Alta** | Botão "Imprimir Relatório" na tela de alta gerando PDF com: dados do paciente, diagnóstico, resumo do tratamento, EVA inicial/final, orientações domiciliares. Pode ser feito com `@media print` + CSS, sem backend | Médio | 🔲 Pendente |
| 6 | **Histórico de Alterações do Prontuário (Auditoria)** | Trilha de auditoria imutável: quem editou o quê e quando em qualquer campo do prontuário. Exigência da Resolução CFM 1.821/07 para prontuário eletrônico | Alto | 🔲 Pendente |
| 7 | **Fotos Comparativas na Evolução** | Upload de fotos antes/depois em cada evolução (postura, inchaço, cicatriz). Tabela `fotos_evolucao` já existe no schema. Endpoint `POST /api/evolucoes/{id}/fotos` | Médio-Alto | 🔲 Pendente |

---

## 🟡 Prioridade 3 — Melhorias de UX e Performance

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 8 | **Paginação na Listagem de Pacientes** | Carregar pacientes em páginas de 20–50, em vez de todos de uma vez. Backend já suporta `Pageable` no Spring Data. Adicionar `?page=0&size=20` e controle visual anterior/próximo | Baixo | 🔲 Pendente |
| 9 | **Busca Avançada de Pacientes** | Filtrar por convênio específico, faixa etária, data de cadastro, status clínico (em tratamento / com alta / sem plano). Chips de filtro no frontend + query params no backend | Médio | 🔲 Pendente |
| 10 | **Exportação CSV** | Botão "Exportar" na lista de pacientes e na agenda que baixa um CSV com os dados visíveis. Feito 100% em JavaScript (Blob) — sem necessidade de backend | Baixíssimo | 🔲 Pendente |
| 11 | **Status do Paciente na Listagem** | Badge visível na tabela de pacientes indicando "Em Tratamento", "Alta Concluída" ou "Sem Plano". Backend retorna campo `status_clinico` calculado; frontend exibe badge colorido | Médio | 🔲 Pendente |
| 12 | **Validação de CPF Duplicado em Tempo Real** | Ao digitar o CPF no cadastro, verificar (debounce 500ms) se já existe outro paciente com aquele CPF. Feedback imediato "CPF já cadastrado — ver paciente" | Baixo | 🔲 Pendente |

---

## 🟡 Prioridade 4 — Agendamento Avançado

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 13 | **Visualização Mensal do Calendário** | Nova opção "Mês" no toggle da agenda (além de Semana e Lista), mostrando quantidade de sessões por dia. Clique no dia abre lista do dia | Médio | 🔲 Pendente |
| 14 | **Bloqueio de Horários / Ausências** | Fisioterapeuta registra períodos de ausência (férias, feriados, cursos) que bloqueiam agendamentos. Tabela `bloqueios_agenda`, validação ao criar sessão | Médio | 🔲 Pendente |
| 15 | **Sessões Recorrentes** | Ao criar sessão, opção "Repetir por X semanas" que gera automaticamente todas as sessões do plano no mesmo horário, validando conflitos em cada uma | Médio-Alto | 🔲 Pendente |
| 16 | **Confirmação por WhatsApp/SMS** | Botão "Enviar lembrete" na agenda que dispara mensagem para o paciente. Integração com WhatsApp Business API (Meta) ou Twilio. Backend: `POST /api/sessoes/{id}/lembrete` | Alto | 🔲 Pendente |

---

## 🟢 Prioridade 5 — Relatórios e Analytics

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 17 | **Dashboard Clínico (Relatórios Gerenciais)** | Página `relatorios.html` com gráficos: atendimentos por mês, taxa de faltas, pacientes por convênio, diagnósticos mais comuns, fisioterapeuta mais ocupado. Chart.js + endpoints de agregação | Alto | 🔲 Pendente |
| 18 | **Relatório de Produtividade por Fisioterapeuta** | Sessões realizadas por profissional no período, taxa de alta, nota média de satisfação dos pacientes. Nova seção no painel admin | Médio | 🔲 Pendente |
| 19 | **Controle de Sessões por Convênio (Autorização)** | Registrar quantas sessões o convênio autorizou e quantas já foram realizadas, com alerta quando estiver próximo do limite. Campos `sessoes_autorizadas` e `codigo_autorizacao` na tabela `convenios_paciente` | Médio | 🔲 Pendente |

---

## 🔵 Prioridade 6 — Faturamento (Módulo Futuro)

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 20 | **Faturamento Básico (M7)** | Registrar valor de cada sessão (particular ou convênio), status de pagamento (pendente/pago/aguardando convênio), relatório mensal de receita por paciente/convênio/fisioterapeuta | Muito Alto | 🔲 Pendente |

---

## ⚙️ Melhorias Técnicas (Qualidade de Código)

| # | Melhoria | Descrição | Esforço | Status |
|---|----------|-----------|---------|--------|
| 21 | **Eliminar Duplicação do Formulário de Alta** | O formulário de Alta está replicado em `alta.html` e na aba Alta de `prontuario.html`. Qualquer mudança precisa ser feita em dois lugares. Extrair para componente JS reutilizável | Médio | 🔲 Pendente |
| 22 | **Paginação Global no Backend** | Alguns endpoints retornam todos os registros sem limite. Adicionar `Pageable` Spring em todos os endpoints de listagem que ainda não têm | Baixo | 🔲 Pendente |
| 23 | **Tratamento de Erros Uniforme** | Alguns erros de API são silenciosos (catch sem toast). Criar wrapper global de fetch com tratamento padronizado para que o usuário sempre saiba quando uma operação falhou | Baixo | 🔲 Pendente |
| 24 | **Acessibilidade (WCAG 2.1)** | Botões com apenas ícone não têm `aria-label`. Formulários não têm `aria-required`. Adicionar atributos ARIA nas interações sem texto visível | Baixo | 🔲 Pendente |

---

## 📊 Resumo Executivo

| Prioridade | Itens | Implementados | Pendentes |
|------------|-------|---------------|-----------|
| 🔴 P1 — Módulos base | 2 | 2 | 0 |
| 🟠 P2 — Clínico alto valor | 5 | 0 | 5 |
| 🟡 P3 — UX e Performance | 5 | 0 | 5 |
| 🟡 P4 — Agendamento | 4 | 0 | 4 |
| 🟢 P5 — Relatórios | 3 | 0 | 3 |
| 🔵 P6 — Faturamento | 1 | 0 | 1 |
| ⚙️ Técnicas | 4 | 0 | 4 |
| **Total** | **24** | **2** | **22** |

---

*Revisão realizada em: 11/06/2026 — versão v1.4 do sistema*
