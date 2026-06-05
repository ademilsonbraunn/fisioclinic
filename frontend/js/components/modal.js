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
