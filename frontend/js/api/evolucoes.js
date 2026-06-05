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

export async function listarEvolucoesPaciente(pacienteId) {
  return req(`/evolucoes/paciente/${pacienteId}`);
}

export async function buscarEvolucaoSessao(sessaoId) {
  return req(`/evolucoes/sessao/${sessaoId}`);
}

export async function criarEvolucao(dados) {
  return req('/evolucoes', { method: 'POST', body: JSON.stringify(dados) });
}
