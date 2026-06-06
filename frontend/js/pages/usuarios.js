// ─────────────────────────────────────────────────────────────────────────────
// pages/usuarios.js — Gestão de usuários do sistema (usuarios.html) — ROLE_ADMIN
// ─────────────────────────────────────────────────────────────────────────────
// Guard: redireciona para dashboard.html se o perfil não for ADMIN.
//
// Fluxo principal:
//  1. carregarUsuarios() → GET /api/fisioterapeutas → armazena em todosUsuarios[]
//  2. renderTabela(lista) → gera <tr> com avatar, badges de perfil/status e botões de ação
//  3. Filtros em tempo real: busca por nome/e-mail, perfil e status (client-side)
//
// Ações por linha (data-action):
//  "editar"  → abre modal com form pré-preenchido → PATCH /api/fisioterapeutas/{id}
//  "senha"   → abre modal de reset → PATCH /api/fisioterapeutas/{id}/senha
//  "status"  → PATCH /api/fisioterapeutas/{id}/status com { ativo: !atual }
//
// Criar usuário: POST /api/fisioterapeutas (senha opcional — padrão: "Fisio@123")
// ─────────────────────────────────────────────────────────────────────────────

import { initTopbar, getToken, getUsuarioPerfil } from '../utils/auth.js';

const API = 'http://localhost:8080/api';

let todosUsuarios = [];

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

async function apiFetch(path, options = {}) {
  const res = await fetch(`${API}${path}`, {
    ...options,
    headers: { ...authHeaders(), ...(options.headers ?? {}) },
  });
  if (res.status === 401) {
    window.location.href = '../index.html';
    return null;
  }
  return res;
}

// ── Init ──────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  if (getUsuarioPerfil() !== 'ADMIN') {
    window.location.href = 'dashboard.html';
    return;
  }

  initTopbar();
  carregarUsuarios();
  bindModalUsuario();
  bindModalSenha();
  bindFiltros();
});

// ── Carga e renderização ──────────────────────────────────────

async function carregarUsuarios() {
  const res = await apiFetch('/fisioterapeutas');
  if (!res?.ok) return;
  todosUsuarios = await res.json();
  atualizarStats();
  aplicarFiltros();
}

function atualizarStats() {
  const admins = todosUsuarios.filter(u => u.perfil === 'ADMIN').length;
  const ativos = todosUsuarios.filter(u => u.ativo).length;
  setText('statTotal',  todosUsuarios.length);
  setText('statAdmins', admins);
  setText('statAtivos', ativos);
}

function renderTabela(lista) {
  const tbody = document.querySelector('#tabelaUsuarios tbody');
  if (!tbody) return;

  if (!lista.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-row">Nenhum usuário encontrado.</td></tr>`;
    return;
  }

  tbody.innerHTML = lista.map(u => {
    const iniciais = u.nome.trim().split(/\s+/).slice(0, 2).map(p => p[0].toUpperCase()).join('');
    const perfilBadge = u.perfil === 'ADMIN'
      ? '<span class="badge badge-blue">Administrador</span>'
      : '<span class="badge badge-green">Fisioterapeuta</span>';
    const statusBadge = u.ativo
      ? '<span class="badge badge-green">Ativo</span>'
      : '<span class="badge badge-gray">Inativo</span>';
    const toggleTitle = u.ativo ? 'Desativar acesso' : 'Reativar acesso';
    const toggleIcon  = u.ativo
      ? `<svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`
      : `<svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>`;
    const toggleCls = u.ativo ? 'btn btn-danger btn-sm btn-icon' : 'btn btn-ghost btn-sm btn-icon';
    const tel = u.telefone ? formatarTelefone(u.telefone) : '';

    return `
      <tr>
        <td>
          <div class="avatar-cell">
            <div class="avatar">${iniciais}</div>
            <div>
              <div class="cell-name">${escapar(u.nome)}</div>
              ${tel ? `<div class="cell-meta">${tel}</div>` : ''}
            </div>
          </div>
        </td>
        <td class="text-sm text-2">${escapar(u.email)}</td>
        <td><span class="text-mono text-sm">${escapar(u.crf)}</span></td>
        <td>${perfilBadge}</td>
        <td>${statusBadge}</td>
        <td>
          <div style="display:flex;gap:4px;justify-content:flex-end;">
            <button class="btn btn-ghost btn-sm btn-icon" title="Editar"
              data-action="editar" data-id="${u.id}">
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" viewBox="0 0 24 24">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </button>
            <button class="btn btn-ghost btn-sm btn-icon" title="Redefinir senha"
              data-action="senha" data-id="${u.id}" data-nome="${escapar(u.nome)}">
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" viewBox="0 0 24 24">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </button>
            <button class="${toggleCls}" title="${toggleTitle}"
              data-action="status" data-id="${u.id}" data-ativo="${u.ativo}">
              ${toggleIcon}
            </button>
          </div>
        </td>
      </tr>`;
  }).join('');

  tbody.querySelectorAll('[data-action]').forEach(btn => {
    btn.addEventListener('click', handleRowAction);
  });
}

function handleRowAction(e) {
  const btn    = e.currentTarget;
  const action = btn.dataset.action;
  const id     = btn.dataset.id;

  if (action === 'editar') {
    abrirModalUsuario(todosUsuarios.find(u => u.id === id));
  } else if (action === 'senha') {
    abrirModalSenha(id, btn.dataset.nome);
  } else if (action === 'status') {
    toggleStatus(id, btn.dataset.ativo === 'true');
  }
}

// ── Filtros ──────────────────────────────────────────────────

function bindFiltros() {
  document.getElementById('buscaUsuario').addEventListener('input',  aplicarFiltros);
  document.getElementById('filtroPerfil').addEventListener('change', aplicarFiltros);
  document.getElementById('filtroStatus').addEventListener('change', aplicarFiltros);
}

function aplicarFiltros() {
  const q      = document.getElementById('buscaUsuario').value.toLowerCase().trim();
  const perfil = document.getElementById('filtroPerfil').value;
  const status = document.getElementById('filtroStatus').value;

  const filtrados = todosUsuarios.filter(u => {
    const matchQ      = !q || u.nome.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
    const matchPerfil = !perfil || u.perfil === perfil;
    const matchStatus = !status || (status === 'ativo' ? u.ativo : !u.ativo);
    return matchQ && matchPerfil && matchStatus;
  });

  renderTabela(filtrados);
}

// ── Modal criar / editar ──────────────────────────────────────

function bindModalUsuario() {
  document.getElementById('btnNovoUsuario').addEventListener('click', () => abrirModalUsuario());
  document.getElementById('btnFecharUsuarioModal').addEventListener('click', fecharModalUsuario);
  document.getElementById('btnCancelarUsuario').addEventListener('click', fecharModalUsuario);
  document.getElementById('formUsuario').addEventListener('submit', salvarUsuario);
  document.getElementById('usuarioModalBackdrop').addEventListener('click', e => {
    if (e.target === e.currentTarget) fecharModalUsuario();
  });
}

function abrirModalUsuario(dados = null) {
  const criando = !dados;
  document.getElementById('usuarioModalTitulo').textContent = criando ? 'Novo usuário' : 'Editar usuário';
  document.getElementById('usuarioId').value       = dados?.id       ?? '';
  document.getElementById('usuarioNome').value     = dados?.nome     ?? '';
  document.getElementById('usuarioCrf').value      = dados?.crf      ?? '';
  document.getElementById('usuarioEmail').value    = dados?.email    ?? '';
  document.getElementById('usuarioTelefone').value = dados?.telefone ?? '';
  document.getElementById('usuarioPerfil').value   = dados?.perfil   ?? 'FISIOTERAPEUTA';
  document.getElementById('senhaWrap').style.display = criando ? '' : 'none';
  document.getElementById('erroModalUsuario').style.display = 'none';
  document.getElementById('usuarioModalBackdrop').classList.add('open');
  document.getElementById('usuarioNome').focus();
}

function fecharModalUsuario() {
  document.getElementById('usuarioModalBackdrop').classList.remove('open');
  document.getElementById('formUsuario').reset();
}

async function salvarUsuario(e) {
  e.preventDefault();
  const id      = document.getElementById('usuarioId').value;
  const criando = !id;
  const errEl   = document.getElementById('erroModalUsuario');
  errEl.style.display = 'none';

  const body = {
    nome:     document.getElementById('usuarioNome').value.trim(),
    crf:      document.getElementById('usuarioCrf').value.trim(),
    email:    document.getElementById('usuarioEmail').value.trim(),
    telefone: document.getElementById('usuarioTelefone').value.trim() || null,
    perfil:   document.getElementById('usuarioPerfil').value,
  };

  if (criando) {
    const senha = document.getElementById('usuarioSenha').value;
    if (senha) body.senha = senha;
  }

  const url    = criando ? '/fisioterapeutas' : `/fisioterapeutas/${id}`;
  const method = criando ? 'POST' : 'PATCH';

  const submitBtn = document.querySelector('#formUsuario ~ .modal-footer .btn-primary');
  if (submitBtn) submitBtn.disabled = true;

  const res = await apiFetch(url, { method, body: JSON.stringify(body) });

  if (submitBtn) submitBtn.disabled = false;

  if (res?.ok || res?.status === 201) {
    fecharModalUsuario();
    await carregarUsuarios();
  } else {
    let msg = criando ? 'Erro ao criar usuário.' : 'Erro ao salvar alterações.';
    try {
      const data = await res?.json();
      if (data?.erro) msg = data.erro;
    } catch { /* mantém msg genérica */ }
    errEl.textContent   = msg;
    errEl.style.display = 'block';
  }
}

// ── Modal reset de senha ──────────────────────────────────────

function bindModalSenha() {
  document.getElementById('btnFecharSenhaModal').addEventListener('click', fecharModalSenha);
  document.getElementById('btnCancelarSenha').addEventListener('click', fecharModalSenha);
  document.getElementById('formResetSenha').addEventListener('submit', salvarSenha);
  document.getElementById('senhaModalBackdrop').addEventListener('click', e => {
    if (e.target === e.currentTarget) fecharModalSenha();
  });
}

function abrirModalSenha(id, nome) {
  document.getElementById('senhaUsuarioId').value = id;
  document.getElementById('senhaUsuarioNome').textContent = nome;
  document.getElementById('erroModalSenha').style.display = 'none';
  document.getElementById('formResetSenha').reset();
  document.getElementById('senhaModalBackdrop').classList.add('open');
  document.getElementById('resetNovaSenha').focus();
}

function fecharModalSenha() {
  document.getElementById('senhaModalBackdrop').classList.remove('open');
  document.getElementById('formResetSenha').reset();
}

async function salvarSenha(e) {
  e.preventDefault();
  const id    = document.getElementById('senhaUsuarioId').value;
  const nova  = document.getElementById('resetNovaSenha').value;
  const conf  = document.getElementById('resetConfirmarSenha').value;
  const errEl = document.getElementById('erroModalSenha');

  if (nova !== conf) {
    errEl.textContent   = 'As senhas não coincidem.';
    errEl.style.display = 'block';
    return;
  }
  if (nova.length < 8) {
    errEl.textContent   = 'A senha deve ter no mínimo 8 caracteres.';
    errEl.style.display = 'block';
    return;
  }
  errEl.style.display = 'none';

  const res = await apiFetch(`/fisioterapeutas/${id}/senha`, {
    method: 'PATCH',
    body: JSON.stringify({ novaSenha: nova }),
  });

  if (res?.status === 204 || res?.ok) {
    fecharModalSenha();
  } else {
    errEl.textContent   = 'Erro ao redefinir senha. Tente novamente.';
    errEl.style.display = 'block';
  }
}

// ── Toggle status ─────────────────────────────────────────────

async function toggleStatus(id, ativoAtual) {
  const res = await apiFetch(`/fisioterapeutas/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ ativo: !ativoAtual }),
  });
  if (res?.ok || res?.status === 204) {
    await carregarUsuarios();
  }
}

// ── Helpers ───────────────────────────────────────────────────

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

function escapar(str) {
  return String(str ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function formatarTelefone(tel) {
  const d = tel.replace(/\D/g, '');
  if (d.length === 11) return `(${d.slice(0,2)}) ${d.slice(2,7)}-${d.slice(7)}`;
  if (d.length === 10) return `(${d.slice(0,2)}) ${d.slice(2,6)}-${d.slice(6)}`;
  return tel;
}
