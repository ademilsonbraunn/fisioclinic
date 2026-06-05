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

export const listarAnamneses = (pacienteId) =>
  req(`/anamneses?paciente_id=${pacienteId}`);

export const buscarAnamnese = (id) =>
  req(`/anamneses/${id}`);

export const criarAnamnese = (dados) =>
  req('/anamneses', { method: 'POST', body: JSON.stringify(dados) });

export const atualizarAnamnese = (id, dados) =>
  req(`/anamneses/${id}`, { method: 'PATCH', body: JSON.stringify(dados) });
