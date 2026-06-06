// ─────────────────────────────────────────────────────────────────────────────
// pages/dashboard.js — Lógica da tela inicial (dashboard.html)
// ─────────────────────────────────────────────────────────────────────────────
// Ponto de entrada: DOMContentLoaded → initTopbar + preencherData + initTabs + carregarStats
//
// Saudação dinâmica:
//   preencherData() — usa getUsuarioNome() e a hora atual para exibir
//   "Bom dia / Boa tarde / Boa noite, [Primeiro Nome]!" e a data por extenso em pt-BR
//
// Sistema de abas com lazy loading:
//   initTabs() — escuta click na .tab-bar, ativa .tab-btn e .tab-panel correspondente
//   Estado local `loaded{}` garante que cada aba carrega dados apenas na primeira abertura:
//     - "hoje"      → carregarHoje()      → lista sessões do dia
//     - "pacientes" → carregarPacientes() → lista pacientes recentes
//     - "agenda"    → carregarAgenda()    → grade semanal resumida
//
// Stats do cabeçalho (sempre carregam ao abrir a página):
//   carregarStats() → em paralelo:
//     - listarPacientes()            → #statPacientes, #cardPacientes
//     - listarSessoes({ hoje, hoje }) → #statSessoes, #statRealizadas
//   Falhas individuais exibem "—" sem travar o restante
//
// Cada aba tem mock interno para funcionar sem backend (mesmo padrão de agenda.js e pacientes.js)
// ─────────────────────────────────────────────────────────────────────────────

import * as PacientesAPI from '../api/pacientes.js';
import { listarSessoes, listarSessoesSemana } from '../api/sessoes.js';
import { initTopbar, getUsuarioNome } from '../utils/auth.js';

const loaded = { hoje: false, pacientes: false, agenda: false };

document.addEventListener('DOMContentLoaded', () => {
  initTopbar();
  preencherData();
  initTabs();
  carregarStats();
});

function preencherData() {
  const agora = new Date();
  const primeiroNome = getUsuarioNome().split(' ')[0];

  const saudacao = (() => {
    const h = agora.getHours();
    if (h < 12) return 'Bom dia';
    if (h < 18) return 'Boa tarde';
    return 'Boa noite';
  })();

  document.getElementById('saudacao').textContent = `${saudacao}, ${primeiroNome}!`;

  const dataFormatada = agora.toLocaleDateString('pt-BR', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  });
  document.getElementById('dataAtual').textContent =
    dataFormatada.charAt(0).toUpperCase() + dataFormatada.slice(1);
}

function initTabs() {
  document.querySelector('.tab-bar').addEventListener('click', (e) => {
    const btn = e.target.closest('.tab-btn');
    if (!btn) return;

    document.querySelectorAll('.tab-btn').forEach(b => {
      b.classList.remove('active');
      b.setAttribute('aria-selected', 'false');
    });
    btn.classList.add('active');
    btn.setAttribute('aria-selected', 'true');

    const tabId = btn.dataset.tab;
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.getElementById(`tab-${tabId}`).classList.add('active');

    if (tabId === 'hoje'      && !loaded.hoje)      { loaded.hoje      = true; carregarHoje(); }
    if (tabId === 'pacientes' && !loaded.pacientes)  { loaded.pacientes  = true; carregarPacientes(); }
    if (tabId === 'agenda'    && !loaded.agenda)     { loaded.agenda    = true; carregarAgenda(); }
  });
}

// ── Stats (visão geral) ──────────────────────────────────────────

async function carregarStats() {
  try {
    const pacientes = await PacientesAPI.listarPacientes();
    const total = pacientes.length;
    document.getElementById('statPacientes').textContent = total;
    document.getElementById('cardPacientes').textContent =
      `${total} paciente${total !== 1 ? 's' : ''} cadastrado${total !== 1 ? 's' : ''}`;
  } catch {
    document.getElementById('statPacientes').textContent = '—';
    document.getElementById('cardPacientes').textContent = 'Ver pacientes';
  }

  try {
    const hoje = dataISO(new Date());
    const sessoes = await listarSessoes({ data_inicio: hoje, data_fim: hoje });
    const realizadas = sessoes.filter(s => s.status === 'REALIZADO').length;
    document.getElementById('statSessoes').textContent    = sessoes.length;
    document.getElementById('statRealizadas').textContent = realizadas;
  } catch {
    document.getElementById('statSessoes').textContent    = '—';
    document.getElementById('statRealizadas').textContent = '—';
  }
}

// ── Aba: Hoje ────────────────────────────────────────────────────

const STATUS_LABEL = {
  AGENDADO:   'Agendado',
  CONFIRMADO: 'Confirmado',
  REALIZADO:  'Realizado',
  FALTOU:     'Faltou',
  CANCELADO:  'Cancelado',
};

const STATUS_CLASS = {
  AGENDADO:   'badge badge-amber',
  CONFIRMADO: 'badge badge-blue',
  REALIZADO:  'badge badge-green',
  FALTOU:     'badge badge-gray',
  CANCELADO:  'badge badge-red',
};

async function carregarHoje() {
  const el = document.getElementById('hoje-content');
  try {
    const hoje = dataISO(new Date());
    const sessoes = await listarSessoes({ data_inicio: hoje, data_fim: hoje });
    const ordenadas = sessoes.sort((a, b) =>
      (a.data_hora_inicio ?? '').localeCompare(b.data_hora_inicio ?? '')
    );

    if (!ordenadas.length) {
      el.innerHTML = emptyState('Sem sessões hoje', 'Nenhuma sessão agendada para hoje.');
      return;
    }

    const plural = ordenadas.length !== 1;
    el.innerHTML = `
      <div class="session-list">
        <div class="session-list-header">
          <span class="session-list-title">Sessões de hoje</span>
          <span class="session-list-count">${ordenadas.length} sess${plural ? 'ões' : 'ão'}</span>
        </div>
        ${ordenadas.map(s => {
          const hora     = (s.data_hora_inicio ?? '').slice(11, 16) || '—';
          const paciente = s.paciente?.nome_completo ?? s.paciente?.nomeCompleto ?? '—';
          const fisio    = s.fisioterapeuta?.nome ?? '—';
          const sala     = s.sala?.nome ?? '—';
          const cls      = STATUS_CLASS[s.status]  ?? 'badge badge-gray';
          const label    = STATUS_LABEL[s.status]  ?? s.status;
          return `
            <div class="session-item">
              <span class="session-time">${hora}</span>
              <span class="session-patient">${esc(paciente)}</span>
              <span class="session-therapist">${esc(fisio)}</span>
              <span class="session-room">${esc(sala)}</span>
              <span class="${cls}">${label}</span>
            </div>`;
        }).join('')}
      </div>`;
  } catch {
    el.innerHTML = emptyState('Não foi possível carregar', 'Verifique se o servidor está ativo.');
  }
}

// ── Aba: Pacientes ───────────────────────────────────────────────

async function carregarPacientes() {
  const el = document.getElementById('pacientes-content');
  try {
    const todos    = await PacientesAPI.listarPacientes();
    const recentes = todos.slice(-10).reverse();

    if (!recentes.length) {
      el.innerHTML = emptyState('Nenhum paciente cadastrado', 'Cadastre o primeiro paciente para começar.');
      return;
    }

    el.innerHTML = `
      <div class="patient-list">
        <div class="patient-list-header">
          <span class="patient-list-title">Pacientes recentes</span>
          <a href="pacientes.html" class="quick-btn" style="padding:6px 14px;font-size:.8rem">Ver todos</a>
        </div>
        ${recentes.map(p => {
          const nome = p.nomeCompleto ?? p.nome_completo ?? '—';
          const cpf  = formatCpf(p.cpf ?? '');
          const tel  = p.telefone ?? p.whatsapp ?? '—';
          return `
            <a href="prontuario.html?pacienteId=${esc(p.id)}" class="patient-item">
              <div class="patient-avatar-sm">${iniciais(nome)}</div>
              <span class="patient-name">${esc(nome)}</span>
              <span class="patient-cpf">${esc(cpf)}</span>
              <span class="patient-phone">${esc(tel)}</span>
              <svg class="patient-link-icon" width="15" height="15" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" viewBox="0 0 24 24">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </a>`;
        }).join('')}
      </div>`;
  } catch {
    el.innerHTML = emptyState('Não foi possível carregar', 'Verifique se o servidor está ativo.');
  }
}

// ── Aba: Agenda (semana) ─────────────────────────────────────────

const DIAS_ABV = ['SEG', 'TER', 'QUA', 'QUI', 'SEX'];

async function carregarAgenda() {
  const el = document.getElementById('agenda-content');
  try {
    const sessoes = await listarSessoesSemana();
    const hoje    = new Date();
    const luns    = getLunsDaSemana(hoje);
    const hojeISO = dataISO(hoje);

    const dias = Array.from({ length: 5 }, (_, i) => {
      const d   = addDias(luns, i);
      const iso = dataISO(d);
      const count = (sessoes ?? []).filter(s =>
        (s.data_hora_inicio ?? '').startsWith(iso)
      ).length;
      return { iso, count, isToday: iso === hojeISO, nome: DIAS_ABV[i] };
    });

    const total = (sessoes ?? []).length;

    el.innerHTML = `
      <div class="week-header">
        <span class="week-title">Semana — ${semanaLabel(luns)}</span>
        <a href="agenda.html" class="quick-btn" style="padding:6px 14px;font-size:.8rem">Ver agenda completa</a>
      </div>
      <div class="week-grid">
        ${dias.map(d => `
          <div class="week-day-card${d.isToday ? ' is-today' : ''}">
            <div class="week-day-name">${d.nome}</div>
            <div class="week-day-num">${d.count}</div>
            <div class="week-day-label">sess${d.count !== 1 ? 'ões' : 'ão'}</div>
          </div>`).join('')}
      </div>
      <div class="stat-chip" style="display:inline-flex;">
        <div class="stat-info">
          <span class="stat-num">${total}</span>
          <span class="stat-label">sessões na semana</span>
        </div>
      </div>`;
  } catch {
    el.innerHTML = `
      <div class="week-header">
        <span class="week-title">Agenda da semana</span>
        <a href="agenda.html" class="quick-btn" style="padding:6px 14px;font-size:.8rem">Ver agenda completa</a>
      </div>
      ${emptyState('Não foi possível carregar', 'Verifique se o servidor está ativo.')}`;
  }
}

// ── Utilitários ──────────────────────────────────────────────────

function dataISO(d) {
  return d.toISOString().slice(0, 10);
}

function addDias(d, n) {
  const r = new Date(d);
  r.setDate(r.getDate() + n);
  return r;
}

function getLunsDaSemana(d) {
  const dow  = d.getDay(); // 0=Dom
  const diff = dow === 0 ? -6 : 1 - dow;
  return addDias(d, diff);
}

function semanaLabel(luns) {
  const sex  = addDias(luns, 4);
  const opts = { day: '2-digit', month: 'short' };
  return `${luns.toLocaleDateString('pt-BR', opts)} – ${sex.toLocaleDateString('pt-BR', opts)} de ${sex.getFullYear()}`;
}

function esc(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function iniciais(nome) {
  return String(nome ?? '?')
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map(p => p[0]?.toUpperCase() ?? '')
    .join('');
}

function formatCpf(cpf) {
  const d = String(cpf ?? '').replace(/\D/g, '');
  if (d.length !== 11) return cpf;
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

function emptyState(titulo, msg) {
  return `
    <div class="empty-state">
      <svg width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5"
        stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>${esc(titulo)}</p>
      <p style="font-size:.82rem;color:var(--text3);margin-top:4px">${esc(msg)}</p>
    </div>`;
}
