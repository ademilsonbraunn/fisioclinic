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

import { getToken } from '../utils/auth.js';
import { API_BASE_URL as API_BASE } from '../config.js';

function headers() {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` };
}

// Extrai JSON da resposta e relança como erro se status não for OK
async function handleResponse(res, path, method) {
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    console.error(`[planos] ${method} ${path} → HTTP ${res.status}`, data);
    throw data ?? { erro: `HTTP ${res.status}`, status: res.status };
  }
  return data;
}

export async function listarPlanos(pacienteId) {
  const path = `/planos?paciente_id=${pacienteId}`;
  const res = await fetch(`${API_BASE}${path}`, { headers: headers() });
  return handleResponse(res, path, 'GET');
}

export async function buscarPlano(id) {
  const path = `/planos/${id}`;
  const res = await fetch(`${API_BASE}${path}`, { headers: headers() });
  return handleResponse(res, path, 'GET');
}

export async function criarPlano(payload) {
  const path = '/planos';
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res, path, 'POST');
}

export async function atualizarPlano(id, payload) {
  const path = `/planos/${id}`;
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify(payload),
  });
  return handleResponse(res, path, 'PATCH');
}

export async function atualizarStatusPlano(id, status) {
  const path = `/planos/${id}/status`;
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify({ status }),
  });
  return handleResponse(res, path, 'PATCH');
}
