// ─────────────────────────────────────────────────────────────────────────────
// pages/prontuario.js — Lógica do prontuário eletrônico (Módulos 2, 3 e 5)
// ─────────────────────────────────────────────────────────────────────────────
// Acessado via prontuario.html?paciente_id=UUID
// Se paciente_id estiver ausente na URL, exibe #no-patient e para a execução.
//
// Inicialização (DOMContentLoaded):
//   1. initTopbar() — topbar com notificações
//   2. bindTabs()   — troca de abas (Anamnese / Plano / Evolução)
//   3. bindEva()    — slider de dor EVA 0–10 com atualização visual em tempo real
//   4. Lê paciente_id da URL e carrega em paralelo:
//       carregarPaciente()       → GET /api/pacientes/{id} → renderCabecalhoPaciente()
//       carregarAnamneses()      → GET /api/anamneses?paciente_id
//       carregarPlanos()         → GET /api/planos?paciente_id
//       carregarFisios()         → GET /api/fisioterapeutas (para selects)
//       carregarEvolucoes()      → GET /api/evolucoes/paciente/{id}
//       carregarSessoesPaciente()→ GET /api/sessoes?paciente_id (para vincular evolução)
//
// Aba Anamnese (M2):
//   - Lista anamneses existentes com data e queixa principal
//   - Formulário expansível para nova anamnese com campo de avaliação física
//   - bindForm() conecta submit → criarAnamnese() → atualiza lista
//
// Aba Plano de Tratamento (M3):
//   - Exibe plano ativo com status (ativo/concluido/cancelado)
//   - Botão de conclusão → atualizarStatusPlano()
//   - bindFormPlano() → criarPlano()
//
// Aba Evolução SOAP (M5):
//   - Lista evoluções com campos S/O/A/P
//   - Select de sessão (apenas sessões REALIZADO sem evolução registrada)
//   - bindFormEvolucao() → criarEvolucao()
//
// Estado local: pacienteId, anamneses[], planos[], evolucoes[],
//               sessoesPaciente[], fisios[], formVisivel, formPlanoVisivel, formEvolVisivel
// ─────────────────────────────────────────────────────────────────────────────

import { initTopbar } from '../utils/auth.js';
import { showToast } from '../components/toast.js';
import { listarAnamneses, criarAnamnese, listarArquivos, uploadArquivo, deletarArquivo } from '../api/anamneses.js';
import { listarPlanos, criarPlano, atualizarStatusPlano, registrarTcle, listarTclesPlano } from '../api/planos.js';
import { listarEvolucoesPaciente, criarEvolucao } from '../api/evolucoes.js';
import { listarSessoes } from '../api/sessoes.js';
// [M6] API de altas reutilizada da página standalone alta.js
import { criarAlta, listarAltasPaciente } from '../api/altas.js';

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
// [M6] Flags da aba Alta — lazy-load e controle de envio
let altaTabCarregada  = false;
let salvandoAlta      = false;
// [M2] Cache de arquivos por anamnese — carregado sob demanda na abertura do card
let arquivosAnamnese  = {};

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
  // [M6] Atualizar link "Registrar Alta" com paciente_id atual
  const btnAltaLink = $('btn-registrar-alta');
  if (btnAltaLink) btnAltaLink.href = `alta.html?paciente_id=${pacienteId}`;
  await Promise.all([carregarPaciente(), carregarAnamneses(), carregarPlanos(), carregarFisios(), carregarEvolucoes(), carregarSessoesPaciente()]);
  $('patient-header').style.display  = 'flex';
  $('prontuario-content').style.display = 'block';

  bindForm();
  bindFormPlano();
  bindFormEvolucao();
  // [M6] Bind das interações da aba Alta (seções colapsáveis + estrelas + submit)
  bindFormAlta();
  bindSatisfacaoStarsTab();
  bindSectionsTab();
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
  // [M3] Verifica TCLE para cada plano em paralelo para exibir o badge de consentimento
  await Promise.all(planos.map(async p => {
    try {
      const termos = await listarTclesPlano(p.id);
      p._tcle = termos.length > 0;
    } catch {
      p._tcle = false;
    }
  }));
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
      const card   = h.closest('.anamnese-card');
      const aberto = card.classList.contains('open');
      card.classList.toggle('open');
      // [M2] Lazy-load: carrega arquivos na primeira abertura do card
      if (!aberto && !card.dataset.arquivosCarregados) {
        card.dataset.arquivosCarregados = '1';
        carregarArquivosCard(card.dataset.anamneseId);
      }
    });
  });

  // [M2] Delegação: upload de arquivo ao selecionar no input oculto
  el.addEventListener('change', e => {
    const inp = e.target.closest('.arq-file-input');
    if (!inp) return;
    uploadArquivosAnamnese(inp.dataset.anamneseId, inp.files);
  });

  // [M2] Delegação: botão de remoção — stopPropagation para não fechar o card
  el.addEventListener('click', e => {
    const btn = e.target.closest('[data-action="deletar-arquivo"]');
    if (!btn) return;
    e.stopPropagation();
    deletarArquivoAnamnese(btn.dataset.anamneseId, btn.dataset.arquivoId);
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
    <div class="anamnese-card" data-anamnese-id="${esc(a.id)}">
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
        <div class="arq-wrap">
          <div class="arq-toolbar">
            <span class="anamnese-field-label" style="margin:0">Arquivos / Exames</span>
            <label class="btn btn-ghost btn-sm arq-add-btn" for="arq-inp-${esc(a.id)}" title="Adicionar arquivo">
              <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Adicionar
            </label>
            <input type="file" id="arq-inp-${esc(a.id)}" class="arq-file-input" data-anamnese-id="${esc(a.id)}" hidden multiple accept=".pdf,.jpg,.jpeg,.png,.doc,.docx">
          </div>
          <div id="arq-lista-${esc(a.id)}" class="arq-lista">
            <span class="arq-placeholder">Abrindo arquivos...</span>
          </div>
        </div>
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

// ── M2: Arquivos da anamnese ──────────────────────────────────────────────────

async function carregarArquivosCard(anamneseId) {
  const lista = $(`arq-lista-${anamneseId}`);
  if (!lista) return;
  try {
    const arquivos = await listarArquivos(anamneseId);
    arquivosAnamnese[anamneseId] = arquivos;
    renderArquivosCard(anamneseId);
  } catch {
    if (lista) lista.innerHTML = '<span class="arq-placeholder" style="color:var(--red)">Erro ao carregar arquivos.</span>';
  }
}

function renderArquivosCard(anamneseId) {
  const lista = $(`arq-lista-${anamneseId}`);
  if (!lista) return;
  const arquivos = arquivosAnamnese[anamneseId] ?? [];
  if (!arquivos.length) {
    lista.innerHTML = '<span class="arq-placeholder">Nenhum arquivo. Clique em <strong>Adicionar</strong> para enviar laudos ou exames.</span>';
    return;
  }
  lista.innerHTML = arquivos.map(arq => {
    const nome = esc(arq.nome_arquivo ?? arq.nomeArquivo ?? 'arquivo');
    const tam  = formatBytes(arq.tamanho_bytes ?? arq.tamanhoBytes);
    return `
      <div class="arq-item">
        <svg class="arq-icon" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
        </svg>
        <span class="arq-nome" title="${nome}">${nome}</span>
        ${tam ? `<span class="arq-tamanho">${tam}</span>` : ''}
        <button type="button" class="btn btn-ghost btn-sm btn-icon arq-btn-download"
          data-url="${esc(arq.url)}" data-nome="${nome}" title="Baixar arquivo">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
        </button>
        <button type="button" class="btn btn-ghost btn-sm btn-icon"
          data-action="deletar-arquivo" data-anamnese-id="${esc(anamneseId)}" data-arquivo-id="${esc(arq.id)}"
          title="Remover arquivo" style="color:var(--red)">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
            <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
          </svg>
        </button>
      </div>`;
  }).join('');

  // Download autenticado (token JWT no header, não exposto na URL)
  lista.querySelectorAll('.arq-btn-download').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      downloadArquivo(btn.dataset.url, btn.dataset.nome);
    });
  });
}

function formatBytes(bytes) {
  if (!bytes) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1048576).toFixed(1)} MB`;
}

async function uploadArquivosAnamnese(anamneseId, files) {
  if (!files || !files.length) return;
  const lista = $(`arq-lista-${anamneseId}`);
  if (lista) lista.innerHTML = '<span class="arq-placeholder">Enviando...</span>';
  let erros = 0;
  for (const file of Array.from(files)) {
    try {
      const fd = new FormData();
      fd.append('arquivo', file);
      const arq = await uploadArquivo(anamneseId, fd);
      if (!arquivosAnamnese[anamneseId]) arquivosAnamnese[anamneseId] = [];
      arquivosAnamnese[anamneseId].push(arq);
    } catch { erros++; }
  }
  renderArquivosCard(anamneseId);
  const inp = $(`arq-inp-${anamneseId}`);
  if (inp) inp.value = '';
  if (erros) showToast(`${erros} arquivo(s) não puderam ser enviados.`, 'error');
  else showToast('Arquivo(s) enviado(s) com sucesso.', 'success');
}

async function deletarArquivoAnamnese(anamneseId, arquivoId) {
  if (!confirm('Remover este arquivo? Esta ação não pode ser desfeita.')) return;
  try {
    await deletarArquivo(anamneseId, arquivoId);
    arquivosAnamnese[anamneseId] = (arquivosAnamnese[anamneseId] ?? []).filter(a => a.id !== arquivoId);
    renderArquivosCard(anamneseId);
    showToast('Arquivo removido.', 'success');
  } catch {
    showToast('Erro ao remover o arquivo.', 'error');
  }
}

// [M2] Download autenticado: busca o arquivo com JWT no header e cria Blob local
async function downloadArquivo(urlPath, nomeArquivo) {
  try {
    const res = await fetch(`http://localhost:8080${urlPath}`, {
      headers: { Authorization: `Bearer ${token()}` },
    });
    if (!res.ok) throw new Error();
    const blob = await res.blob();
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = nomeArquivo;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch {
    showToast('Erro ao baixar o arquivo.', 'error');
  }
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

      // [M6] Lazy-load da aba Alta: carrega dados apenas na primeira ativação
      if (btn.dataset.tab === 'alta' && pacienteId && !altaTabCarregada) {
        carregarAltaTab();
      }
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
        ${p._tcle ? '<span class="badge badge-blue" title="Termo de Consentimento Livre e Esclarecido assinado">TCLE ✓</span>' : ''}
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

    // [M3] Se TCLE foi marcado, registra o Termo de Consentimento vinculado ao plano recém-criado
    if ($('plano-tcle').checked) {
      try {
        await registrarTcle({
          paciente_id: pacienteId,
          plano_id: novo.id,
          tipo: 'TCLE',
          assinado_em: new Date().toISOString().slice(0, 19), // LocalDateTime sem timezone
        });
        novo._tcle = true;
      } catch {
        showToast('Plano salvo, mas houve erro ao registrar o TCLE.', 'warning');
      }
    }

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

// ══════════════════════════════════════════════════════════════════════════════
// MÓDULO 6 — Alta e Relatórios (aba integrada no prontuário)
// Consume dados já carregados: planos[], fisios[], sessoesPaciente[], evolucoes[]
// Registra alta via POST /api/altas; exibe feedback inline (sem redirecionamento).
// ══════════════════════════════════════════════════════════════════════════════

// ── Carregamento inicial da aba Alta ──────────────────────────────────────────
async function carregarAltaTab() {
  altaTabCarregada = true;

  // Preenche resumo do tratamento com dados já carregados
  preencherResumoTab();

  // Popula selects com dados já na memória (planos[] e fisios[])
  popularSelectPlanosTab();
  popularSelectFisioTab();

  // Data padrão = hoje
  const hoje = new Date().toISOString().split('T')[0];
  if ($('tab-inp-data-alta')) $('tab-inp-data-alta').value = hoje;

  // Pré-seleciona plano_id se informado na URL
  const planoIdUrl = new URLSearchParams(window.location.search).get('plano_id');
  if (planoIdUrl && $('tab-inp-plano')) $('tab-inp-plano').value = planoIdUrl;

  // Busca altas existentes e exibe card se houver
  try {
    const altas = await listarAltasPaciente(pacienteId);
    if (altas.length > 0) mostrarAltaTabRegistrada(altas[0]);
  } catch { /* silencioso — não bloqueia o formulário */ }
}

// ── Popula o resumo de sessões/evoluções na aba Alta ─────────────────────────
function preencherResumoTab() {
  const realizadas = sessoesPaciente.filter(s =>
    (s.status ?? '').toUpperCase() === 'REALIZADO'
  );
  if ($('res-tab-sessoes')) $('res-tab-sessoes').textContent = realizadas.length;

  // Dias de tratamento: distância entre primeira sessão realizada e hoje
  if (realizadas.length) {
    const datas = realizadas
      .map(s => new Date(s.data_hora_inicio ?? s.dataHoraInicio ?? s.dataHora))
      .filter(d => !isNaN(d));
    if (datas.length) {
      const primeira = new Date(Math.min(...datas));
      const hoje     = new Date();
      const dias     = Math.round((hoje - primeira) / 86400000);
      if ($('res-tab-dias')) $('res-tab-dias').textContent = dias;
    }
  }

  // Sessões com EVA registrado (antes ou depois)
  const comEva = evolucoes.filter(e => (e.eva_antes ?? e.evaAntes) > 0 || (e.eva_depois ?? e.evaDepois) > 0);
  if ($('res-tab-eva')) $('res-tab-eva').textContent = comEva.length;

  // Diagnóstico CIF do plano ativo
  const planoAtivo = planos.find(p => p.status === 'ativo');
  const diagEl = $('res-tab-diagnostico');
  if (diagEl && planoAtivo) {
    const cif = planoAtivo.diagnostico_cif ?? planoAtivo.diagnosticoCif;
    if (cif) {
      diagEl.textContent = `Diagnóstico CIF: ${cif}`;
      diagEl.style.display = 'block';
    }
  }

  if ($('resumo-wrap-tab')) $('resumo-wrap-tab').style.display = 'block';
}

// ── Popula selects da aba Alta ────────────────────────────────────────────────
function popularSelectPlanosTab() {
  const sel = $('tab-inp-plano');
  if (!sel) return;
  if (!planos.length) return;
  sel.innerHTML = '<option value="">— Nenhum (sem plano formal) —</option>'
    + planos.map(p => {
        const cif  = String(p.diagnostico_cif ?? p.diagnosticoCif ?? '').slice(0, 50);
        const stat = p.status ?? '';
        return `<option value="${esc(p.id)}">${esc(cif)} (${esc(stat)})</option>`;
      }).join('');
}

function popularSelectFisioTab() {
  const sel = $('tab-inp-fisio-alta');
  if (!sel) return;
  if (!fisios.length) return;
  sel.innerHTML = '<option value="">— Selecione —</option>'
    + fisios.map(f => `<option value="${esc(f.id)}">${esc(f.nome)} (CRF ${esc(f.crf)})</option>`).join('');
}

// ── Exibe o card de alta já registrada ───────────────────────────────────────
function mostrarAltaTabRegistrada(alta) {
  const motivos = {
    alta_clinica: 'Alta Clínica', alta_administrativa: 'Alta Administrativa',
    desistencia: 'Desistência', encaminhamento: 'Encaminhamento', obito: 'Óbito',
  };
  if ($('ar-tab-data'))      $('ar-tab-data').textContent    = formatData(alta.data_alta ?? alta.dataAlta);
  if ($('ar-tab-motivo'))    $('ar-tab-motivo').textContent  = motivos[alta.motivo] ?? alta.motivo ?? '—';
  if ($('ar-tab-resultado')) $('ar-tab-resultado').textContent = alta.resultado_objetivos ?? alta.resultadoObjetivos ?? '—';
  if ($('alta-tab-registrada')) $('alta-tab-registrada').style.display = 'block';
}

// ── Seções colapsáveis da aba Alta ────────────────────────────────────────────
function bindSectionsTab() {
  document.querySelectorAll('#tab-alta .alta-section-header').forEach(header => {
    header.addEventListener('click', () => {
      const secId = header.dataset.section;
      if (!secId) return;
      const sec = $(secId);
      if (sec) sec.classList.toggle('open');
    });
  });
}

// ── Estrelas de satisfação da aba Alta ────────────────────────────────────────
// [M6] Mesmo padrão de alta.js: radio + label separados dentro de .satisfacao-star
function bindSatisfacaoStarsTab() {
  const stars = document.querySelectorAll('#tab-satisfacao-stars .satisfacao-star input[type="radio"]');
  stars.forEach(input => {
    input.addEventListener('change', () => {
      const nota = parseInt(input.value, 10);
      stars.forEach((s, idx) => {
        s.nextElementSibling.classList.toggle('ativa', idx < nota);
      });
    });
  });
}

// ── Submit do formulário de alta ─────────────────────────────────────────────
function bindFormAlta() {
  const btn = $('btn-registrar-alta-tab');
  if (!btn) return;
  btn.addEventListener('click', registrarAlta);
}

async function registrarAlta() {
  if (salvandoAlta) return;

  // Validação dos campos obrigatórios
  const motivo    = document.querySelector('input[name="tab-motivo"]:checked')?.value;
  const resultado = $('tab-inp-resultado')?.value.trim();

  const errMotivo    = $('tab-err-motivo');
  const errResultado = $('tab-err-resultado');
  if (errMotivo)    errMotivo.textContent    = '';
  if (errResultado) errResultado.textContent = '';

  let invalido = false;
  if (!motivo) {
    if (errMotivo) errMotivo.textContent = 'Selecione o motivo da alta.';
    $('sec-tab-dados')?.classList.add('open');
    invalido = true;
  }
  if (!resultado) {
    if (errResultado) errResultado.textContent = 'Descreva o resultado vs objetivos.';
    $('sec-tab-dados')?.classList.add('open');
    invalido = true;
  }
  if (invalido) return;

  // Monta payload com todos os campos do Módulo 6
  const notaRaw = document.querySelector('input[name="tab-satisfacao"]:checked')?.value;
  const payload = {
    paciente_id:              pacienteId,
    motivo,
    data_alta:                $('tab-inp-data-alta')?.value       || null,
    plano_id:                 $('tab-inp-plano')?.value           || null,
    fisioterapeuta_id:        $('tab-inp-fisio-alta')?.value      || null,
    resultado_objetivos:      resultado,
    orientacoes_domiciliares: $('tab-inp-orientacoes')?.value.trim()   || null,
    relatorio_evolucao:       $('tab-inp-rel-evolucao')?.value.trim()  || null,
    relatorio_medico:         $('tab-inp-rel-medico')?.value.trim()    || null,
    agendamento_retorno:      $('tab-inp-retorno')?.value              || null,
    satisfacao_nota:          notaRaw ? parseInt(notaRaw, 10) : null,
    satisfacao_comentario:    $('tab-inp-sat-comentario')?.value.trim() || null,
  };

  const btn = $('btn-registrar-alta-tab');
  salvandoAlta    = true;
  btn.disabled    = true;
  btn.textContent = 'Registrando...';

  try {
    const alta = await criarAlta(payload);
    mostrarAltaTabRegistrada(alta);
    // Feedback inline: oculta formulário e exibe card de sucesso — sem redirecionamento
    if ($('form-alta-tab'))   $('form-alta-tab').style.display   = 'none';
    if ($('alta-tab-footer')) $('alta-tab-footer').style.display = 'none';
    showToast('Alta registrada com sucesso!', 'success');
  } catch (err) {
    const msg = err?.erro ?? err?.message ?? 'Erro ao registrar alta. Tente novamente.';
    showToast(msg, 'error');
    salvandoAlta = false;
    btn.disabled = false;
    btn.innerHTML = `
      <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
        <polyline points="20 6 9 17 4 12"/>
      </svg>
      Registrar Alta`;
  }
}
