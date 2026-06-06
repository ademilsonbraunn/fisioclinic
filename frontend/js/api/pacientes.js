// ─────────────────────────────────────────────────────────────────────────────
// api/pacientes.js — Fetch wrapper para /api/pacientes
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso à API de pacientes. Toda chamada HTTP do frontend relativa
// a pacientes deve passar por aqui — nunca fazer fetch inline nas páginas.
//
// Funções exportadas:
//   listarPacientes(params) → GET /api/pacientes?[busca=termo]
//   buscarPaciente(id)      → GET /api/pacientes/{id}
//   criarPaciente(dados)    → POST /api/pacientes
//   atualizarPaciente(id, dados) → PATCH /api/pacientes/{id}
//
// Nota: lê token do localStorage (não sessionStorage) pois pacientes.js foi
// escrito antes da padronização para sessionStorage feita nos módulos seguintes.
// Erros de API são relançados como Error com a mensagem do campo "erro" do JSON.
// ─────────────────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080/api';

// Monta headers com Content-Type e Bearer token
function headers() {
  const token = localStorage.getItem('token');
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

async function req(path, opts = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...opts,
    headers: { ...headers(), ...opts.headers },
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.erro || `Erro ${res.status}`);
  }
  return res.json();
}

export const listarPacientes = (params = {}) => {
  const qs = new URLSearchParams(params).toString();
  return req(`/pacientes${qs ? '?' + qs : ''}`);
};

export const buscarPaciente  = id       => req(`/pacientes/${id}`);
export const criarPaciente   = dados    => req('/pacientes', { method: 'POST', body: JSON.stringify(dados) });
export const atualizarPaciente = (id, dados) => req(`/pacientes/${id}`, { method: 'PATCH', body: JSON.stringify(dados) });
