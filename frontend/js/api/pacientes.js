const API_BASE = 'http://localhost:8080/api';

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
