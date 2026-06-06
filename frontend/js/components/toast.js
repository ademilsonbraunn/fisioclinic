// ─────────────────────────────────────────────────────────────────────────────
// toast.js — Notificações temporárias (toasts) no canto da tela
// ─────────────────────────────────────────────────────────────────────────────
// Exporta showToast(message, type, duration):
//   - message: texto a exibir
//   - type: 'success' | 'error' | 'warning' | 'default'
//   - duration: ms até desaparecer (padrão: 4000ms)
//
// Funcionamento:
//   1. Localiza #toastContainer no DOM (deve existir no HTML da página)
//   2. Cria um <div class="toast [type]"> com ícone SVG inline + texto
//   3. Agenda a remoção com dismiss() após `duration` ms
//   4. Clicar no toast dispara dismiss() imediatamente
//   5. dismiss() anima opacidade e translateX antes de remover o elemento
//
// CSS dos toasts está em base.css (.toast, .toast.success, .toast.error, etc.)
// ─────────────────────────────────────────────────────────────────────────────

const ICON = {
  success: `<svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>`,
  error:   `<svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`,
  warning: `<svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" viewBox="0 0 24 24"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><circle cx="12" cy="17" r=".5" fill="currentColor"/></svg>`,
};

/**
 * @param {string} message
 * @param {'success'|'error'|'warning'|'default'} [type]
 * @param {number} [duration] ms
 */
export function showToast(message, type = 'default', duration = 4000) {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const el = document.createElement('div');
  el.className = `toast${type !== 'default' ? ' ' + type : ''}`;
  el.innerHTML = `${ICON[type] ?? ''}<span>${message}</span>`;
  container.appendChild(el);

  const dismiss = () => {
    el.style.transition = 'opacity .18s, transform .18s';
    el.style.opacity = '0';
    el.style.transform = 'translateX(10px)';
    setTimeout(() => el.remove(), 200);
  };

  const timer = setTimeout(dismiss, duration);
  el.addEventListener('click', () => { clearTimeout(timer); dismiss(); });
}
