import * as API from '../api/pacientes.js';
import { initTopbar } from '../utils/auth.js';

document.addEventListener('DOMContentLoaded', () => {
  initTopbar();
  preencherData();
  carregarStats();
});

function preencherData() {
  const agora = new Date();

  const saudacao = (() => {
    const h = agora.getHours();
    if (h < 12) return 'Bom dia';
    if (h < 18) return 'Boa tarde';
    return 'Boa noite';
  })();

  document.getElementById('saudacao').textContent = `${saudacao}, João!`;

  const dataFormatada = agora.toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
  document.getElementById('dataAtual').textContent =
    dataFormatada.charAt(0).toUpperCase() + dataFormatada.slice(1);
}

async function carregarStats() {
  // Total de pacientes — dado real da API
  try {
    const pacientes = await API.listarPacientes();
    const total = pacientes.length;

    document.getElementById('statPacientes').textContent = total;
    document.getElementById('cardPacientes').textContent =
      `${total} paciente${total !== 1 ? 's' : ''} cadastrado${total !== 1 ? 's' : ''}`;
  } catch {
    document.getElementById('statPacientes').textContent = '—';
    document.getElementById('cardPacientes').textContent = 'Ver pacientes';
  }

  // Sessões do dia — placeholder (módulo 4 ainda em desenvolvimento)
  document.getElementById('statSessoes').textContent    = '—';
  document.getElementById('statRealizadas').textContent = '—';
}
