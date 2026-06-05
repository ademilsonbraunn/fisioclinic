import { initNotificacoes } from '../components/notificacoes.js';

// Utilitário central de autenticação — usado por todas as páginas protegidas

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

  // Exibe botão de administração apenas para perfil ADMIN
  const adminBtn = document.getElementById('topbar-admin-btn');
  if (adminBtn && getUsuarioPerfil() === 'ADMIN') {
    adminBtn.style.display = '';
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
    dropdown.innerHTML = `
      <div class="user-dropdown-header">
        <span class="user-dropdown-nome">${nome}</span>
        <span class="user-dropdown-perfil">${getUsuarioPerfil()}</span>
      </div>
      <div class="user-dropdown-divider"></div>
      <button class="user-dropdown-item user-dropdown-sair" id="btn-logout">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
             fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Sair
      </button>
    `;
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
