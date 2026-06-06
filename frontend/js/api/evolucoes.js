// ─────────────────────────────────────────────────────────────────────────────
// api/evolucoes.js — Fetch wrapper para /api/evolucoes
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso à API de evolução clínica SOAP (Módulo 5).
//
// Funções exportadas:
//   listarEvolucoesPaciente(pacienteId) → GET /api/evolucoes/paciente/{id}
//   buscarEvolucaoSessao(sessaoId)      → GET /api/evolucoes/sessao/{id}
//   criarEvolucao(dados)               → POST /api/evolucoes
//
// Leitura: nota que não existe endpoint de atualização de evolução — prontuários
// são imutáveis após registro (Resolução CFM 1.821/07).
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
    console.error(`[evolucoes] ${opts.method || 'GET'} ${path} → HTTP ${res.status}`, err);
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

export async function listarEvolucoesPaciente(pacienteId) {
  return req(`/evolucoes/paciente/${pacienteId}`);
}

export async function buscarEvolucaoSessao(sessaoId) {
  return req(`/evolucoes/sessao/${sessaoId}`);
}

export async function criarEvolucao(dados) {
  return req('/evolucoes', { method: 'POST', body: JSON.stringify(dados) });
}
