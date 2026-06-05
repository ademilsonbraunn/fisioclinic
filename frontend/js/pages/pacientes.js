import { showToast } from '../components/toast.js';
import { openModal, closeModal } from '../components/modal.js';
import * as API from '../api/pacientes.js';

// ── Mock data (usado quando o backend não está disponível) ──────────────────
const MOCK = [
  {
    id: '550e8400-e29b-41d4-a716-446655440001',
    nomeCompleto: 'Maria Aparecida Santos',
    cpf: '12345678901',
    dataNascimento: '1985-03-15',
    sexo: 'feminino',
    estadoCivil: 'casado',
    profissao: 'Professora',
    telefone: '11987654321',
    email: 'maria.santos@email.com',
    cep: '01310100', logradouro: 'Av. Paulista', numero: '1000',
    complemento: 'Apto 42', bairro: 'Bela Vista', cidade: 'São Paulo', uf: 'SP',
    tipoPagamento: 'particular',
    nomeConvenio: null, numCarteirinha: null, validadePlano: null, responsavelFinanceiro: null,
    emergenciaNome: 'João Santos', emergenciaParentesco: 'conjuge', emergenciaTelefone: '11976543210',
    createdAt: '2026-05-10T10:30:00Z',
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440002',
    nomeCompleto: 'Carlos Eduardo Oliveira',
    cpf: '98765432100',
    dataNascimento: '1972-11-28',
    sexo: 'masculino',
    estadoCivil: 'solteiro',
    profissao: 'Engenheiro',
    telefone: '11912345678',
    email: 'carlos.oliveira@empresa.com.br',
    cep: null, logradouro: null, numero: null, complemento: null, bairro: null, cidade: null, uf: null,
    tipoPagamento: 'convenio',
    nomeConvenio: 'Unimed', numCarteirinha: '123456789012345', validadePlano: '2027-12',
    responsavelFinanceiro: null,
    emergenciaNome: 'Ana Oliveira', emergenciaParentesco: 'pai_mae', emergenciaTelefone: '11965432198',
    createdAt: '2026-05-22T14:00:00Z',
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440003',
    nomeCompleto: 'Beatriz Lima Mendes',
    cpf: '45612378900',
    dataNascimento: '1998-07-04',
    sexo: 'feminino',
    estadoCivil: 'solteiro',
    profissao: 'Estudante',
    telefone: '11934567890',
    email: null,
    cep: null, logradouro: null, numero: null, complemento: null, bairro: null, cidade: null, uf: null,
    tipoPagamento: 'convenio',
    nomeConvenio: 'Bradesco Saúde', numCarteirinha: '987654321000001', validadePlano: '2026-12',
    responsavelFinanceiro: 'Roberto Lima',
    emergenciaNome: 'Roberto Lima', emergenciaParentesco: 'pai_mae', emergenciaTelefone: '11945678901',
    createdAt: '2026-06-01T09:15:00Z',
  },
];

// ── State ──────────────────────────────────────────────────────────────────
const state = {
  pacientes: [],
  filtro: { busca: '', tipoPagamento: '' },
  editandoId: null,
  fotoBase64: null,
};

// ── DOM helpers ────────────────────────────────────────────────────────────
const $ = id => document.getElementById(id);

// ── Init ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  await carregarPacientes();
  bindEvents();
  aplicarMascaras();
});

// ── Data ───────────────────────────────────────────────────────────────────
async function carregarPacientes() {
  try {
    state.pacientes = await API.listarPacientes();
  } catch {
    state.pacientes = [...MOCK];
  }
  renderLista();
}

// ── Render ─────────────────────────────────────────────────────────────────
function renderLista() {
  const { busca, tipoPagamento } = state.filtro;
  const termo = busca.trim().toLowerCase();

  const lista = state.pacientes.filter(p => {
    const matchBusca = !termo
      || p.nomeCompleto.toLowerCase().includes(termo)
      || p.cpf.includes(termo.replace(/\D/g, ''));
    const matchTipo = !tipoPagamento || p.tipoPagamento === tipoPagamento;
    return matchBusca && matchTipo;
  });

  const total = state.pacientes.length;
  $('totalPacientes').textContent =
    `${total} paciente${total !== 1 ? 's' : ''} cadastrado${total !== 1 ? 's' : ''}`;

  if (lista.length === 0) {
    $('listaPacientes').innerHTML = `
      <tr><td colspan="5">
        <div class="empty-state">
          <div class="icon">
            <svg width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.25" viewBox="0 0 24 24">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
            </svg>
          </div>
          <h3>${termo || tipoPagamento ? 'Nenhum resultado' : 'Nenhum paciente cadastrado'}</h3>
          <p>${termo || tipoPagamento ? 'Tente outros filtros.' : 'Clique em "Novo paciente" para começar.'}</p>
        </div>
      </td></tr>`;
    return;
  }

  $('listaPacientes').innerHTML = lista.map(linhaHTML).join('');

  $('listaPacientes').querySelectorAll('[data-action="editar"]').forEach(btn => {
    btn.addEventListener('click', () => abrirModalEditar(btn.dataset.id));
  });
}

function linhaHTML(p) {
  const convenio = p.tipoPagamento === 'convenio'
    ? `<span class="badge badge-blue">${p.nomeConvenio || 'Convênio'}</span>`
    : `<span class="badge badge-gray">Particular</span>`;

  return `
    <tr>
      <td>
        <div class="patient-cell">
          <div class="avatar">${iniciais(p.nomeCompleto)}</div>
          <div>
            <div class="patient-name">${esc(p.nomeCompleto)}</div>
            <div class="patient-cpf">${formatCPF(p.cpf)}</div>
          </div>
        </div>
      </td>
      <td class="text-mono">${formatTel(p.telefone)}</td>
      <td>${convenio}</td>
      <td class="text-2 text-sm">${formatData(p.createdAt)}</td>
      <td>
        <div class="row-actions">
          <button class="btn btn-ghost btn-icon btn-sm" data-action="editar" data-id="${p.id}"
            title="Editar paciente" aria-label="Editar ${esc(p.nomeCompleto)}">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
        </div>
      </td>
    </tr>`;
}

// ── Modal ──────────────────────────────────────────────────────────────────
function abrirModalNovo() {
  state.editandoId = null;
  state.fotoBase64 = null;
  $('modalTitulo').textContent = 'Novo paciente';
  $('btnSalvarLabel').textContent = 'Cadastrar paciente';
  $('formPaciente').reset();
  $('pacienteId').value = '';
  resetFoto();
  resetChips();
  setTipoPagamento('particular');
  openModal('modalBackdrop');
}

function abrirModalEditar(id) {
  const p = state.pacientes.find(x => x.id === id);
  if (!p) return;

  state.editandoId = id;
  state.fotoBase64 = null;
  $('modalTitulo').textContent = 'Editar paciente';
  $('btnSalvarLabel').textContent = 'Salvar alterações';
  $('formPaciente').reset();
  resetChips();
  preencherForm(p);
  openModal('modalBackdrop');
}

function preencherForm(p) {
  // Campos de texto/data/select normais
  const campos = [
    'nomeCompleto','dataNascimento','profissao',
    'email','cep','logradouro','numero','complemento','bairro','cidade','uf',
    'nomeConvenio','numCarteirinha','validadePlano','responsavelFinanceiro',
    'emergenciaNome',
  ];
  campos.forEach(k => {
    const el = document.querySelector(`[name="${k}"]`);
    if (el && p[k] != null) el.value = p[k];
  });

  $('pacienteId').value = p.id;
  $('cpf').value = formatCPF(p.cpf);
  $('telefone').value = formatTel(p.telefone);
  if (p.emergenciaTelefone) $('emergenciaTelefone').value = formatTel(p.emergenciaTelefone);

  // Chips de radio
  setRadioValue('sexo', p.sexo);
  setRadioValue('estadoCivil', p.estadoCivil);
  setRadioValue('emergenciaParentesco', p.emergenciaParentesco);

  setTipoPagamento(p.tipoPagamento || 'particular');
}

function resetFoto() {
  $('photoThumb').innerHTML = `
    <svg width="26" height="26" fill="none" stroke="var(--text3)" stroke-width="1.5" viewBox="0 0 24 24">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>`;
  $('inputFoto').value = '';
}

// ── Radio chips ────────────────────────────────────────────────────────────

function getRadioValue(name) {
  return document.querySelector(`input[name="${name}"]:checked`)?.value ?? null;
}

function setRadioValue(name, value) {
  if (!value) return;
  const radio = document.querySelector(`input[name="${name}"][value="${value}"]`);
  if (!radio) return;
  radio.checked = true;
  syncChip(radio);
}

function syncChip(radio) {
  const group = radio.closest('.radio-chips');
  if (!group) return;
  group.querySelectorAll('.chip').forEach(c => c.classList.remove('is-checked'));
  radio.closest('.chip')?.classList.add('is-checked');
}

function resetChips() {
  document.querySelectorAll('.radio-chips input[type="radio"]').forEach(r => {
    r.checked = false;
    r.closest('.chip')?.classList.remove('is-checked');
  });
  document.querySelectorAll('.radio-chips.is-error').forEach(g => g.classList.remove('is-error'));
}

function bindChipEvents() {
  document.querySelectorAll('.radio-chips input[type="radio"]').forEach(radio => {
    radio.addEventListener('change', () => syncChip(radio));
  });
}

// ── Tipo pagamento toggle ──────────────────────────────────────────────────
function setTipoPagamento(tipo) {
  $('tipoPagamento').value = tipo;
  $('btnParticular').classList.toggle('active', tipo === 'particular');
  $('btnConvenio').classList.toggle('active', tipo === 'convenio');
  $('camposConvenio').style.display = tipo === 'convenio' ? '' : 'none';
}

// ── Submit ─────────────────────────────────────────────────────────────────
async function handleSubmit() {
  limparErros();
  if (!validar()) return;

  setSaving(true);

  try {
    const dados = coletarDados();

    const resultado = state.editandoId
      ? await API.atualizarPaciente(state.editandoId, dados)
      : await API.criarPaciente(dados);

    if (state.editandoId) {
      state.pacientes = state.pacientes.map(p => p.id === state.editandoId ? resultado : p);
      showToast('Paciente atualizado com sucesso', 'success');
    } else {
      state.pacientes.unshift(resultado);
      showToast('Paciente cadastrado com sucesso', 'success');
    }

    closeModal('modalBackdrop');
    renderLista();
  } catch (err) {
    // TypeError = erro de rede (servidor inacessível)
    if (err instanceof TypeError) {
      showToast('Servidor inacessível. Verifique se o backend está rodando.', 'error');
    } else {
      // Erro real da API (400, 409, 500…) — exibe a mensagem do servidor
      showToast(err.message || 'Erro ao salvar paciente', 'error');
    }
  } finally {
    setSaving(false);
  }
}

function validar() {
  let ok = true;

  const mostrar = (id, msg, campoEl) => {
    const el = $(id);
    if (el) { el.textContent = msg; el.style.display = 'block'; }
    (campoEl ?? el?.previousElementSibling)?.classList.add('is-error');
    ok = false;
  };

  if (!$('nomeCompleto').value.trim())
    mostrar('erroNome', 'Campo obrigatório.');

  if (!$('dataNascimento').value)
    mostrar('erroNasc', 'Campo obrigatório.');

  if (!getRadioValue('sexo'))
    mostrar('erroSexo', 'Campo obrigatório.',
      document.querySelector('.radio-chips[aria-label="Sexo"]'));

  if (!$('telefone').value.trim())
    mostrar('erroTel', 'Campo obrigatório.');

  const cpfLimpo = $('cpf').value.replace(/\D/g, '');
  if (!cpfLimpo || !validarCPF(cpfLimpo)) mostrar('erroCpf', 'CPF inválido.');

  return ok;
}

function limparErros() {
  ['erroNome','erroNasc','erroSexo','erroTel','erroCpf'].forEach(id => {
    const el = $(id);
    if (el) el.style.display = 'none';
  });
  document.querySelectorAll('.is-error').forEach(el => el.classList.remove('is-error'));
  document.querySelectorAll('.radio-chips.is-error').forEach(g => g.classList.remove('is-error'));
}

function coletarDados() {
  const form = $('formPaciente');
  const data = new FormData(form);
  const obj = {};
  data.forEach((v, k) => { obj[k] = v.trim() || null; });
  obj.cpf = obj.cpf?.replace(/\D/g, '') ?? null;
  obj.telefone = obj.telefone?.replace(/\D/g, '') ?? null;
  obj.emergenciaTelefone = obj.emergenciaTelefone?.replace(/\D/g, '') ?? null;
  obj.cep = obj.cep?.replace(/\D/g, '') ?? null;
  if (state.fotoBase64) obj.fotoBase64 = state.fotoBase64;
  return obj;
}

// ── Saving state ───────────────────────────────────────────────────────────
function setSaving(on) {
  $('btnSalvar').disabled = on;
  $('btnSalvarLabel').style.display = on ? 'none' : '';
  $('btnSalvarSpinner').style.display = on ? '' : 'none';
}

// ── CEP lookup (ViaCEP) ────────────────────────────────────────────────────
async function buscarCEP(cep) {
  const c = cep.replace(/\D/g, '');
  if (c.length !== 8) return;

  $('wrapCep').classList.add('cep-loading');
  try {
    const res = await fetch(`https://viacep.com.br/ws/${c}/json/`);
    const json = await res.json();
    if (json.erro) return;
    $('logradouro').value = json.logradouro || '';
    $('bairro').value     = json.bairro     || '';
    $('cidade').value     = json.localidade || '';
    $('uf').value         = json.uf         || '';
    $('numero').focus();
  } catch {
    // ViaCEP indisponível — ignora silenciosamente
  } finally {
    $('wrapCep').classList.remove('cep-loading');
  }
}

// ── Input masks ────────────────────────────────────────────────────────────
function aplicarMascaras() {
  maskCPF($('cpf'));
  maskTel($('telefone'));
  maskTel($('emergenciaTelefone'));
  maskCEP($('cep'));
}

function maskCPF(el) {
  el.addEventListener('input', () => {
    let v = el.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 9) v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d+)/, '$1.$2.$3');
    else if (v.length > 3) v = v.replace(/^(\d{3})(\d+)/, '$1.$2');
    el.value = v;
  });
}

function maskTel(el) {
  el.addEventListener('input', () => {
    let v = el.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 10)      v = v.replace(/^(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    else if (v.length > 6)  v = v.replace(/^(\d{2})(\d{4,5})(\d*)/, '($1) $2-$3');
    else if (v.length > 2)  v = v.replace(/^(\d{2})(\d*)/, '($1) $2');
    el.value = v;
  });
}

function maskCEP(el) {
  el.addEventListener('input', () => {
    let v = el.value.replace(/\D/g, '').slice(0, 8);
    if (v.length > 5) v = v.replace(/^(\d{5})(\d+)/, '$1-$2');
    el.value = v;
  });
  el.addEventListener('blur', () => buscarCEP(el.value));
}

// ── Photo preview ──────────────────────────────────────────────────────────
function bindFotoEvents() {
  const input = $('inputFoto');
  const thumb = $('photoThumb');

  thumb.addEventListener('click', () => input.click());
  thumb.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') input.click(); });

  input.addEventListener('change', () => {
    const file = input.files[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      showToast('A foto deve ter no máximo 5 MB', 'error');
      return;
    }
    const reader = new FileReader();
    reader.onload = e => {
      state.fotoBase64 = e.target.result;
      thumb.innerHTML = `<img src="${e.target.result}" alt="Foto do paciente">`;
    };
    reader.readAsDataURL(file);
  });
}

// ── Event bindings ─────────────────────────────────────────────────────────
function bindEvents() {
  // Novo paciente
  $('btnNovoPaciente').addEventListener('click', abrirModalNovo);

  // Fechar modal
  $('btnFecharModal').addEventListener('click', () => closeModal('modalBackdrop'));
  $('btnCancelar').addEventListener('click', () => closeModal('modalBackdrop'));
  $('modalBackdrop').addEventListener('click', e => {
    if (e.target === $('modalBackdrop')) closeModal('modalBackdrop');
  });

  // Esc fecha modal
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && $('modalBackdrop').classList.contains('open')) {
      closeModal('modalBackdrop');
    }
  });

  // Salvar
  $('btnSalvar').addEventListener('click', handleSubmit);

  // Tipo pagamento toggle
  $('btnParticular').addEventListener('click', () => setTipoPagamento('particular'));
  $('btnConvenio').addEventListener('click', () => setTipoPagamento('convenio'));

  // Busca e filtro
  $('searchPaciente').addEventListener('input', e => {
    state.filtro.busca = e.target.value;
    renderLista();
  });

  $('filtroConvenio').addEventListener('change', e => {
    state.filtro.tipoPagamento = e.target.value;
    renderLista();
  });

  // Foto
  bindFotoEvents();

  // Chips de radio
  bindChipEvents();
}

// ── Helpers ────────────────────────────────────────────────────────────────
function iniciais(nome) {
  const partes = nome.trim().split(/\s+/);
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase();
  return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
}

function formatCPF(cpf) {
  if (!cpf) return '—';
  const c = String(cpf).replace(/\D/g, '').padStart(11, '0');
  return c.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

function formatTel(tel) {
  if (!tel) return '—';
  const t = String(tel).replace(/\D/g, '');
  if (t.length === 11) return t.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
  if (t.length === 10) return t.replace(/^(\d{2})(\d{4})(\d{4})$/, '($1) $2-$3');
  return tel;
}

function formatData(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('pt-BR');
}

function esc(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function validarCPF(cpf) {
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
  let soma = 0;
  for (let i = 0; i < 9; i++) soma += parseInt(cpf[i]) * (10 - i);
  let r = (soma * 10) % 11;
  if (r === 10 || r === 11) r = 0;
  if (r !== parseInt(cpf[9])) return false;
  soma = 0;
  for (let i = 0; i < 10; i++) soma += parseInt(cpf[i]) * (11 - i);
  r = (soma * 10) % 11;
  if (r === 10 || r === 11) r = 0;
  return r === parseInt(cpf[10]);
}
