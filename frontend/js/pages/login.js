document.addEventListener('DOMContentLoaded', () => {
  setupPasswordToggle();
  setupForm();
});

function setupPasswordToggle() {
  const toggle = document.getElementById('toggleSenha');
  const input  = document.getElementById('senha');
  const icon   = document.getElementById('eyeIcon');

  const EYE_OPEN   = `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`;
  const EYE_CLOSED = `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>`;

  toggle.addEventListener('click', () => {
    const isPassword = input.type === 'password';
    input.type     = isPassword ? 'text' : 'password';
    icon.innerHTML = isPassword ? EYE_CLOSED : EYE_OPEN;
    toggle.setAttribute('aria-label', isPassword ? 'Ocultar senha' : 'Mostrar senha');
  });
}

function setupForm() {
  const form    = document.getElementById('formLogin');
  const errorEl = document.getElementById('loginError');
  const btn     = document.getElementById('btnLogin');
  const label   = document.getElementById('btnLoginLabel');
  const spinner = document.getElementById('btnLoginSpinner');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorEl.style.display = 'none';

    const email = document.getElementById('email').value.trim();
    const senha = document.getElementById('senha').value;
    if (!email || !senha) return;

    btn.disabled         = true;
    label.style.display  = 'none';
    spinner.style.display = 'block';

    try {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha }),
      });

      if (res.ok) {
        const data = await res.json();
        sessionStorage.setItem('token', data.token);
        sessionStorage.setItem('usuario_nome', data.nome);
        sessionStorage.setItem('usuario_perfil', data.perfil);
        window.location.href = 'pages/dashboard.html';
      } else {
        errorEl.style.display = 'flex';
      }
    } catch {
      errorEl.style.display = 'flex';
    } finally {
      btn.disabled          = false;
      label.style.display   = 'block';
      spinner.style.display = 'none';
    }
  });
}
