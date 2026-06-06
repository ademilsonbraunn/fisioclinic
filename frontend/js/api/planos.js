// ─────────────────────────────────────────────────────────────────────────────
// api/planos.js — Fetch wrapper para /api/planos
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso à API de plano de tratamento (Módulo 3).
//
// Funções exportadas:
//   listarPlanos(pacienteId)         → GET /api/planos?paciente_id={id}
//   buscarPlano(id)                  → GET /api/planos/{id}
//   criarPlano(payload)              → POST /api/planos
//   atualizarPlano(id, payload)      → PATCH /api/planos/{id}
//   atualizarStatusPlano(id, status) → PATCH /api/planos/{id}/status
//
// Diferente dos outros wrappers: usa handleResponse() que lança o objeto
// de erro completo (não apenas a mensagem) para que o chamador possa inspecionar
// campos como status HTTP e campos de validação.
// ─────────────────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080/api';

function token() {
  return sessionStorage.getItem('token') || localStorage.getItem('token');
}

function headers() {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token()}` };
}

// Extrai JSON da resposta e relança como erro se status não for OK
async function handleResponse(res) {
  const data = await res.json().catch(() => null);
  if (!res.ok) throw data ?? { erro: `HTTP ${res.status}`, status: res.status };
  return data;
}

export async function listarPlanos(pacienteId) {
  const res = await fetch(`${API_BASE}/planos?paciente_id=${pacienteId}`, {
    headers: headers(),
  });
  return handleResponse(res);
}

export async function buscarPlano(id) {
  const res = await fetch(`${API_BASE}/planos/${id}`, { headers: headers() });
  return handleResponse(res);
}

export async function criarPlano(payload) {
  const res = await fetch(`${API_BASE}/planos`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function atualizarPlano(id, payload) {
  const res = await fetch(`${API_BASE}/planos/${id}`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function atualizarStatusPlano(id, status) {
  const res = await fetch(`${API_BASE}/planos/${id}/status`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify({ status }),
  });
  return handleResponse(res);
}
