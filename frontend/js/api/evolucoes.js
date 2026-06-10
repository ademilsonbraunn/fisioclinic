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
// [M5 P2] Fotos comparativas:
//   listarFotosEvolucao(evolucaoId)                 → GET  /api/evolucoes/{id}/fotos
//   uploadFotoEvolucao(evolucaoId, formData)         → POST /api/evolucoes/{id}/fotos (multipart)
//   deletarFotoEvolucao(evolucaoId, fotoId)          → DELETE /api/evolucoes/{id}/fotos/{fotoId}
//   urlFotoEvolucao(evolucaoId, fotoId)              → string URL para exibição autenticada
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

// ── [M5 P2] Fotos Comparativas ───────────────────────────────────────────────

// [M5 P2] Lista fotos vinculadas a uma evolução (antes/depois)
export async function listarFotosEvolucao(evolucaoId) {
  return req(`/evolucoes/${evolucaoId}/fotos`);
}

/**
 * [M5 P2] Envia foto de evolução via multipart.
 * formData deve conter: file (File), tipo ('antes'|'depois'|'comparativo'|'outro')
 * Sem Content-Type manual — o browser define o boundary correto do multipart.
 */
export async function uploadFotoEvolucao(evolucaoId, formData) {
  const token = getToken();
  const res = await fetch(`${API_BASE}/evolucoes/${evolucaoId}/fotos`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  });
  if (res.status === 401) { location.href = '../index.html'; return; }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ erro: 'Erro desconhecido' }));
    throw err;
  }
  return res.json();
}

// [M5 P2] Remove uma foto de evolução
export async function deletarFotoEvolucao(evolucaoId, fotoId) {
  return req(`/evolucoes/${evolucaoId}/fotos/${fotoId}`, { method: 'DELETE' });
}

// [M5 P2] Retorna a URL autenticada para exibição inline da foto
export function urlFotoEvolucao(evolucaoId, fotoId) {
  return `${API_BASE}/evolucoes/${evolucaoId}/fotos/${fotoId}/arquivo`;
}
