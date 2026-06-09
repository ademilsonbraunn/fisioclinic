// ─────────────────────────────────────────────────────────────────────────────
// api/anamneses.js — Fetch wrapper para /api/anamneses
// ─────────────────────────────────────────────────────────────────────────────
// Módulo de acesso à API de anamnese e avaliação inicial (Módulo 2).
//
// Funções exportadas:
//   listarAnamneses(pacienteId) → GET /api/anamneses?paciente_id={id}
//   buscarAnamnese(id)          → GET /api/anamneses/{id}
//   criarAnamnese(dados)        → POST /api/anamneses
//   atualizarAnamnese(id, dados) → PATCH /api/anamneses/{id}
//
// Lê token de sessionStorage com fallback para localStorage (ver auth.js#getToken).
// Redireciona para index.html em caso de 401.
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
    console.error(`[anamneses] ${opts.method || 'GET'} ${path} → HTTP ${res.status}`, err);
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

// ── Arquivos da anamnese (M2) ─────────────────────────────────────────────────

export const listarArquivos = (anamneseId) =>
  req(`/anamneses/${anamneseId}/arquivos`);

// Upload multipart — não usa headers JSON; FormData define o Content-Type
export async function uploadArquivo(anamneseId, formData) {
  const tkn = getToken();
  const res = await fetch(`${API_BASE}/anamneses/${anamneseId}/arquivos`, {
    method: 'POST',
    headers: tkn ? { Authorization: `Bearer ${tkn}` } : {},
    body: formData,
  });
  if (res.status === 401) { location.href = '../index.html'; return; }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ erro: 'Erro ao enviar arquivo' }));
    throw err;
  }
  return res.json();
}

export async function deletarArquivo(anamneseId, arquivoId) {
  const tkn = getToken();
  const res = await fetch(`${API_BASE}/anamneses/${anamneseId}/arquivos/${arquivoId}`, {
    method: 'DELETE',
    headers: tkn ? { Authorization: `Bearer ${tkn}` } : {},
  });
  if (res.status === 401) { location.href = '../index.html'; return; }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ erro: 'Erro ao remover arquivo' }));
    throw err;
  }
}
