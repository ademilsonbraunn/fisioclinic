// ─────────────────────────────────────────────────────────────────────────────
// api/altas.js — Fetch wrapper para /api/altas (Módulo 6)
// ─────────────────────────────────────────────────────────────────────────────
// Funções exportadas:
//   criarAlta(dados)               → POST /api/altas
//   listarAltasPaciente(pacienteId) → GET /api/altas/paciente/{id}
//   buscarAlta(altaId)             → GET /api/altas/{id}
//
// Alta é um evento terminal — não existe endpoint de atualização ou exclusão.
// ─────────────────────────────────────────────────────────────────────────────

import { getToken } from '../utils/auth.js';
import { API_BASE_URL as API_BASE } from '../config.js';

function headers() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function req(path, opts = {}) {
  const res = await fetch(`${API_BASE}${path}`, { headers: headers(), ...opts });
  if (res.status === 401) { location.href = '../index.html'; return; }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ erro: 'Erro desconhecido' }));
    console.error(`[altas] ${opts.method || 'GET'} ${path} → HTTP ${res.status}`, err);
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

export async function criarAlta(dados) {
  return req('/altas', { method: 'POST', body: JSON.stringify(dados) });
}

export async function listarAltasPaciente(pacienteId) {
  return req(`/altas/paciente/${pacienteId}`);
}

export async function buscarAlta(altaId) {
  return req(`/altas/${altaId}`);
}
