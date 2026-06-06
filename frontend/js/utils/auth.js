// ─────────────────────────────────────────────────────────────────────────────
// auth.js — Utilitário central de autenticação e topbar
// ─────────────────────────────────────────────────────────────────────────────
// Importado por todas as páginas protegidas (dashboard, pacientes, agenda,
// prontuário, admin). NÃO importado pelo login.js — login faz fetch direto.
//
// Responsabilidades:
//  - getToken() / getUsuarioNome() / getUsuarioPerfil():
//      Lê credenciais do sessionStorage (padrão) ou localStorage ("lembrar de mim")
//  - logout(): limpa ambos os storages e redireciona para index.html
//      Caminho relativo ../index.html funciona porque todas as páginas protegidas
//      estão em pages/, um nível abaixo do login
//  - initTopbar(): popula avatar, nome e perfil no topbar; cria dropdown de logout
//      dinamicamente se não existir; exibe botão Admin apenas para perfil ADMIN;
//      inicializa o painel de notificações (initNotificacoes)
// ─────────────────────────────────────────────────────────────────────────────

import { initNotificacoes } from '../components/notificacoes.js';

// Utilitário central de autenticação — usado por todas as páginas protegidas

// Verifica sessionStorage primeiro; cai para localStorage apenas quando o usuário
// escolheu "lembrar de mim" no login (login.js grava nesse storage nesse caso).
// Trocar por cookies httpOnly exigiria mudança no backend — aceito como tradeoff consciente.
export function getToken() {
  return sessionStorage.getItem('token') || localStorage.getItem('token');
}

export function getUsuarioNome() {
  return sessionStorage.getItem('usuario_nome') || localStorage.getItem('usuario_nome') || 'Usuário';
}

export function getUsuarioPerfil() {
  return sessionStorage.getItem('usuario_perfil') || localStorage.getItem('usuario_perfil') || '';
}

export function logout() {
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('usuario_nome');
  sessionStorage.removeItem('usuario_perfil');
  localStorage.removeItem('token');
  localStorage.removeItem('usuario_nome');
  localStorage.removeItem('usuario_perfil');

  // Todas as páginas protegidas estão em pages/ — login está um nível acima
  window.location.href = '../index.html';
}

function iniciais(nome) {
  return nome
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map(p => p[0].toUpperCase())
    .join('');
}

// Popula o topbar com nome/avatar e abre dropdown de logout ao clicar
export function initTopbar() {
  const nome   = getUsuarioNome();
  const avatarEl = document.getElementById('topbar-avatar');
  const nomeEl   = document.getElementById('topbar-nome');
  const userEl   = document.getElementById('topbar-user');

  if (avatarEl) avatarEl.textContent = iniciais(nome);
  if (nomeEl)   nomeEl.textContent   = nome;

  // Exibe botões de administração/usuários apenas para perfil ADMIN
  if (getUsuarioPerfil() === 'ADMIN') {
    const adminBtn = document.getElementById('topbar-admin-btn');
    if (adminBtn) adminBtn.style.display = '';
    const usersBtn = document.getElementById('topbar-users-btn');
    if (usersBtn) usersBtn.style.display = '';
  }

  if (!userEl) return;

  userEl.setAttribute('role', 'button');
  userEl.setAttribute('aria-haspopup', 'true');
  userEl.setAttribute('aria-expanded', 'false');
  userEl.style.cursor = 'pointer';

  // Cria dropdown se ainda não existir
  if (!document.getElementById('user-dropdown')) {
    const dropdown = document.createElement('div');
    dropdown.id        = 'user-dropdown';
    dropdown.className = 'user-dropdown';

    const header = document.createElement('div');
    header.className = 'user-dropdown-header';

    const nomeSpan = document.createElement('span');
    nomeSpan.className = 'user-dropdown-nome';
    nomeSpan.textContent = nome;

    const perfilSpan = document.createElement('span');
    perfilSpan.className = 'user-dropdown-perfil';
    perfilSpan.textContent = getUsuarioPerfil();

    header.appendChild(nomeSpan);
    header.appendChild(perfilSpan);

    const divider = document.createElement('div');
    divider.className = 'user-dropdown-divider';

    const btnLogout = document.createElement('button');
    btnLogout.className = 'user-dropdown-item user-dropdown-sair';
    btnLogout.id = 'btn-logout';
    btnLogout.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
           fill="none" stroke="currentColor" stroke-width="2"
           stroke-linecap="round" stroke-linejoin="round">
        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
        <polyline points="16 17 21 12 16 7"/>
        <line x1="21" y1="12" x2="9" y2="12"/>
      </svg>
      Sair
    `;

    dropdown.appendChild(header);
    dropdown.appendChild(divider);
    dropdown.appendChild(btnLogout);
    userEl.appendChild(dropdown);

    document.getElementById('btn-logout').addEventListener('click', (e) => {
      e.stopPropagation();
      logout();
    });
  }

  // Toggle ao clicar no usuário
  userEl.addEventListener('click', (e) => {
    const dropdown = document.getElementById('user-dropdown');
    const aberto   = dropdown.classList.toggle('open');
    userEl.setAttribute('aria-expanded', String(aberto));
    e.stopPropagation();
  });

  // Fecha ao clicar fora
  document.addEventListener('click', () => {
    const dropdown = document.getElementById('user-dropdown');
    if (dropdown) {
      dropdown.classList.remove('open');
      userEl.setAttribute('aria-expanded', 'false');
    }
  });

  // Inicializa painel de atualizações do sistema
  initNotificacoes();
}
