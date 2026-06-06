// ─────────────────────────────────────────────────────────────────────────────
// api/atualizacoes.js — Fetch wrapper para /api/atualizacoes
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso às novidades do sistema (changelog).
// Usado exclusivamente pelo componente notificacoes.js.
//
// Funções exportadas:
//   listarAtualizacoes() → GET /api/atualizacoes
//     Retorna as 10 atualizações mais recentes com ativo=true.
//     Em caso de erro (rede ou autenticação), retorna [] silenciosamente
//     para não quebrar o topbar se o backend estiver offline.
// ─────────────────────────────────────────────────────────────────────────────

import { getToken } from '../utils/auth.js';
import { API_BASE_URL as API_BASE } from '../config.js';

function getAuthHeaders() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

export async function listarAtualizacoes() {
  const resp = await fetch(`${API_BASE}/atualizacoes`, {
    headers: getAuthHeaders(),
  });
  if (!resp.ok) return [];
  return resp.json();
}
