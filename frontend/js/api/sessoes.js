const API_BASE = 'http://localhost:8080/api';

function headers() {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token');
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
