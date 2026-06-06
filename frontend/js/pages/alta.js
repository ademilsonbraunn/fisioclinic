/**
 * Módulo 6 — Alta e Relatórios
 * Registra o encerramento do tratamento fisioterapêutico.
 * Consome dados de M1 (paciente), M3 (plano), M4 (sessões) e M5 (evoluções).
 * Acesso: alta.html?paciente_id=UUID[&plano_id=UUID]
 */

import { initTopbar } from '../utils/auth.js';
import { showToast } from '../components/toast.js';
import { criarAlta, listarAltasPaciente } from '../api/altas.js';
import { listarPlanos } from '../api/planos.js';
import { listarSessoes } from '../api/sessoes.js';
import { listarEvolucoesPaciente } from '../api/evolucoes.js';

const API_BASE = 'http://localhost:8080/api';

// ── Estado ────────────────────────────────────────────────────────────────────
let pacienteId  = null;
let planoIdUrl  = null;   // plano_id vindo da URL (pré-seleciona o select)
let salvando    = false;

// ── DOM helpers ───────────────────────────────────────────────────────────────
const $ = id => document.getElementById(id);

function esc(str) {
  return String(str ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function iniciais(nome) {
  return String(nome ?? '').trim().split(/\s+/).slice(0,2).map(p => p[0]?.toUpperCase() ?? '').join('');
}

function formatCpf(cpf) {
  if (!cpf) return '';
  const c = String(cpf).replace(/\D/g,'').padStart(11,'0');
  return c.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

function formatData(iso) {
  if (!iso) return '—';
  const d = new Date(iso + 'T00:00:00');
  return d.toLocaleDateString('pt-BR');
}

function motivoLabel(motivo) {
  const mapa = {
    alta_clinica:        'Alta Clínica',
    alta_administrativa: 'Alta Administrativa',
    desistencia:         'Desistência',
    encaminhamento:      'Encaminhamento',
    obito:               'Óbito',
  };
  return mapa[motivo] ?? motivo;
}

// ── Inicialização ─────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  initTopbar();
  bindSections();
  bindSatisfacaoStars();

  const params = new URLSearchParams(location.search);
  pacienteId = params.get('paciente_id');
  planoIdUrl = params.get('plano_id');

  if (!pacienteId) {
    $('no-patient').style.display = '';
    $('form-alta').style.display  = 'none';
    return;
  }

  // Atualizar link "Prontuário" da topbar e botão Cancelar
  $('topbar-back').href         = `prontuario.html?paciente_id=${pacienteId}`;
  $('btn-prontuario').href      = `prontuario.html?paciente_id=${pacienteId}`;
  $('btn-cancelar').addEventListener('click', () => {
    location.href = `prontuario.html?paciente_id=${pacienteId}`;
  });

  // Definir data de alta padrão = hoje
  const hoje = new Date().toISOString().split('T')[0];
  $('inp-data-alta').value = hoje;

  // Carregar dados em paralelo
  await Promise.allSettled([
    carregarPaciente(),
    carregarPlanos(),
    carregarFisios(),
    carregarResumo(),
    carregarAltasExistentes(),
  ]);

  bindSubmit();
  $('patient-header').style.display = '';
});

// ── Carregar paciente ─────────────────────────────────────────────────────────
async function carregarPaciente() {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    const res = await fetch(`${API_BASE}/pacientes/${pacienteId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return;
    const p = await res.json();

    $('ph-avatar').textContent = iniciais(p.nome_completo);
    $('ph-nome').textContent   = p.nome_completo ?? '—';
    $('ph-cpf').textContent    = formatCpf(p.cpf);

    if (p.data_nascimento) {
      $('ph-nasc').textContent = formatData(p.data_nascimento);
    }

    if (p.convenios?.length) {
      const conv = p.convenios[0];
      $('ph-convenio').textContent = conv.tipo_pagamento === 'PARTICULAR'
        ? 'Particular'
        : (conv.nome_convenio ?? 'Convênio');
    }
  } catch (e) {
    console.error('[alta] carregarPaciente', e);
  }
}

// ── Carregar planos ───────────────────────────────────────────────────────────
async function carregarPlanos() {
  try {
    const planos = await listarPlanos(pacienteId);
    const sel = $('inp-plano');
    planos.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = `${p.diagnostico_cif?.substring(0, 60) ?? 'Plano'}${p.diagnostico_cif?.length > 60 ? '…' : ''} (${p.status})`;
      sel.appendChild(opt);
    });
    // Pré-selecionar plano vindo da URL
    if (planoIdUrl) sel.value = planoIdUrl;
  } catch (e) {
    console.error('[alta] carregarPlanos', e);
  }
}

// ── Carregar fisioterapeutas ──────────────────────────────────────────────────
async function carregarFisios() {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    const res = await fetch(`${API_BASE}/fisioterapeutas`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return;
    const fisios = await res.json();
    const sel = $('inp-fisio-alta');
    fisios.forEach(f => {
      const opt = document.createElement('option');
      opt.value = f.id;
      opt.textContent = `${f.nome} (CRF ${f.crf})`;
      sel.appendChild(opt);
    });
  } catch (e) {
    console.error('[alta] carregarFisios', e);
  }
}

// ── Resumo do tratamento ──────────────────────────────────────────────────────
async function carregarResumo() {
  try {
    const [sessoes, evolucoes, planos] = await Promise.all([
      listarSessoes(pacienteId),
      listarEvolucoesPaciente(pacienteId),
      listarPlanos(pacienteId),
    ]);

    const realizadas = sessoes.filter(s => s.status === 'REALIZADO');
    $('res-sessoes').textContent = realizadas.length;

    // Calcular dias de tratamento (primeira sessão até hoje)
    if (realizadas.length >= 1) {
      const datas = realizadas.map(s => new Date(s.data_hora_inicio)).sort((a,b) => a-b);
      const diasTrat = Math.round((new Date() - datas[0]) / 86400000);
      $('res-dias').textContent = diasTrat > 0 ? diasTrat : 1;
    }

    // Sessões com EVA registrado
    const comEva = evolucoes.filter(e => e.eva_antes != null || e.eva_depois != null);
    $('res-eva').textContent = comEva.length;

    // Mostrar diagnóstico do plano ativo
    const planoAtivo = planos.find(p => p.status === 'ativo');
    if (planoAtivo?.diagnostico_cif) {
      $('res-diagnostico').innerHTML =
        `<strong>Diagnóstico CIF:</strong> ${esc(planoAtivo.diagnostico_cif)}`;
      $('res-diagnostico').style.display = '';
    }

    $('resumo-wrap').style.display = '';
  } catch (e) {
    console.error('[alta] carregarResumo', e);
  }
}

// ── Exibir alta mais recente já registrada ────────────────────────────────────
async function carregarAltasExistentes() {
  try {
    const altas = await listarAltasPaciente(pacienteId);
    if (!altas.length) return;

    const ultima = altas[0]; // ordenadas desc pelo backend
    $('ar-data').textContent      = formatData(ultima.data_alta);
    $('ar-motivo').textContent    = motivoLabel(ultima.motivo);
    $('ar-resultado').textContent = ultima.resultado_objetivos;
    $('alta-registrada').style.display = '';
  } catch (e) {
    console.error('[alta] carregarAltasExistentes', e);
  }
}

// ── Bind das seções colapsáveis ───────────────────────────────────────────────
function bindSections() {
  document.querySelectorAll('.alta-section-header').forEach(header => {
    header.addEventListener('click', () => {
      const section = header.closest('.alta-section');
      section.classList.toggle('open');
    });
  });
}

// ── Bind das estrelas de satisfação ──────────────────────────────────────────
function bindSatisfacaoStars() {
  const stars = document.querySelectorAll('.satisfacao-star input[type="radio"]');
  stars.forEach(input => {
    input.addEventListener('change', () => {
      const nota = parseInt(input.value, 10);
      stars.forEach((s, idx) => {
        s.nextElementSibling.classList.toggle('ativa', idx < nota);
      });
    });
  });
}

// ── Submit do formulário ──────────────────────────────────────────────────────
function bindSubmit() {
  $('btn-registrar').addEventListener('click', async () => {
    if (salvando) return;

    // Limpar erros anteriores
    ['err-motivo', 'err-resultado'].forEach(id => { $(id).textContent = ''; });

    // Validação
    const motivo    = document.querySelector('input[name="motivo"]:checked')?.value;
    const resultado = $('inp-resultado').value.trim();
    let valido = true;

    if (!motivo) {
      $('err-motivo').textContent = 'Selecione o motivo da alta.';
      // Abrir seção se estiver fechada
      $('sec-dados').classList.add('open');
      valido = false;
    }
    if (!resultado) {
      $('err-resultado').textContent = 'Descreva o resultado em relação aos objetivos.';
      $('sec-dados').classList.add('open');
      valido = false;
    }
    if (!valido) return;

    // Montar payload
    const satisfacaoNota = parseInt(
      document.querySelector('input[name="satisfacao_nota"]:checked')?.value ?? '', 10
    ) || null;

    const dados = {
      paciente_id:             pacienteId,
      motivo,
      resultado_objetivos:     resultado,
      data_alta:               $('inp-data-alta').value || null,
      fisioterapeuta_id:       $('inp-fisio-alta').value || null,
      plano_id:                $('inp-plano').value || null,
      orientacoes_domiciliares: $('inp-orientacoes').value.trim() || null,
      relatorio_evolucao:      $('inp-rel-evolucao').value.trim() || null,
      relatorio_medico:        $('inp-rel-medico').value.trim() || null,
      agendamento_retorno:     $('inp-retorno').value || null,
      satisfacao_nota:         satisfacaoNota,
      satisfacao_comentario:   $('inp-sat-comentario').value.trim() || null,
    };

    salvando = true;
    $('btn-registrar').disabled = true;
    $('btn-registrar').textContent = 'Registrando...';

    try {
      await criarAlta(dados);
      showToast('Alta registrada com sucesso!', 'success');
      setTimeout(() => {
        location.href = `prontuario.html?paciente_id=${pacienteId}`;
      }, 1500);
    } catch (err) {
      const msg = err?.erro ?? 'Erro ao registrar alta. Tente novamente.';
      showToast(msg, 'error');
      salvando = false;
      $('btn-registrar').disabled = false;
      $('btn-registrar').innerHTML = `
        <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" style="margin-right:6px">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        Registrar Alta`;
    }
  });
}
