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

const API_BASE = 'http://localhost:8080/api';

function getAuthHeaders() {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

export async function listarAtualizacoes() {
    const resp = await fetch(`${API_BASE}/atualizacoes`, {
        headers: getAuthHeaders()
    });
    if (!resp.ok) return [];
    return resp.json();
}
