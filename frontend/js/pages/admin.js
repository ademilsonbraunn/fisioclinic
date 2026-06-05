import { initTopbar, getToken, getUsuarioPerfil } from '../utils/auth.js';

const API = 'http://localhost:8080/api';

function authHeaders() {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
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
  carregarStats();
  bindFisioModal();
  bindSalaModal();
  bindSenhaForm();
  bindBusca();
});

// ── Stats ─────────────────────────────────────────────────────

async function carregarStats() {
  try {
    const [fisios, salas, pacientes] = await Promise.all([
      apiFetch('/fisioterapeutas').then(r => r?.ok ? r.json() : []),
      apiFetch('/salas').then(r => r?.ok ? r.json() : []),
      fetch(`${API}/pacientes`).then(r => r.ok ? r.json() : []),
    ]);
    setText('statFisios',    Array.isArray(fisios)    ? fisios.length    : '—');
    setText('statSalas',     Array.isArray(salas)     ? salas.length     : '—');
    setText('statPacientes', Array.isArray(pacientes) ? pacientes.length : '—');
  } catch {
    /* backend offline — mantém "—" */
  }
}

// ── Fisioterapeuta modal ──────────────────────────────────────

function bindFisioModal() {
  document.getElementById('btnAddFisio').addEventListener('click', () => abrirFisioModal());
  document.getElementById('btnFecharFisioModal').addEventListener('click', fecharFisioModal);
  document.getElementById('btnCancelarFisio').addEventListener('click', fecharFisioModal);
  document.getElementById('formFisio').addEventListener('submit', salvarFisio);
  document.getElementById('fisioModalBackdrop').addEventListener('click', e => {
    if (e.target === e.currentTarget) fecharFisioModal();
  });
}

function abrirFisioModal(dados = null) {
  document.getElementById('fisioModalTitulo').textContent =
    dados ? 'Editar fisioterapeuta' : 'Novo fisioterapeuta';
  document.getElementById('fisioId').value       = dados?.id       ?? '';
  document.getElementById('fisioNome').value     = dados?.nome     ?? '';
  document.getElementById('fisioCrf').value      = dados?.crf      ?? '';
  document.getElementById('fisioEmail').value    = dados?.email    ?? '';
  document.getElementById('fisioTelefone').value = dados?.telefone ?? '';
  document.getElementById('fisioModalBackdrop').classList.add('open');
  document.getElementById('fisioNome').focus();
}

function fecharFisioModal() {
  document.getElementById('fisioModalBackdrop').classList.remove('open');
  document.getElementById('formFisio').reset();
}

async function salvarFisio(e) {
  e.preventDefault();
  const id   = document.getElementById('fisioId').value;
  const body = {
    nome:     document.getElementById('fisioNome').value.trim(),
    crf:      document.getElementById('fisioCrf').value.trim(),
    email:    document.getElementById('fisioEmail').value.trim(),
    telefone: document.getElementById('fisioTelefone').value.trim(),
  };

  const url    = id ? `/fisioterapeutas/${id}` : '/fisioterapeutas';
  const method = id ? 'PATCH' : 'POST';

  const res = await apiFetch(url, { method, body: JSON.stringify(body) });
  if (res?.ok) { fecharFisioModal(); location.reload(); }
}

// ── Sala modal ────────────────────────────────────────────────

function bindSalaModal() {
  document.getElementById('btnAddSala').addEventListener('click', () => abrirSalaModal());
  document.getElementById('btnFecharSalaModal').addEventListener('click', fecharSalaModal);
  document.getElementById('btnCancelarSala').addEventListener('click', fecharSalaModal);
  document.getElementById('formSala').addEventListener('submit', salvarSala);
  document.getElementById('salaModalBackdrop').addEventListener('click', e => {
    if (e.target === e.currentTarget) fecharSalaModal();
  });
}

function abrirSalaModal(dados = null) {
  document.getElementById('salaModalTitulo').textContent =
    dados ? 'Editar sala' : 'Nova sala';
  document.getElementById('salaId').value         = dados?.id         ?? '';
  document.getElementById('salaNome').value       = dados?.nome       ?? '';
  document.getElementById('salaTipo').value       = dados?.tipo       ?? '';
  document.getElementById('salaCapacidade').value = dados?.capacidade ?? '';
  document.getElementById('salaModalBackdrop').classList.add('open');
  document.getElementById('salaNome').focus();
}

function fecharSalaModal() {
  document.getElementById('salaModalBackdrop').classList.remove('open');
  document.getElementById('formSala').reset();
}

async function salvarSala(e) {
  e.preventDefault();
  const id   = document.getElementById('salaId').value;
  const body = {
    nome:       document.getElementById('salaNome').value.trim(),
    tipo:       document.getElementById('salaTipo').value,
    capacidade: Number(document.getElementById('salaCapacidade').value) || 1,
  };

  const url    = id ? `/salas/${id}` : '/salas';
  const method = id ? 'PATCH' : 'POST';

  const res = await apiFetch(url, { method, body: JSON.stringify(body) });
  if (res?.ok) { fecharSalaModal(); location.reload(); }
}

// ── Alterar senha ─────────────────────────────────────────────

function bindSenhaForm() {
  document.getElementById('formSenha').addEventListener('submit', async (e) => {
    e.preventDefault();
    const nova     = document.getElementById('novaSenha').value;
    const confirma = document.getElementById('confirmaSenha').value;
    const errEl    = document.getElementById('erroSenha');

    if (nova !== confirma) {
      errEl.textContent    = 'As senhas não coincidem.';
      errEl.style.display  = 'block';
      return;
    }
    errEl.style.display = 'none';

    const res = await apiFetch('/auth/senha', {
      method: 'PATCH',
      body: JSON.stringify({
        senhaAtual: document.getElementById('senhaAtual').value,
        novaSenha:  nova,
      }),
    });

    if (res?.ok || res?.status === 204) {
      document.getElementById('formSenha').reset();
      errEl.textContent   = 'Senha alterada com sucesso!';
      errEl.style.color   = 'var(--green)';
      errEl.style.display = 'block';
      setTimeout(() => { errEl.style.display = 'none'; errEl.style.color = ''; }, 3000);
    } else if (res?.status === 409) {
      errEl.textContent   = 'Senha atual incorreta.';
      errEl.style.display = 'block';
    }
  });
}

// ── Busca de fisioterapeutas ──────────────────────────────────

function bindBusca() {
  document.getElementById('buscaFisio').addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase();
    document.querySelectorAll('#tabelaFisios tbody tr').forEach(tr => {
      tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  });
}

// ── Helpers ───────────────────────────────────────────────────

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}
