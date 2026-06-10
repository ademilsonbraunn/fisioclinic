/**
 * [Auditoria P2] — API wrapper para trilha de auditoria do prontuário
 * Endpoint: GET /api/auditoria/paciente/{id}
 * Dados retornados: metadados de quem fez o quê e quando — sem dados clínicos (LGPD)
 */

import { getToken } from '../utils/auth.js';
import { API_BASE_URL as API_BASE } from '../config.js';

export async function listarAuditoriaPaciente(pacienteId) {
  const token = getToken();
  const res = await fetch(`${API_BASE}/auditoria/paciente/${pacienteId}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  if (res.status === 401) { location.href = '../index.html'; return []; }
  if (!res.ok) throw new Error(`Erro ao carregar auditoria: ${res.status}`);
  return res.json();
}
