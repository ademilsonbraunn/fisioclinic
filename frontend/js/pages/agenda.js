// ─────────────────────────────────────────────────────────────────────────────
// pages/agenda.js — Lógica completa do calendário semanal (Módulo 4)
// ─────────────────────────────────────────────────────────────────────────────
// Ponto de entrada: DOMContentLoaded → carrega dados + monta calendário
//
// Arquitetura do calendário:
//  - Grade de 7 colunas (seg a dom) × 26 slots de 30min (07:00–20:00)
//  - SLOT_HEIGHT = 48px/slot — eventos são posicionados com top e height
//    calculados em px com base no horário (inicio - CAL_START) / 30 * SLOT_HEIGHT
//  - Eventos são renderizados absolutamente dentro da coluna do dia correto
//
// Seções principais:
//  Constantes    — SLOT_HEIGHT, CAL_START/END, DIAS_ABV, STATUS_LABEL, TIPO_LABEL
//  MOCK data     — fisioterapeutas, salas, pacientes e sessões de exemplo
//
//  Estado (state {}) — semana exibida, sessões carregadas, filtros ativos,
//                      dados de fisios/salas/pacientes para os selects
//
//  carregarDadosSemana() — GET /api/sessoes/semana (fallback: MOCK)
//  carregarFisios()      — GET /api/fisioterapeutas para popular selects
//  carregarSalas()       — GET /api/salas/ativas
//  carregarPacientes()   — GET /api/pacientes
//
//  renderCalendario()    — monta o grid completo da semana com eventos
//  renderEvento(s)       — posiciona e estiliza cada sessão como bloco clicável
//
//  Modal de agendamento — cria/edita sessão com todos os campos obrigatórios
//  Painel de detalhes   — exibe dados da sessão clicada + botões de ação de status
//
//  Navegação de semana  — botões ← → recalculam getLunsDaSemana() e recarregam
// ─────────────────────────────────────────────────────────────────────────────

import { listarSessoesSemana, listarSessoes, criarSessao, atualizarSessao, atualizarStatusSessao } from '../api/sessoes.js';
import { showToast } from '../components/toast.js';
import { openModal, closeModal } from '../components/modal.js';
import { initTopbar } from '../utils/auth.js';

// ── Constantes ──────────────────────────────────────────────────────────────

const SLOT_HEIGHT = 48;       // px por slot de 30min
const CAL_START   = 7 * 60;   // 07:00 em minutos
const CAL_END     = 20 * 60;  // 20:00 em minutos
const SLOTS       = (CAL_END - CAL_START) / 30; // 26 slots

const DIAS_ABV  = ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB', 'DOM'];
const MESES_PT  = ['janeiro','fevereiro','março','abril','maio','junho',
                   'julho','agosto','setembro','outubro','novembro','dezembro'];

const STATUS_LABEL = {
  AGENDADO:   'Agendado',
  CONFIRMADO: 'Confirmado',
  REALIZADO:  'Realizado',
  FALTOU:     'Faltou',
  CANCELADO:  'Cancelado',
};

const TIPO_LABEL = {
  AVALIACAO:   'Avaliação',
  SESSAO:      'Sessão',
  REAVALIACAO: 'Reavaliação',
  ALTA:        'Alta',
};

// ── Mock data ───────────────────────────────────────────────────────────────

const MOCK_FISIOTERAPEUTAS = [
  { id: 'fisio-001', nome: 'Carlos Eduardo Oliveira', crf: 'CREFITO-3/12345-F' },
  { id: 'fisio-002', nome: 'Fernanda Lima',            crf: 'CREFITO-3/67890-F' },
  { id: 'fisio-003', nome: 'Paulo Ricardo Andrade',    crf: 'CREFITO-3/54321-F' },
];

const MOCK_SALAS = [
  { id: 'sala-001', nome: 'Box 1',         tipo: 'BOX',             ativo: true },
  { id: 'sala-002', nome: 'Box 2',         tipo: 'BOX',             ativo: true },
  { id: 'sala-003', nome: 'Sala Individual',tipo: 'SALA_INDIVIDUAL', ativo: true },
  { id: 'sala-004', nome: 'Sala em Grupo', tipo: 'SALA_GRUPO',      ativo: true },
];

const MOCK_PACIENTES = [
  { id: 'pac-001', nome_completo: 'Maria das Graças Silva', cpf: '123.456.789-00' },
  { id: 'pac-002', nome_completo: 'João Pedro Santos',      cpf: '234.567.890-11' },
  { id: 'pac-003', nome_completo: 'Ana Paula Costa',        cpf: '345.678.901-22' },
  { id: 'pac-004', nome_completo: 'Roberto Ferreira',       cpf: '456.789.012-33' },
  { id: 'pac-005', nome_completo: 'Carla Mendes',           cpf: '567.890.123-44' },
  { id: 'pac-006', nome_completo: 'Luiza Almeida',          cpf: '678.901.234-55' },
];

function mockSessoes() {
  const luns = getLunsDaSemana(new Date());
  const d = (i) => dataISO(addDias(luns, i));
  return [
    { id:'sess-001', paciente:MOCK_PACIENTES[0], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[0],
      data_hora_inicio:`${d(0)}T08:00:00`, data_hora_fim:`${d(0)}T09:00:00`,
      tipo_sessao:'SESSAO', status:'REALIZADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-002', paciente:MOCK_PACIENTES[1], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[1],
      data_hora_inicio:`${d(1)}T10:00:00`, data_hora_fim:`${d(1)}T11:00:00`,
      tipo_sessao:'AVALIACAO', status:'REALIZADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-003', paciente:MOCK_PACIENTES[2], fisioterapeuta:MOCK_FISIOTERAPEUTAS[1], sala:MOCK_SALAS[0],
      data_hora_inicio:`${d(1)}T14:00:00`, data_hora_fim:`${d(1)}T15:00:00`,
      tipo_sessao:'SESSAO', status:'REALIZADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-004', paciente:MOCK_PACIENTES[0], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[0],
      data_hora_inicio:`${d(2)}T08:00:00`, data_hora_fim:`${d(2)}T09:00:00`,
      tipo_sessao:'SESSAO', status:'REALIZADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-005', paciente:MOCK_PACIENTES[3], fisioterapeuta:MOCK_FISIOTERAPEUTAS[1], sala:MOCK_SALAS[2],
      data_hora_inicio:`${d(3)}T09:00:00`, data_hora_fim:`${d(3)}T10:00:00`,
      tipo_sessao:'REAVALIACAO', status:'CONFIRMADO', observacoes:'Reavaliação após 10 sessões', motivo_cancelamento:null },
    { id:'sess-006', paciente:MOCK_PACIENTES[1], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[1],
      data_hora_inicio:`${d(3)}T11:00:00`, data_hora_fim:`${d(3)}T12:00:00`,
      tipo_sessao:'SESSAO', status:'AGENDADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-007', paciente:MOCK_PACIENTES[4], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[0],
      data_hora_inicio:`${d(4)}T08:00:00`, data_hora_fim:`${d(4)}T09:00:00`,
      tipo_sessao:'SESSAO', status:'AGENDADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-008', paciente:MOCK_PACIENTES[2], fisioterapeuta:MOCK_FISIOTERAPEUTAS[1], sala:MOCK_SALAS[1],
      data_hora_inicio:`${d(4)}T14:00:00`, data_hora_fim:`${d(4)}T15:30:00`,
      tipo_sessao:'SESSAO', status:'FALTOU', observacoes:null, motivo_cancelamento:null },
    { id:'sess-009', paciente:MOCK_PACIENTES[3], fisioterapeuta:MOCK_FISIOTERAPEUTAS[0], sala:MOCK_SALAS[2],
      data_hora_inicio:`${d(5)}T10:00:00`, data_hora_fim:`${d(5)}T11:00:00`,
      tipo_sessao:'SESSAO', status:'AGENDADO', observacoes:null, motivo_cancelamento:null },
    { id:'sess-010', paciente:MOCK_PACIENTES[5], fisioterapeuta:MOCK_FISIOTERAPEUTAS[1], sala:MOCK_SALAS[0],
      data_hora_inicio:`${d(5)}T14:00:00`, data_hora_fim:`${d(5)}T15:00:00`,
      tipo_sessao:'AVALIACAO', status:'CANCELADO', observacoes:null, motivo_cancelamento:'Paciente remarcou' },
  ];
}

// ── Estado ──────────────────────────────────────────────────────────────────

let semanaBase        = getLunsDaSemana(new Date());
let sessoes           = [];
let fisioterapeutas   = [];
let salas             = [];
let pacientes         = [];
let filtroFisioId     = '';
let filtroStatus      = '';
let modoVista         = 'calendario';
let sessaoEditando    = null;
let salvando          = false;

// ── Helpers de data ─────────────────────────────────────────────────────────

function getLunsDaSemana(d) {
  const date = new Date(d);
  date.setHours(0, 0, 0, 0);
  const isoDay = (date.getDay() + 6) % 7; // seg=0 … dom=6
  date.setDate(date.getDate() - isoDay);
  return date;
}

function addDias(d, n) {
  const r = new Date(d);
  r.setDate(r.getDate() + n);
  return r;
}

function dataISO(d) {
  // "2026-06-05"
  return d.toISOString().slice(0, 10);
}

function formatDataCurta(d) {
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

function formatHora(d) {
  return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatDataHora(iso) {
  const d = new Date(iso);
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    + ' ' + formatHora(d);
}

function semanaLabel() {
  const fim = addDias(semanaBase, 6);
  const mesI = semanaBase.getMonth(), mesF = fim.getMonth();
  const ano  = fim.getFullYear();
  if (mesI === mesF) {
    return `${semanaBase.getDate()} – ${fim.getDate()} de ${MESES_PT[mesF]} ${ano}`;
  }
  return `${formatDataCurta(semanaBase)} – ${formatDataCurta(fim)}/${ano}`;
}

function minutosDeHorario(str) {
  // "09:30" → 570
  const [h, m] = str.split(':').map(Number);
  return h * 60 + m;
}

function horarioDeMinutos(mins) {
  const h = String(Math.floor(mins / 60)).padStart(2, '0');
  const m = String(mins % 60).padStart(2, '0');
  return `${h}:${m}`;
}

function calcTop(iso) {
  const d = new Date(iso);
  const mins = d.getHours() * 60 + d.getMinutes();
  return ((mins - CAL_START) / 30) * SLOT_HEIGHT;
}

function calcHeight(isoInicio, isoFim) {
  const dur = (new Date(isoFim) - new Date(isoInicio)) / 60000;
  return Math.max((dur / 30) * SLOT_HEIGHT, 24);
}

function colIndex(iso) {
  const d = new Date(iso);
  d.setHours(0, 0, 0, 0);
  return Math.round((d - semanaBase) / 86400000);
}

// ── Helpers de UI ───────────────────────────────────────────────────────────

function iniciais(nome) {
  return nome.trim().split(/\s+/).slice(0, 2).map(p => p[0].toUpperCase()).join('');
}

function esc(str) {
  return String(str ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatCpf(cpf) {
  if (!cpf) return '';
  const c = String(cpf).replace(/\D/g, '').padStart(11, '0');
  return c.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

function badgeStatus(status) {
  return `<span class="badge badge-${status.toLowerCase()}">${esc(STATUS_LABEL[status] ?? status)}</span>`;
}

function tipoSalaLabel(tipo) {
  const m = { BOX:'Box', SALA_INDIVIDUAL:'Sala Individual', SALA_GRUPO:'Sala em Grupo' };
  return m[tipo] ?? tipo;
}

// ── Radio chips ─────────────────────────────────────────────────────────────

function getChipValue(name) {
  const el = document.querySelector(`input[name="${name}"]:checked`);
  return el ? el.value : null;
}

function setChipValue(name, value) {
  const el = document.querySelector(`input[name="${name}"][value="${value}"]`);
  if (!el) return;
  el.checked = true;
  syncChip(el.closest('.chip'));
}

function syncChip(chip) {
  if (!chip) return;
  chip.classList.toggle('is-checked', chip.querySelector('input[type="radio"]').checked);
}

function bindChipsEm(container) {
  container.querySelectorAll('.chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const inp = chip.querySelector('input[type="radio"]');
      inp.checked = true;
      const name = inp.name;
      document.querySelectorAll(`input[name="${name}"]`).forEach(r => syncChip(r.closest('.chip')));
      if (name === 'status') onStatusChipChange();
    });
  });
}

function resetChipsEm(container) {
  container.querySelectorAll('.chip').forEach(chip => {
    chip.classList.remove('is-checked');
    chip.querySelector('input[type="radio"]').checked = false;
  });
}

// ── Carregar dados ──────────────────────────────────────────────────────────

async function carregarDados() {
  document.getElementById('cal-loading').hidden = false;

  // Fisioterapeutas
  try {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    const res = await fetch('http://localhost:8080/api/fisioterapeutas', {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (res.ok) fisioterapeutas = await res.json();
    else fisioterapeutas = MOCK_FISIOTERAPEUTAS;
  } catch { fisioterapeutas = MOCK_FISIOTERAPEUTAS; }

  // Salas
  try {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    const res = await fetch('http://localhost:8080/api/salas', {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (res.ok) salas = await res.json();
    else salas = MOCK_SALAS;
  } catch { salas = MOCK_SALAS; }

  // Pacientes
  try {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    const res = await fetch('http://localhost:8080/api/pacientes?page=0&size=200', {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (res.ok) {
      const data = await res.json();
      pacientes = data.content ?? data;
    } else pacientes = MOCK_PACIENTES;
  } catch { pacientes = MOCK_PACIENTES; }

  popularSelects();
  await carregarSessoes();
}

async function carregarSessoes() {
  document.getElementById('cal-loading').hidden = false;
  try {
    const inicio = dataISO(semanaBase);
    const fim    = dataISO(addDias(semanaBase, 6));
    const data   = await listarSessoes({ data_inicio: inicio, data_fim: fim });
    sessoes = Array.isArray(data) ? data : (data?.content ?? []);
  } catch {
    sessoes = mockSessoes();
  }
  document.getElementById('cal-loading').hidden = true;
  renderView();
}

// ── Selects do modal ────────────────────────────────────────────────────────

function popularSelects() {
  // Pacientes
  const selPaciente = document.getElementById('sel-paciente');
  selPaciente.innerHTML = '<option value="">Selecione o paciente</option>'
    + pacientes.map(p => `<option value="${esc(p.id)}">${esc(p.nomeCompleto ?? p.nome_completo)} — ${esc(formatCpf(p.cpf))}</option>`).join('');

  // Fisioterapeutas
  const selFisio = document.getElementById('sel-fisioterapeuta');
  selFisio.innerHTML = '<option value="">Selecione o fisioterapeuta</option>'
    + fisioterapeutas.map(f => `<option value="${esc(f.id)}">${esc(f.nome)}</option>`).join('');

  // Salas
  const selSala = document.getElementById('sel-sala');
  selSala.innerHTML = '<option value="">Selecione a sala / box</option>'
    + salas.filter(s => s.ativo !== false).map(s =>
        `<option value="${esc(s.id)}">${esc(s.nome)} (${esc(tipoSalaLabel(s.tipo))})</option>`
      ).join('');

  // Filtro fisioterapeuta
  const filtFisio = document.getElementById('filtro-fisio');
  filtFisio.innerHTML = '<option value="">Todos os fisioterapeutas</option>'
    + fisioterapeutas.map(f => `<option value="${esc(f.id)}">${esc(f.nome)}</option>`).join('');
}

// ── Navegação de semana ─────────────────────────────────────────────────────

function semanaAnterior() {
  semanaBase = addDias(semanaBase, -7);
  carregarSessoes();
}

function proximaSemana() {
  semanaBase = addDias(semanaBase, 7);
  carregarSessoes();
}

function irParaHoje() {
  semanaBase = getLunsDaSemana(new Date());
  carregarSessoes();
}

// ── Toggle de visualização ──────────────────────────────────────────────────

function setVista(vista) {
  modoVista = vista;
  document.getElementById('view-calendario').classList.toggle('active', vista === 'calendario');
  document.getElementById('view-lista').classList.toggle('active', vista === 'lista');
  document.getElementById('section-calendario').hidden = vista !== 'calendario';
  document.getElementById('section-lista').hidden      = vista !== 'lista';
  renderView();
}

// ── Renderização principal ──────────────────────────────────────────────────

function renderView() {
  document.getElementById('semana-label').textContent = semanaLabel();
  if (modoVista === 'calendario') renderCalendario();
  else renderLista();
}

function sessoesFiltradas() {
  return sessoes.filter(s => {
    if (filtroFisioId && s.fisioterapeuta?.id !== filtroFisioId) return false;
    if (filtroStatus  && s.status !== filtroStatus) return false;
    return true;
  });
}

// ── Calendário ──────────────────────────────────────────────────────────────

function renderCalendario() {
  const hoje = new Date(); hoje.setHours(0,0,0,0);

  // Atualiza cabeçalhos dos dias
  for (let i = 0; i < 7; i++) {
    const dia = addDias(semanaBase, i);
    const hdr = document.getElementById(`cal-hdr-${i}`);
    hdr.querySelector('.cal-weekday').textContent  = DIAS_ABV[i];
    hdr.querySelector('.cal-date-num').textContent = dia.getDate();
    hdr.classList.toggle('is-today', dia.getTime() === hoje.getTime());
    hdr.dataset.data = dataISO(dia);
  }

  // Remove eventos anteriores
  document.querySelectorAll('.cal-event').forEach(el => el.remove());

  const lista = sessoesFiltradas();

  // Agrupa por coluna para detectar sobreposições
  const porCol = Array.from({ length: 7 }, () => []);
  lista.forEach(s => {
    const idx = colIndex(s.data_hora_inicio);
    if (idx >= 0 && idx <= 6) porCol[idx].push(s);
  });

  for (let i = 0; i < 7; i++) {
    const col = document.getElementById(`cal-col-${i}`);
    const grupos = calcularGruposSobrepostos(porCol[i]);
    grupos.forEach(({ sessao, coluna, totalCols }) => {
      col.appendChild(criarCardEvento(sessao, coluna, totalCols));
    });
  }
}

function calcularGruposSobrepostos(lista) {
  if (!lista.length) return [];
  const result = [];
  const ordenada = [...lista].sort((a,b) =>
    new Date(a.data_hora_inicio) - new Date(b.data_hora_inicio));

  // Algoritmo simplificado: detecta overlaps e distribui colunas
  const ativos = [];
  ordenada.forEach(s => {
    const ini = new Date(s.data_hora_inicio).getTime();
    const fim = new Date(s.data_hora_fim).getTime();

    // Remove encerrados
    ativos.forEach((a,i) => { if (new Date(a.s.data_hora_fim).getTime() <= ini) ativos.splice(i,1); });

    const coluna = ativos.length;
    ativos.push({ s, coluna, fim });
    result.push({ sessao: s, coluna, totalCols: ativos.length });
  });

  // Re-calcula totalCols: para cada sessão, verifica quantas se sobrepõem
  const final = [];
  result.forEach(item => {
    const ini = new Date(item.sessao.data_hora_inicio).getTime();
    const fim = new Date(item.sessao.data_hora_fim).getTime();
    const sobrepostos = result.filter(r => {
      const ri = new Date(r.sessao.data_hora_inicio).getTime();
      const rf = new Date(r.sessao.data_hora_fim).getTime();
      return ri < fim && rf > ini;
    });
    final.push({ sessao: item.sessao, coluna: item.coluna, totalCols: sobrepostos.length });
  });
  return final;
}

function criarCardEvento(sessao, coluna, totalCols) {
  const top    = calcTop(sessao.data_hora_inicio);
  const height = calcHeight(sessao.data_hora_inicio, sessao.data_hora_fim);
  const status = sessao.status.toLowerCase();

  const pct   = 100 / totalCols;
  const left  = `calc(3px + ${coluna * pct}%)`;
  const width = `calc(${pct}% - 5px)`;

  const card = document.createElement('div');
  card.className = `cal-event status-${status}`;
  card.style.cssText = `top:${top}px;height:${height}px;left:${left};right:auto;width:${width}`;
  card.dataset.id = sessao.id;

  const horaI = formatHora(new Date(sessao.data_hora_inicio));
  const horaF = formatHora(new Date(sessao.data_hora_fim));
  const primeiroNome = sessao.paciente.nome_completo.split(' ').slice(0, 2).join(' ');

  card.innerHTML = `
    <span class="event-time">${esc(horaI)}–${esc(horaF)}</span>
    <span class="event-patient">${esc(primeiroNome)}</span>
    <span class="event-meta">${esc(sessao.sala.nome)} · ${esc(sessao.fisioterapeuta.nome.split(' ')[0])}</span>
  `;

  card.addEventListener('click', (e) => { e.stopPropagation(); abrirModalEditar(sessao); });
  return card;
}

// ── Lista ────────────────────────────────────────────────────────────────────

function renderLista() {
  const lista = sessoesFiltradas().sort(
    (a,b) => new Date(a.data_hora_inicio) - new Date(b.data_hora_inicio)
  );

  const tbody = document.getElementById('lista-tbody');
  if (!lista.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7">
          <div class="empty-state">
            <div class="icon">
              <svg width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                <rect x="3" y="4" width="18" height="18" rx="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <h3>Sem sessões nesta semana</h3>
            <p>Use os filtros ou navegue para outra semana.</p>
          </div>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = lista.map(s => {
    const dt = new Date(s.data_hora_inicio);
    const horaI = formatHora(dt);
    const horaF = formatHora(new Date(s.data_hora_fim));
    const dataStr = dt.toLocaleDateString('pt-BR', { weekday:'short', day:'2-digit', month:'2-digit' });
    return `
      <tr>
        <td class="text-mono">${esc(dataStr)} ${esc(horaI)}–${esc(horaF)}</td>
        <td>
          <div style="display:flex;align-items:center;gap:8px">
            <div class="avatar" style="width:28px;height:28px;font-size:.714rem">${esc(iniciais(s.paciente.nome_completo))}</div>
            ${esc(s.paciente.nome_completo)}
          </div>
        </td>
        <td>${esc(s.fisioterapeuta.nome)}</td>
        <td>${esc(s.sala.nome)}</td>
        <td><span class="badge badge-gray">${esc(TIPO_LABEL[s.tipo_sessao] ?? s.tipo_sessao)}</span></td>
        <td>${badgeStatus(s.status)}</td>
        <td>
          <div class="row-actions">
            <button class="btn btn-ghost btn-sm btn-icon" title="Editar" onclick="abrirModalEditarId('${esc(s.id)}')">
              <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.75" viewBox="0 0 24 24">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </button>
          </div>
        </td>
      </tr>`;
  }).join('');
}

// ── Modal nova / editar sessão ───────────────────────────────────────────────

function abrirModalNovo(data, hora) {
  sessaoEditando = null;
  document.getElementById('modal-titulo').textContent = 'Nova Sessão';
  document.getElementById('btn-excluir-sessao').hidden = true;
  limparModal();

  if (data) document.getElementById('inp-data').value = data;
  if (hora) {
    document.getElementById('inp-hora-inicio').value = hora;
    const minsFim = minutosDeHorario(hora) + 60;
    document.getElementById('inp-hora-fim').value = horarioDeMinutos(Math.min(minsFim, CAL_END));
  }
  setChipValue('tipo_sessao', 'SESSAO');
  setChipValue('status', 'AGENDADO');
  ocultarConflito();
  openModal('modal-sessao-backdrop');
}

function abrirModalEditar(sessao) {
  sessaoEditando = sessao;
  document.getElementById('modal-titulo').textContent = 'Editar Sessão';
  document.getElementById('btn-excluir-sessao').hidden = false;
  limparModal();

  // Preenche campos
  document.getElementById('sel-paciente').value = sessao.paciente.id;
  document.getElementById('sel-fisioterapeuta').value = sessao.fisioterapeuta.id;
  document.getElementById('sel-sala').value = sessao.sala.id;

  const dtI = new Date(sessao.data_hora_inicio);
  const dtF = new Date(sessao.data_hora_fim);
  document.getElementById('inp-data').value        = dataISO(dtI);
  document.getElementById('inp-hora-inicio').value = formatHora(dtI);
  document.getElementById('inp-hora-fim').value    = formatHora(dtF);

  document.getElementById('inp-observacoes').value = sessao.observacoes ?? '';
  document.getElementById('inp-motivo').value      = sessao.motivo_cancelamento ?? '';

  setChipValue('tipo_sessao', sessao.tipo_sessao);
  setChipValue('status', sessao.status);
  onStatusChipChange();
  ocultarConflito();
  openModal('modal-sessao-backdrop');
}

window.abrirModalEditarId = function(id) {
  const s = sessoes.find(s => s.id === id);
  if (s) abrirModalEditar(s);
};

function fecharModal() {
  closeModal('modal-sessao-backdrop');
  sessaoEditando = null;
}

function limparModal() {
  document.getElementById('form-sessao').reset();
  resetChipsEm(document.getElementById('form-sessao'));
  document.getElementById('msg-erro-modal').hidden = true;
  document.getElementById('motivo-row').hidden = true;
  ocultarConflito();
}

function onStatusChipChange() {
  const status = getChipValue('status');
  document.getElementById('motivo-row').hidden = status !== 'CANCELADO';
}

// ── Conflito de sala ─────────────────────────────────────────────────────────

function verificarConflito() {
  const salaId = document.getElementById('sel-sala').value;
  const data   = document.getElementById('inp-data').value;
  const hI     = document.getElementById('inp-hora-inicio').value;
  const hF     = document.getElementById('inp-hora-fim').value;

  if (!salaId || !data || !hI || !hF) { ocultarConflito(); return; }

  const novoI = new Date(`${data}T${hI}:00`);
  const novoF = new Date(`${data}T${hF}:00`);
  if (novoI >= novoF) { ocultarConflito(); return; }

  const conflito = sessoes.find(s => {
    if (s.id === sessaoEditando?.id) return false;
    if (s.sala.id !== salaId) return false;
    if (s.status === 'CANCELADO') return false;
    const sI = new Date(s.data_hora_inicio);
    const sF = new Date(s.data_hora_fim);
    return novoI < sF && novoF > sI;
  });

  if (conflito) {
    const horaI = formatHora(new Date(conflito.data_hora_inicio));
    const horaF = formatHora(new Date(conflito.data_hora_fim));
    mostrarConflito(`Conflito com sessão de ${esc(conflito.paciente.nome_completo.split(' ')[0])} das ${horaI} às ${horaF}.`);
  } else {
    ocultarConflito();
  }
}

function mostrarConflito(msg) {
  const el = document.getElementById('conflict-alert');
  el.classList.remove('hidden');
  el.querySelector('.conflict-msg').textContent = msg;
}

function ocultarConflito() {
  document.getElementById('conflict-alert').classList.add('hidden');
}

// ── Validação ────────────────────────────────────────────────────────────────

function validarForm() {
  const erros = [];
  const req = (id, label) => {
    const el = document.getElementById(id);
    const vazio = !el.value.trim();
    el.classList.toggle('is-error', vazio);
    if (vazio) erros.push(label);
  };

  req('sel-paciente',      'Paciente');
  req('sel-fisioterapeuta','Fisioterapeuta');
  req('sel-sala',          'Sala / box');
  req('inp-data',          'Data');
  req('inp-hora-inicio',   'Hora início');
  req('inp-hora-fim',      'Hora fim');

  if (!getChipValue('tipo_sessao')) erros.push('Tipo de sessão');
  if (!getChipValue('status'))      erros.push('Status');

  const hI = document.getElementById('inp-hora-inicio').value;
  const hF = document.getElementById('inp-hora-fim').value;
  if (hI && hF && hI >= hF) {
    erros.push('Hora fim deve ser após a hora início');
    document.getElementById('inp-hora-fim').classList.add('is-error');
  }

  return erros;
}

function coletarDados() {
  const data = document.getElementById('inp-data').value;
  const hI   = document.getElementById('inp-hora-inicio').value;
  const hF   = document.getElementById('inp-hora-fim').value;

  return {
    paciente_id:         document.getElementById('sel-paciente').value,
    fisioterapeuta_id:   document.getElementById('sel-fisioterapeuta').value,
    sala_id:             document.getElementById('sel-sala').value,
    data_hora_inicio:    `${data}T${hI}:00`,
    data_hora_fim:       `${data}T${hF}:00`,
    tipo_sessao:         getChipValue('tipo_sessao'),
    status:              getChipValue('status'),
    observacoes:         document.getElementById('inp-observacoes').value.trim() || null,
    motivo_cancelamento: document.getElementById('inp-motivo').value.trim() || null,
  };
}

async function handleSubmit() {
  if (salvando) return;

  const erros = validarForm();
  if (erros.length) {
    document.getElementById('msg-erro-modal').hidden = false;
    document.getElementById('msg-erro-modal').textContent = `Preencha: ${erros.join(', ')}.`;
    return;
  }
  document.getElementById('msg-erro-modal').hidden = true;

  salvando = true;
  const btnSalvar = document.getElementById('btn-salvar-sessao');
  btnSalvar.disabled = true;
  btnSalvar.innerHTML = '<span class="spinner"></span> Salvando…';

  try {
    const dados = coletarDados();
    if (sessaoEditando) {
      await atualizarSessao(sessaoEditando.id, dados);
      showToast('Sessão atualizada com sucesso.', 'success');
    } else {
      await criarSessao(dados);
      showToast('Sessão criada com sucesso.', 'success');
    }
    fecharModal();
    await carregarSessoes();
  } catch (e) {
    document.getElementById('msg-erro-modal').hidden = false;
    document.getElementById('msg-erro-modal').textContent = e?.erro ?? 'Erro ao salvar sessão.';
  } finally {
    salvando = false;
    btnSalvar.disabled = false;
    btnSalvar.innerHTML = 'Salvar';
  }
}

async function handleExcluir() {
  if (!sessaoEditando) return;
  if (!confirm(`Deseja cancelar a sessão de ${sessaoEditando.paciente.nome_completo.split(' ')[0]}?`)) return;
  try {
    await atualizarStatusSessao(sessaoEditando.id, 'CANCELADO', 'Cancelada manualmente');
    showToast('Sessão cancelada.', 'warning');
    fecharModal();
    await carregarSessoes();
  } catch (e) {
    showToast(e?.erro ?? 'Erro ao cancelar.', 'error');
  }
}

// ── Clique em slot vazio do calendário ───────────────────────────────────────

function bindSlotClicks() {
  for (let i = 0; i < 7; i++) {
    const col  = document.getElementById(`cal-col-${i}`);
    const hdr  = document.getElementById(`cal-hdr-${i}`);

    col.addEventListener('click', (e) => {
      if (e.target.closest('.cal-event')) return;
      const colEl  = e.currentTarget;
      const rect   = colEl.getBoundingClientRect();
      const offsetY = e.clientY - rect.top + colEl.closest('.cal-body').scrollTop;
      const slot   = Math.floor(offsetY / SLOT_HEIGHT);
      const mins   = CAL_START + slot * 30;
      const dia    = addDias(semanaBase, i);
      abrirModalNovo(dataISO(dia), horarioDeMinutos(mins));
    });

    hdr.addEventListener('click', () => {
      const dia = addDias(semanaBase, i);
      abrirModalNovo(dataISO(dia), '08:00');
    });
  }
}

// ── Eventos de UI ────────────────────────────────────────────────────────────

function bindEvents() {
  document.getElementById('btn-semana-ant').addEventListener('click', semanaAnterior);
  document.getElementById('btn-semana-prox').addEventListener('click', proximaSemana);
  document.getElementById('btn-hoje').addEventListener('click', irParaHoje);

  document.getElementById('btn-nova-sessao').addEventListener('click', () => {
    abrirModalNovo(dataISO(new Date()), '08:00');
  });

  document.getElementById('view-calendario').addEventListener('click', () => setVista('calendario'));
  document.getElementById('view-lista').addEventListener('click', () => setVista('lista'));

  document.getElementById('filtro-fisio').addEventListener('change', e => {
    filtroFisioId = e.target.value;
    renderView();
  });

  document.getElementById('filtro-status').addEventListener('change', e => {
    filtroStatus = e.target.value;
    renderView();
  });

  // Modal salvar / fechar
  document.getElementById('btn-salvar-sessao').addEventListener('click', handleSubmit);
  document.getElementById('btn-cancelar-modal').addEventListener('click', fecharModal);
  document.getElementById('btn-fechar-modal').addEventListener('click', fecharModal);
  document.getElementById('btn-excluir-sessao').addEventListener('click', handleExcluir);
  document.getElementById('modal-sessao-backdrop').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) fecharModal();
  });

  // Tecla ESC fecha modal
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') fecharModal();
  });

  // Chips no modal
  bindChipsEm(document.getElementById('form-sessao'));

  // Verificar conflito ao mudar sala/data/hora
  ['sel-sala','inp-data','inp-hora-inicio','inp-hora-fim'].forEach(id => {
    document.getElementById(id).addEventListener('change', verificarConflito);
  });

  // Auto preenche hora fim (+1h)
  document.getElementById('inp-hora-inicio').addEventListener('change', (e) => {
    const hF = document.getElementById('inp-hora-fim');
    if (!hF.value) {
      const mins = minutosDeHorario(e.target.value) + 60;
      hF.value = horarioDeMinutos(Math.min(mins, CAL_END));
    }
  });

  bindSlotClicks();
}

// ── Monta estrutura DOM do calendário ───────────────────────────────────────

function initCalendarDom() {
  const timesCol = document.getElementById('cal-times');
  timesCol.innerHTML = '';
  for (let i = 0; i < SLOTS; i++) {
    const mins  = CAL_START + i * 30;
    const label = document.createElement('div');
    label.className   = 'cal-time-label';
    label.textContent = horarioDeMinutos(mins);
    timesCol.appendChild(label);
  }

  for (let i = 0; i < 7; i++) {
    const col = document.getElementById(`cal-col-${i}`);
    col.innerHTML = '';
    for (let j = 0; j < SLOTS; j++) {
      const slot = document.createElement('div');
      slot.className = 'cal-slot';
      col.appendChild(slot);
    }
  }
}

// ── Init ─────────────────────────────────────────────────────────────────────

async function init() {
  initTopbar();
  initCalendarDom();
  bindEvents();
  await carregarDados();
}

init();
