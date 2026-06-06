// ─────────────────────────────────────────────────────────────────────────────
// modal.js — Utilitário de abertura/fechamento de modais
// ─────────────────────────────────────────────────────────────────────────────
// Interface mínima: dois métodos que operam sobre o elemento backdrop do modal.
//
// openModal(backdropId):
//   - Adiciona a classe "open" ao backdrop (CSS controla a visibilidade via display)
//   - Bloqueia o scroll do body enquanto o modal está aberto
//   - Move o foco para o primeiro campo interativo do modal (acessibilidade)
//
// closeModal(backdropId):
//   - Remove a classe "open" do backdrop
//   - Restaura o scroll do body
//
// O backdrop deve ter id igual a backdropId no HTML.
// O estado "aberto" é controlado 100% por CSS (.modal-backdrop.open { display:flex })
// definido em components.css — não há manipulação de display aqui.
// ─────────────────────────────────────────────────────────────────────────────

export function openModal(backdropId) {
  const el = document.getElementById(backdropId);
  if (!el) return;
  el.classList.add('open');
  document.body.style.overflow = 'hidden';
  // Focus first focusable element
  const focusable = el.querySelector('input:not([type=hidden]), select, textarea, button:not([aria-label="Fechar modal"])');
  focusable?.focus();
}

export function closeModal(backdropId) {
  const el = document.getElementById(backdropId);
  if (!el) return;
  el.classList.remove('open');
  document.body.style.overflow = '';
}
