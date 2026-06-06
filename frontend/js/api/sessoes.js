// ─────────────────────────────────────────────────────────────────────────────
// api/sessoes.js — Fetch wrapper para /api/sessoes
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso à API de agendamento (Módulo 4).
//
// Funções exportadas:
//   listarSessoes(params)             → GET /api/sessoes?[data_inicio&data_fim&paciente_id]
//   listarSessoesSemana(params)       → GET /api/sessoes/semana
//   buscarSessao(id)                  → GET /api/sessoes/{id}
//   criarSessao(dados)                → POST /api/sessoes
//   atualizarStatusSessao(id, status, motivo) → PATCH /api/sessoes/{id}/status
//   atualizarSessao(id, dados)        → PATCH /api/sessoes/{id}
//   excluirSessao(id)                 → DELETE /api/sessoes/{id}
//
// Lê token de sessionStorage com fallback para localStorage (ver auth.js#getToken).
// Redireciona para index.html automaticamente se receber HTTP 401.
// Retorna null para respostas 204 No Content (excluirSessao).
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
    console.error(`[sessoes] ${opts.method || 'GET'} ${path} → HTTP ${res.status}`, err);
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

export async function listarSessoes(params = {}) {
  const q = new URLSearchParams(params).toString();
  return req(`/sessoes${q ? '?' + q : ''}`);
}

export async function listarSessoesSemana(params = {}) {
  const q = new URLSearchParams(params).toString();
  return req(`/sessoes/semana${q ? '?' + q : ''}`);
}

export async function buscarSessao(id) {
  return req(`/sessoes/${id}`);
}

export async function criarSessao(dados) {
  return req('/sessoes', { method: 'POST', body: JSON.stringify(dados) });
}

export async function atualizarStatusSessao(id, status, motivo) {
  return req(`/sessoes/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, motivo_cancelamento: motivo ?? null }),
  });
}

export async function atualizarSessao(id, dados) {
  return req(`/sessoes/${id}`, { method: 'PATCH', body: JSON.stringify(dados) });
}

export async function excluirSessao(id) {
  return req(`/sessoes/${id}`, { method: 'DELETE' });
}
