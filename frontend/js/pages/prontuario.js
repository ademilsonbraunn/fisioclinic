import { initTopbar } from '../utils/auth.js';
import { showToast } from '../components/toast.js';
import { listarAnamneses, criarAnamnese } from '../api/anamneses.js';
import { listarPlanos, criarPlano, atualizarStatusPlano } from '../api/planos.js';
import { listarEvolucoesPaciente, criarEvolucao } from '../api/evolucoes.js';
import { listarSessoes } from '../api/sessoes.js';

const API_BASE = 'http://localhost:8080/api';

// ── Estado ───────────────────────────────────────────────────────────────────
let pacienteId        = null;
let anamneses         = [];
let planos            = [];
let evolucoes         = [];
let sessoesPaciente   = [];
let fisios            = [];
let formVisivel       = false;
let formPlanoVisivel  = false;
let formEvolVisivel   = false;

// ── DOM helpers ──────────────────────────────────────────────────────────────
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

function token() {
  return sessionStorage.getItem('token') || localStorage.getItem('token');
}

// ── Inicialização ─────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  initTopbar();
  bindTabs();
  bindEva();

  const params = new URLSearchParams(window.location.search);
  pacienteId = params.get('paciente_id');

  if (!pacienteId) {
    $('no-patient').style.display = 'block';
    return;
  }

  $('no-patient').style.display = 'none';
  await Promise.all([carregarPaciente(), carregarAnamneses(), carregarPlanos(), carregarFisios(), carregarEvolucoes(), carregarSessoesPaciente()]);
  $('patient-header').style.display  = 'flex';
  $('prontuario-content').style.display = 'block';

  bindForm();
  bindFormPlano();
  bindFormEvolucao();
});

// ── Dados ─────────────────────────────────────────────────────────────────────
async function carregarPaciente() {
  try {
    const res = await fetch(`${API_BASE}/pacientes/${pacienteId}`, {
      headers: { Authorization: `Bearer ${token()}` },
    });
    if (!res.ok) throw new Error();
    const p = await res.json();
    renderCabecalhoPaciente(p);
  } catch {
    showToast('Não foi possível carregar os dados do paciente.', 'error');
  }
}

async function carregarAnamneses() {
  try {
    anamneses = await listarAnamneses(pacienteId);
  } catch {
    anamneses = [];
  }
  renderAnamneses();
}

async function carregarFisios() {
  try {
    const res = await fetch(`${API_BASE}/fisioterapeutas`, {
      headers: { Authorization: `Bearer ${token()}` },
    });
    if (res.ok) fisios = await res.json();
  } catch { /* silencioso */ }
  popularSelectFisio();
  popularSelectFisioPlano();
}

async function carregarPlanos() {
  try {
    planos = await listarPlanos(pacienteId);
  } catch {
    planos = [];
  }
  renderPlanos();
}

async function carregarEvolucoes() {
  try {
    evolucoes = await listarEvolucoesPaciente(pacienteId);
  } catch {
    evolucoes = [];
  }
  renderEvolucoes();
}

async function carregarSessoesPaciente() {
  try {
    sessoesPaciente = await listarSessoes({ paciente_id: pacienteId });
  } catch {
    sessoesPaciente = [];
  }
}

// ── Render: cabeçalho do paciente ─────────────────────────────────────────────
function renderCabecalhoPaciente(p) {
  const nome = p.nomeCompleto ?? p.nome_completo ?? 'Paciente';
  $('ph-avatar').textContent   = iniciais(nome);
  $('ph-nome').textContent     = nome;
  $('ph-cpf').textContent      = formatCpf(p.cpf);

  if (p.dataNascimento) {
    const idade = calcularIdade(p.dataNascimento);
    $('ph-nasc').textContent = `${formatData(p.dataNascimento)} (${idade} anos)`;
  }

  const pag = p.tipoPagamento === 'convenio'
    ? `Convênio${p.nomeConvenio ? ': ' + p.nomeConvenio : ''}`
    : 'Particular';
  $('ph-pagamento').textContent = pag;

  document.title = `Prontuário — ${nome}`;
}

function calcularIdade(dataNasc) {
  const hoje  = new Date();
  const nasc  = new Date(dataNasc + 'T00:00:00');
  let idade   = hoje.getFullYear() - nasc.getFullYear();
  const m = hoje.getMonth() - nasc.getMonth();
  if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) idade--;
  return idade;
}

// ── Render: lista de anamneses ────────────────────────────────────────────────
function renderAnamneses() {
  const el = $('lista-anamneses');

  if (!anamneses.length) {
    el.innerHTML = `
      <div class="empty-anamnese">
        <svg width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
        </svg>
        <p>Nenhuma avaliação registrada. Clique em <strong>Nova Avaliação</strong> para começar.</p>
      </div>`;
    return;
  }

  el.innerHTML = anamneses.map(a => cartaoAnamnese(a)).join('');
  el.querySelectorAll('.anamnese-card-header').forEach(h => {
    h.addEventListener('click', () => {
      const card = h.closest('.anamnese-card');
      card.classList.toggle('open');
    });
  });
}

function cartaoAnamnese(a) {
  const fisioNome = a.fisioterapeuta?.nome ?? '—';
  const eva       = a.avaliacao_fisica?.eva ?? a.avaliacaoFisica?.eva;
  const postura   = a.avaliacao_fisica?.postura ?? a.avaliacaoFisica?.postura;
  const forca     = a.avaliacao_fisica?.forca_muscular ?? a.avaliacaoFisica?.forca_muscular;
  const testes    = a.avaliacao_fisica?.testes_especiais ?? a.avaliacaoFisica?.testes_especiais;
  const adm       = a.avaliacao_fisica?.adm ?? a.avaliacaoFisica?.adm;
  const gonio     = a.avaliacao_fisica?.goniometria ?? a.avaliacaoFisica?.goniometria;

  return `
    <div class="anamnese-card">
      <div class="anamnese-card-header">
        <span class="anamnese-card-date">${esc(formatData(a.data_avaliacao ?? a.dataAvaliacao))}</span>
        <span class="anamnese-card-fisio">Dr(a). ${esc(fisioNome)}</span>
        <span class="anamnese-card-queixa">${esc(a.queixa_principal ?? a.queixaPrincipal ?? '')}</span>
        <svg class="anamnese-card-chevron" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
      <div class="anamnese-card-body">
        ${campoAnamnese('Queixa principal', a.queixa_principal ?? a.queixaPrincipal)}
        ${campoAnamnese('Histórico da doença atual', a.historico_doenca_atual ?? a.historicoDoencaAtual)}
        ${a.tempo_inicio_sintomas || a.tempoInicioSintomas ? campoAnamnese('Tempo de início dos sintomas', a.tempo_inicio_sintomas ?? a.tempoInicioSintomas) : ''}
        ${a.doencas_preexistentes || a.doencasPreexistentes ? campoAnamnese('Doenças preexistentes', a.doencas_preexistentes ?? a.doencasPreexistentes) : ''}
        ${a.cirurgias_anteriores  || a.cirurgiasAnteriores  ? campoAnamnese('Cirurgias anteriores', a.cirurgias_anteriores  ?? a.cirurgiasAnteriores) : ''}
        ${a.medicamentos ? campoAnamnese('Medicamentos', a.medicamentos) : ''}
        ${a.alergias     ? campoAnamnese('Alergias', a.alergias) : ''}
        ${a.historico_familiar || a.historicoFamiliar ? campoAnamnese('Histórico familiar', a.historico_familiar ?? a.historicoFamiliar) : ''}
        ${eva != null    ? campoAnamnese('Dor (EVA)', `${eva}/10`) : ''}
        ${postura        ? campoAnamnese('Postura', postura) : ''}
        ${adm            ? campoAnamnese('ADM', adm) : ''}
        ${forca          ? campoAnamnese('Força muscular', forca) : ''}
        ${gonio          ? campoAnamnese('Goniometria', gonio) : ''}
        ${testes?.length ? campoAnamnese('Testes especiais', Array.isArray(testes) ? testes.join(' · ') : testes) : ''}
        ${a.observacoes  ? campoAnamnese('Observações', a.observacoes) : ''}
      </div>
    </div>`;
}

function campoAnamnese(label, valor) {
  if (!valor && valor !== 0) return '';
  return `
    <div class="anamnese-field">
      <div class="anamnese-field-label">${esc(label)}</div>
      <div class="anamnese-field-value">${esc(String(valor))}</div>
    </div>`;
}

// ── Select fisioterapeutas ────────────────────────────────────────────────────
function popularSelectFisio() {
  const sel = $('sel-fisio-anamnese');
  sel.innerHTML = '<option value="">Selecione (opcional)</option>'
    + fisios.map(f => `<option value="${esc(f.id)}">${esc(f.nome)} — ${esc(f.crf)}</option>`).join('');
}

function popularSelectFisioPlano() {
  const sel = $('sel-fisio-plano');
  if (!sel) return;
  sel.innerHTML = '<option value="">Selecione (opcional)</option>'
    + fisios.map(f => `<option value="${esc(f.id)}">${esc(f.nome)} — ${esc(f.crf)}</option>`).join('');
}

function popularSelectAnamnese() {
  const sel = $('plano-anamnese');
  if (!sel) return;
  sel.innerHTML = '<option value="">Nenhuma (opcional)</option>'
    + anamneses.map(a => {
        const data = formatData(a.data_avaliacao ?? a.dataAvaliacao);
        const queixa = esc(String(a.queixa_principal ?? a.queixaPrincipal ?? '').slice(0, 50));
        return `<option value="${esc(a.id)}">${data} — ${queixa}</option>`;
      }).join('');
}

// ── Form: show/hide ───────────────────────────────────────────────────────────
function bindForm() {
  $('btn-nova-avaliacao').addEventListener('click', abrirForm);
  $('btn-cancelar-form').addEventListener('click', fecharForm);
  $('btn-salvar-anamnese').addEventListener('click', salvarAnamnese);
}

function abrirForm() {
  limparForm();
  formVisivel = true;
  $('form-nova-avaliacao').classList.add('visible');
  $('btn-nova-avaliacao').style.display = 'none';
  $('inp-queixa').focus();
}

function fecharForm() {
  formVisivel = false;
  $('form-nova-avaliacao').classList.remove('visible');
  $('btn-nova-avaliacao').style.display = '';
}

function limparForm() {
  ['inp-queixa','inp-historico','inp-tempo','inp-alergias','inp-doencas',
   'inp-cirurgias','inp-medicamentos','inp-historico-fam','inp-postura',
   'inp-adm','inp-forca','inp-goniometria','inp-testes','inp-obs'].forEach(id => {
    const el = $(id);
    if (el) el.value = '';
  });
  $('inp-eva').value   = 0;
  $('eva-display').textContent = '0';
  $('sel-fisio-anamnese').value = '';
  $('form-erro').style.display = 'none';
}

// ── EVA slider ────────────────────────────────────────────────────────────────
function bindEva() {
  bindEvaSlider('inp-eva', 'eva-display');
  bindEvaSlider('evol-eva-antes', 'evol-eva-antes-display');
  bindEvaSlider('evol-eva-depois', 'evol-eva-depois-display');
}

function bindEvaSlider(sliderId, displayId) {
  const slider  = $(sliderId);
  const display = $(displayId);
  if (!slider || !display) return;
  slider.addEventListener('input', () => {
    const v = slider.value;
    display.textContent = v;
    const cor = v <= 3 ? 'var(--green)' : v <= 6 ? 'var(--amber)' : 'var(--red)';
    display.style.background = cor;
    display.style.color = '#fff';
  });
}

// ── Tabs ──────────────────────────────────────────────────────────────────────
function bindTabs() {
  document.querySelectorAll('.tab-btn:not([disabled])').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => {
        b.classList.remove('active');
        b.setAttribute('aria-selected', 'false');
      });
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      btn.setAttribute('aria-selected', 'true');
      $('tab-' + btn.dataset.tab)?.classList.add('active');
    });
  });
}

// ── Salvar anamnese ───────────────────────────────────────────────────────────
async function salvarAnamnese() {
  const erroEl = $('form-erro');
  erroEl.style.display = 'none';

  const queixa   = $('inp-queixa').value.trim();
  const historico = $('inp-historico').value.trim();
  if (!queixa || !historico) {
    erroEl.textContent = 'Preencha os campos obrigatórios: Queixa principal e Histórico da doença atual.';
    erroEl.style.display = 'block';
    return;
  }

  const testes = $('inp-testes').value.trim()
    ? $('inp-testes').value.split(',').map(t => t.trim()).filter(Boolean)
    : [];

  const avaliacaoFisica = {};
  const eva     = parseInt($('inp-eva').value, 10);
  const postura = $('inp-postura').value.trim();
  const adm     = $('inp-adm').value.trim();
  const forca   = $('inp-forca').value.trim();
  const gonio   = $('inp-goniometria').value.trim();

  if (eva > 0)      avaliacaoFisica.eva              = eva;
  if (postura)      avaliacaoFisica.postura           = postura;
  if (adm)          avaliacaoFisica.adm               = adm;
  if (forca)        avaliacaoFisica.forca_muscular    = forca;
  if (gonio)        avaliacaoFisica.goniometria       = gonio;
  if (testes.length) avaliacaoFisica.testes_especiais = testes;

  const payload = {
    paciente_id:              pacienteId,
    queixa_principal:         queixa,
    historico_doenca_atual:   historico,
    tempo_inicio_sintomas:    $('inp-tempo').value.trim()          || null,
    doencas_preexistentes:    $('inp-doencas').value.trim()        || null,
    cirurgias_anteriores:     $('inp-cirurgias').value.trim()      || null,
    medicamentos:             $('inp-medicamentos').value.trim()   || null,
    alergias:                 $('inp-alergias').value.trim()       || null,
    historico_familiar:       $('inp-historico-fam').value.trim()  || null,
    observacoes:              $('inp-obs').value.trim()            || null,
    fisioterapeuta_id:        $('sel-fisio-anamnese').value        || null,
    avaliacao_fisica:         Object.keys(avaliacaoFisica).length ? avaliacaoFisica : null,
  };

  const btn     = $('btn-salvar-anamnese');
  const label   = $('btn-salvar-label');
  const spinner = $('btn-salvar-spinner');
  btn.disabled        = true;
  label.style.display = 'none';
  spinner.style.display = 'block';

  try {
    const nova = await criarAnamnese(payload);
    anamneses.unshift(nova);
    renderAnamneses();
    fecharForm();
    showToast('Avaliação registrada com sucesso.', 'success');
  } catch (err) {
    erroEl.textContent = err?.erro ?? err?.message ?? 'Erro ao salvar. Tente novamente.';
    erroEl.style.display = 'block';
  } finally {
    btn.disabled          = false;
    label.style.display   = 'block';
    spinner.style.display = 'none';
  }
}

// ══════════════════════════════════════════════════════════════════════════════
// MÓDULO 3 — Plano de Tratamento
// ══════════════════════════════════════════════════════════════════════════════

const STATUS_PLANO_LABEL = { ativo: 'Ativo', concluido: 'Concluído', cancelado: 'Cancelado' };
const STATUS_PLANO_CLASS = { ativo: 'badge-green', concluido: 'badge-blue', cancelado: 'badge-red' };

// ── Render: lista de planos ───────────────────────────────────────────────────
function renderPlanos() {
  const el = $('lista-planos');
  if (!el) return;

  if (!planos.length) {
    el.innerHTML = `
      <div class="empty-plano">
        <svg width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
        </svg>
        <p>Nenhum plano de tratamento registrado. Clique em <strong>Novo Plano</strong> para começar.</p>
      </div>`;
    return;
  }

  el.innerHTML = planos.map(p => cartaoPlano(p)).join('');

  el.querySelectorAll('.plano-card-header').forEach(h => {
    h.addEventListener('click', () => h.closest('.plano-card').classList.toggle('open'));
  });

  el.querySelectorAll('[data-action="encerrar-plano"]').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      encerrarPlano(btn.dataset.id, 'concluido');
    });
  });

  el.querySelectorAll('[data-action="cancelar-plano"]').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      encerrarPlano(btn.dataset.id, 'cancelado');
    });
  });
}

function cartaoPlano(p) {
  const fisioNome = p.fisioterapeuta?.nome ?? p.fisioterapeuta?.nome_completo ?? '—';
  const tecnicas  = p.tecnicas ?? [];
  const statusLabel = STATUS_PLANO_LABEL[p.status] ?? p.status;
  const statusCls   = STATUS_PLANO_CLASS[p.status] ?? 'badge-gray';
  const dataInicio  = formatData(p.data_inicio ?? p.dataInicio);
  const dataAlta    = p.data_previsao_alta ?? p.dataPrevisaoAlta;

  const acoes = p.status === 'ativo' ? `
    <button class="btn btn-sm btn-secondary" data-action="encerrar-plano" data-id="${esc(p.id)}" title="Marcar como concluído">Concluir</button>
    <button class="btn btn-sm btn-ghost" style="color:var(--red)" data-action="cancelar-plano" data-id="${esc(p.id)}" title="Cancelar plano">Cancelar</button>
  ` : '';

  return `
    <div class="plano-card plano-status-${esc(p.status)}">
      <div class="plano-card-header">
        <span class="plano-card-date">${esc(dataInicio)}</span>
        <span class="plano-card-fisio">Dr(a). ${esc(fisioNome)}</span>
        <span class="badge ${statusCls}">${esc(statusLabel)}</span>
        <span class="plano-card-diag">${esc(String(p.diagnostico_cif ?? p.diagnosticoCif ?? '').slice(0, 80))}</span>
        <svg class="plano-card-chevron" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
      <div class="plano-card-body">
        ${campoPlan('Diagnóstico (CIF)', p.diagnostico_cif ?? p.diagnosticoCif)}
        ${p.cid10 ? campoPlan('CID-10', p.cid10) : ''}
        ${campoPlan('Objetivos de curto prazo', p.objetivos_curto_prazo ?? p.objetivosCurtoPrazo)}
        ${campoPlan('Objetivos de longo prazo', p.objetivos_longo_prazo ?? p.objetivosLongoPrazo)}
        ${p.hipoteses_tratamento || p.hipotesesTratamento ? campoPlan('Hipóteses de tratamento', p.hipoteses_tratamento ?? p.hipotesesTratamento) : ''}
        ${tecnicas.length ? campoPlan('Técnicas', tecnicas.join(' · ')) : ''}
        ${campoPlan('Frequência', `${p.frequencia_semanal ?? p.frequenciaSemanal}× por semana`)}
        ${campoPlan('Sessões estimadas', String(p.num_sessoes_estimado ?? p.numSessoesEstimado))}
        ${dataAlta ? campoPlan('Previsão de alta', formatData(dataAlta)) : ''}
        ${p.anamnese ? campoPlan('Anamnese vinculada', formatData(p.anamnese.data_avaliacao ?? p.anamnese.dataAvaliacao)) : ''}
        ${p.status === 'ativo' ? `<div class="plano-acoes">${acoes}</div>` : ''}
      </div>
    </div>`;
}

function campoPlan(label, valor) {
  if (!valor && valor !== 0) return '';
  return `
    <div class="anamnese-field">
      <div class="anamnese-field-label">${esc(label)}</div>
      <div class="anamnese-field-value">${esc(String(valor))}</div>
    </div>`;
}

// ── Form: show/hide ───────────────────────────────────────────────────────────
function bindFormPlano() {
  const btnNovo    = $('btn-novo-plano');
  const btnCancel  = $('btn-cancelar-plano');
  const btnSalvar  = $('btn-salvar-plano');
  if (!btnNovo) return;

  btnNovo.addEventListener('click', abrirFormPlano);
  btnCancel.addEventListener('click', fecharFormPlano);
  btnSalvar.addEventListener('click', salvarPlano);
}

function abrirFormPlano() {
  limparFormPlano();
  popularSelectAnamnese();
  formPlanoVisivel = true;
  $('form-novo-plano').classList.add('visible');
  $('btn-novo-plano').style.display = 'none';
  $('plano-diagnostico-cif').focus();
}

function fecharFormPlano() {
  formPlanoVisivel = false;
  $('form-novo-plano').classList.remove('visible');
  $('btn-novo-plano').style.display = '';
}

function limparFormPlano() {
  ['plano-diagnostico-cif','plano-cid10','plano-hipoteses',
   'plano-obj-curto','plano-obj-longo','plano-num-sessoes','plano-previsao-alta'].forEach(id => {
    const el = $(id);
    if (el) el.value = '';
  });
  $('plano-frequencia').value = '';
  $('sel-fisio-plano').value  = '';
  $('plano-tcle').checked     = false;
  $('plano-anamnese').value   = '';
  document.querySelectorAll('#tecnicas-chips input[type="checkbox"]')
    .forEach(cb => { cb.checked = false; });
  $('plano-form-erro').style.display = 'none';
}

// ── Salvar plano ──────────────────────────────────────────────────────────────
async function salvarPlano() {
  const erroEl = $('plano-form-erro');
  erroEl.style.display = 'none';

  const diagnosticoCif     = $('plano-diagnostico-cif').value.trim();
  const objetivosCurto     = $('plano-obj-curto').value.trim();
  const objetivosLongo     = $('plano-obj-longo').value.trim();
  const frequencia         = $('plano-frequencia').value;
  const numSessoes         = $('plano-num-sessoes').value.trim();

  if (!diagnosticoCif || !objetivosCurto || !objetivosLongo || !frequencia || !numSessoes) {
    erroEl.textContent = 'Preencha os campos obrigatórios: Diagnóstico (CIF), Objetivos, Frequência e Nº de sessões.';
    erroEl.style.display = 'block';
    return;
  }

  const numSessoesInt = parseInt(numSessoes, 10);
  if (isNaN(numSessoesInt) || numSessoesInt < 1) {
    erroEl.textContent = 'Número de sessões deve ser um valor inteiro maior que zero.';
    erroEl.style.display = 'block';
    return;
  }

  const tecnicas = Array.from(
    document.querySelectorAll('#tecnicas-chips input[type="checkbox"]:checked')
  ).map(cb => cb.value);

  const previsaoAlta = $('plano-previsao-alta').value || null;

  const payload = {
    paciente_id:              pacienteId,
    diagnostico_cif:          diagnosticoCif,
    cid10:                    $('plano-cid10').value.trim()     || null,
    hipoteses_tratamento:     $('plano-hipoteses').value.trim() || null,
    objetivos_curto_prazo:    objetivosCurto,
    objetivos_longo_prazo:    objetivosLongo,
    tecnicas:                 tecnicas.length ? tecnicas : null,
    frequencia_semanal:       parseInt(frequencia, 10),
    num_sessoes_estimado:     numSessoesInt,
    data_previsao_alta:       previsaoAlta,
    fisioterapeuta_id:        $('sel-fisio-plano').value        || null,
    anamnese_id:              $('plano-anamnese').value         || null,
  };

  const btn     = $('btn-salvar-plano');
  const label   = $('btn-salvar-plano-label');
  const spinner = $('btn-salvar-plano-spinner');
  btn.disabled        = true;
  label.style.display = 'none';
  spinner.style.display = 'block';

  try {
    const novo = await criarPlano(payload);
    planos.unshift(novo);
    renderPlanos();
    fecharFormPlano();
    showToast('Plano de tratamento registrado com sucesso.', 'success');
  } catch (err) {
    erroEl.textContent = err?.erro ?? err?.message ?? 'Erro ao salvar. Tente novamente.';
    erroEl.style.display = 'block';
  } finally {
    btn.disabled          = false;
    label.style.display   = 'block';
    spinner.style.display = 'none';
  }
}

// ── Encerrar / cancelar plano ─────────────────────────────────────────────────
async function encerrarPlano(id, novoStatus) {
  const msg = novoStatus === 'concluido'
    ? 'Marcar este plano como concluído?'
    : 'Cancelar este plano de tratamento?';
  if (!confirm(msg)) return;

  try {
    const atualizado = await atualizarStatusPlano(id, novoStatus);
    const idx = planos.findIndex(p => p.id === id);
    if (idx !== -1) planos[idx] = atualizado;
    renderPlanos();
    showToast(novoStatus === 'concluido' ? 'Plano concluído.' : 'Plano cancelado.', 'success');
  } catch {
    showToast('Erro ao atualizar o status do plano.', 'error');
  }
}

// ══════════════════════════════════════════════════════════════════════════════
// MÓDULO 5 — Evolução Clínica (SOAP)
// ══════════════════════════════════════════════════════════════════════════════

function formatDataHora(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

// ── Render: lista de evoluções ────────────────────────────────────────────────
function renderEvolucoes() {
  const el = $('lista-evolucoes');
  if (!el) return;

  if (!evolucoes.length) {
    el.innerHTML = `
      <div class="empty-evolucao">
        <svg width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
        </svg>
        <p>Nenhuma evolução registrada. Clique em <strong>Nova Evolução</strong> para começar.</p>
      </div>`;
    return;
  }

  el.innerHTML = evolucoes.map(e => cartaoEvolucao(e)).join('');
  el.querySelectorAll('.evol-card-header').forEach(h => {
    h.addEventListener('click', () => h.closest('.evol-card').classList.toggle('open'));
  });
}

function cartaoEvolucao(e) {
  const fisioNome = e.fisioterapeuta?.nome ?? '—';
  const evaAntes  = e.eva_antes  ?? e.evaAntes;
  const evaDepois = e.eva_depois ?? e.evaDepois;
  const tecnicas  = e.tecnicas_realizadas ?? e.tecnicasRealizadas ?? [];
  const num       = e.num_sessao ?? e.numSessao ?? '—';
  const dataHora  = e.data_hora  ?? e.dataHora;
  const tempo     = e.tempo_atendimento_min ?? e.tempoAtendimentoMin;

  const evaTag = (val, label) => {
    if (val == null) return '';
    const cor = val <= 3 ? 'badge-green' : val <= 6 ? 'badge-amber' : 'badge-red';
    return `<span class="badge ${cor}" title="${label}">EVA ${label}: ${val}/10</span>`;
  };

  return `
    <div class="evol-card">
      <div class="evol-card-header">
        <span class="evol-card-num">Sessão ${esc(String(num))}</span>
        <span class="evol-card-data">${esc(formatDataHora(dataHora))}</span>
        <span class="evol-card-fisio">Dr(a). ${esc(fisioNome)}</span>
        <div class="evol-card-eva">
          ${evaTag(evaAntes, 'antes')}
          ${evaTag(evaDepois, 'após')}
        </div>
        <svg class="evol-card-chevron" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
      <div class="evol-card-body">
        <div class="soap-display">
          ${soapBloco('S', 'Subjetivo', e.subjetivo)}
          ${soapBloco('O', 'Objetivo',  e.objetivo)}
          ${soapBloco('A', 'Avaliação', e.avaliacao)}
          ${soapBloco('P', 'Plano',     e.plano_evolucao ?? e.planoEvolucao)}
        </div>
        ${tecnicas.length ? campoEvol('Técnicas realizadas', tecnicas.join(' · ')) : ''}
        ${e.aparelhos ? campoEvol('Aparelhos e parâmetros', e.aparelhos) : ''}
        ${tempo ? campoEvol('Tempo de atendimento', `${tempo} min`) : ''}
        ${e.codigo_tuss || e.codigoTuss ? campoEvol('Código TUSS', e.codigo_tuss ?? e.codigoTuss) : ''}
        ${e.observacoes ? campoEvol('Observações', e.observacoes) : ''}
      </div>
    </div>`;
}

function soapBloco(letra, titulo, texto) {
  if (!texto) return '';
  const cor = { S: 'soap-s', O: 'soap-o', A: 'soap-a', P: 'soap-p' }[letra] ?? '';
  return `
    <div class="soap-display-row">
      <span class="soap-letter ${cor}">${esc(letra)}</span>
      <div>
        <div class="anamnese-field-label">${esc(titulo)}</div>
        <div class="anamnese-field-value" style="margin-top:4px">${esc(String(texto))}</div>
      </div>
    </div>`;
}

function campoEvol(label, valor) {
  if (!valor && valor !== 0) return '';
  return `
    <div class="anamnese-field">
      <div class="anamnese-field-label">${esc(label)}</div>
      <div class="anamnese-field-value">${esc(String(valor))}</div>
    </div>`;
}

// ── Form: show/hide ───────────────────────────────────────────────────────────
function bindFormEvolucao() {
  const btnNovo   = $('btn-nova-evolucao');
  const btnCancel = $('btn-cancelar-evolucao');
  const btnSalvar = $('btn-salvar-evolucao');
  if (!btnNovo) return;

  btnNovo.addEventListener('click', abrirFormEvolucao);
  btnCancel.addEventListener('click', fecharFormEvolucao);
  btnSalvar.addEventListener('click', salvarEvolucao);
}

function abrirFormEvolucao() {
  limparFormEvolucao();
  popularSelectSessoes();
  popularSelectFisioEvol();
  popularSelectPlanoEvol();
  formEvolVisivel = true;
  $('form-nova-evolucao').classList.add('visible');
  $('btn-nova-evolucao').style.display = 'none';
}

function fecharFormEvolucao() {
  formEvolVisivel = false;
  $('form-nova-evolucao').classList.remove('visible');
  $('btn-nova-evolucao').style.display = '';
}

function limparFormEvolucao() {
  ['evol-num-sessao','evol-tempo','evol-subjetivo','evol-objetivo',
   'evol-avaliacao','evol-plano-evolucao','evol-aparelhos','evol-codigo-tuss','evol-obs'].forEach(id => {
    const el = $(id);
    if (el) el.value = '';
  });
  $('evol-sessao').value           = '';
  $('sel-fisio-evol').value        = '';
  $('evol-plano').value            = '';
  $('evol-eva-antes').value        = 0;
  $('evol-eva-depois').value       = 0;
  $('evol-eva-antes-display').textContent  = '0';
  $('evol-eva-depois-display').textContent = '0';
  $('evol-eva-antes-display').style.background  = '';
  $('evol-eva-depois-display').style.background = '';
  document.querySelectorAll('#evol-tecnicas-chips input[type="checkbox"]')
    .forEach(cb => { cb.checked = false; });
  $('evol-form-erro').style.display = 'none';
}

// ── Selects auxiliares ────────────────────────────────────────────────────────
function popularSelectSessoes() {
  const sel = $('evol-sessao');
  if (!sel) return;
  const sessoesSemEvol = sessoesPaciente.filter(s =>
    s.status === 'REALIZADO' &&
    !evolucoes.some(e => (e.sessao?.id ?? e.sessao_id) === s.id)
  );
  if (!sessoesSemEvol.length) {
    sel.innerHTML = '<option value="">Nenhuma sessão realizada disponível</option>';
    return;
  }
  sel.innerHTML = '<option value="">Selecione a sessão</option>'
    + sessoesSemEvol.map(s => {
        const dt = formatDataHora(s.data_hora_inicio ?? s.dataHoraInicio);
        const tipo = tipoSessaoLabel(s.tipo_sessao ?? s.tipoSessao);
        return `<option value="${esc(s.id)}">${esc(dt)} — ${esc(tipo)}</option>`;
      }).join('');
}

function tipoSessaoLabel(tipo) {
  const labels = { AVALIACAO: 'Avaliação', SESSAO: 'Sessão', REAVALIACAO: 'Reavaliação', ALTA: 'Alta' };
  return labels[tipo] ?? tipo ?? '—';
}

function popularSelectFisioEvol() {
  const sel = $('sel-fisio-evol');
  if (!sel) return;
  sel.innerHTML = '<option value="">Selecione (opcional)</option>'
    + fisios.map(f => `<option value="${esc(f.id)}">${esc(f.nome)} — ${esc(f.crf)}</option>`).join('');
}

function popularSelectPlanoEvol() {
  const sel = $('evol-plano');
  if (!sel) return;
  const planosAtivos = planos.filter(p => p.status === 'ativo');
  if (!planosAtivos.length) {
    sel.innerHTML = '<option value="">Nenhum plano ativo</option>';
    return;
  }
  sel.innerHTML = '<option value="">Nenhum (opcional)</option>'
    + planosAtivos.map(p => {
        const data = formatData(p.data_inicio ?? p.dataInicio);
        const diag = esc(String(p.diagnostico_cif ?? p.diagnosticoCif ?? '').slice(0, 50));
        return `<option value="${esc(p.id)}">${data} — ${diag}</option>`;
      }).join('');
}

// ── Salvar evolução ───────────────────────────────────────────────────────────
async function salvarEvolucao() {
  const erroEl = $('evol-form-erro');
  erroEl.style.display = 'none';

  const sessaoId    = $('evol-sessao').value;
  const numSessao   = $('evol-num-sessao').value.trim();
  const subjetivo   = $('evol-subjetivo').value.trim();
  const objetivo    = $('evol-objetivo').value.trim();
  const avaliacao   = $('evol-avaliacao').value.trim();
  const planoEvol   = $('evol-plano-evolucao').value.trim();

  if (!sessaoId || !numSessao || !subjetivo || !objetivo || !avaliacao || !planoEvol) {
    erroEl.textContent = 'Preencha os campos obrigatórios: Sessão, Nº da sessão e os quatro campos SOAP.';
    erroEl.style.display = 'block';
    return;
  }

  const numSessaoInt = parseInt(numSessao, 10);
  if (isNaN(numSessaoInt) || numSessaoInt < 1) {
    erroEl.textContent = 'Nº da sessão deve ser um valor inteiro positivo.';
    erroEl.style.display = 'block';
    return;
  }

  const tecnicas = Array.from(
    document.querySelectorAll('#evol-tecnicas-chips input[type="checkbox"]:checked')
  ).map(cb => cb.value);

  const evaAntes  = parseInt($('evol-eva-antes').value,  10);
  const evaDepois = parseInt($('evol-eva-depois').value, 10);

  const payload = {
    sessao_id:             sessaoId,
    num_sessao:            numSessaoInt,
    fisioterapeuta_id:     $('sel-fisio-evol').value  || null,
    plano_tratamento_id:   $('evol-plano').value      || null,
    tempo_atendimento_min: parseInt($('evol-tempo').value, 10) || null,
    subjetivo,
    objetivo,
    avaliacao,
    plano_evolucao:        planoEvol,
    tecnicas_realizadas:   tecnicas.length ? tecnicas : null,
    aparelhos:             $('evol-aparelhos').value.trim()    || null,
    eva_antes:             evaAntes,
    eva_depois:            evaDepois,
    codigo_tuss:           $('evol-codigo-tuss').value.trim()  || null,
    observacoes:           $('evol-obs').value.trim()          || null,
  };

  const btn     = $('btn-salvar-evolucao');
  const label   = $('btn-salvar-evol-label');
  const spinner = $('btn-salvar-evol-spinner');
  btn.disabled        = true;
  label.style.display = 'none';
  spinner.style.display = 'block';

  try {
    const nova = await criarEvolucao(payload);
    evolucoes.unshift(nova);
    renderEvolucoes();
    fecharFormEvolucao();
    showToast('Evolução registrada com sucesso.', 'success');
  } catch (err) {
    erroEl.textContent = err?.erro ?? err?.message ?? 'Erro ao salvar. Tente novamente.';
    erroEl.style.display = 'block';
  } finally {
    btn.disabled          = false;
    label.style.display   = 'block';
    spinner.style.display = 'none';
  }
}
