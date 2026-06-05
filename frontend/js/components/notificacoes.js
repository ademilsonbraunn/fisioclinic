import { listarAtualizacoes } from '../api/atualizacoes.js';

const LABELS = {
    NOVO_RECURSO: 'Novo recurso',
    MELHORIA:     'Melhoria',
    CORRECAO:     'Correção'
};

const STORAGE_KEY = 'fisioclinic_notif_lidas';

function getLidas() {
    try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]'); }
    catch (_) { return []; }
}

function salvarLidas(ids) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
}

function marcarComoLida(id) {
    const lidas = getLidas();
    if (!lidas.includes(id)) {
        lidas.push(id);
        salvarLidas(lidas);
    }
}

function marcarTodasComoLidas(ids) {
    salvarLidas(ids);
}

function formatarData(isoDate) {
    if (!isoDate) return '';
    const [y, m, d] = String(isoDate).split('-');
    return `${d}/${m}/${y}`;
}

function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function renderItem(a, lidas) {
    const lida      = lidas.includes(String(a.id));
    const tipoClass = a.tipo === 'NOVO_RECURSO' ? 'blue'
                    : a.tipo === 'MELHORIA'     ? 'amber'
                    : 'green';
    const label = LABELS[a.tipo] || a.tipo;

    return `
        <div class="notif-item${lida ? '' : ' notif-item--unread'}"
             data-id="${escHtml(a.id)}"
             role="button"
             tabindex="0"
             title="${lida ? 'Já lida' : 'Clique para marcar como lida'}">
            <div class="notif-item-header">
                <span class="notif-versao">${escHtml(a.versao)}</span>
                <span class="badge badge-${tipoClass} notif-tipo">${escHtml(label)}</span>
                ${lida ? '' : '<span class="notif-dot" aria-label="Não lida"></span>'}
            </div>
            <p class="notif-titulo">${escHtml(a.titulo)}</p>
            <p class="notif-descricao">${escHtml(a.descricao)}</p>
            <div class="notif-item-footer">
                <span class="notif-data">${formatarData(a.data_lancamento)}</span>
                ${lida
                    ? '<span class="notif-lida-label">Lida</span>'
                    : '<button class="notif-marcar-btn" data-id="' + escHtml(a.id) + '">Marcar como lida</button>'}
            </div>
        </div>`;
}

function atualizarBadge(badge, dados, lidas) {
    const qtd = dados.filter(a => !lidas.includes(String(a.id))).length;
    if (qtd > 0) {
        badge.textContent = qtd;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

function atualizarBotaoMarcarTodas(header, dados, lidas) {
    const btnExistente = header.querySelector('#notifMarcarTodas');
    const temNaoLidas  = dados.some(a => !lidas.includes(String(a.id)));
    if (temNaoLidas && !btnExistente) {
        const btn = document.createElement('button');
        btn.id        = 'notifMarcarTodas';
        btn.className = 'notif-marcar-todas-btn';
        btn.textContent = 'Marcar todas como lidas';
        header.insertBefore(btn, header.querySelector('#notifClose'));
    } else if (!temNaoLidas && btnExistente) {
        btnExistente.remove();
    }
}

export async function initNotificacoes() {
    const btn    = document.getElementById('notifBtn');
    const panel  = document.getElementById('notifPanel');
    const badge  = document.getElementById('notifBadge');
    const list   = document.getElementById('notifList');
    const header = panel ? panel.querySelector('.notif-panel-header') : null;
    if (!btn || !panel || !list || !header) return;

    let dados = [];
    try { dados = await listarAtualizacoes(); } catch (_) { dados = []; }

    function renderizar() {
        const lidas = getLidas();
        list.innerHTML = dados.length
            ? dados.map(a => renderItem(a, lidas)).join('')
            : '<p class="notif-empty">Nenhuma atualização disponível.</p>';
        atualizarBadge(badge, dados, lidas);
        atualizarBotaoMarcarTodas(header, dados, lidas);
        bindItemEvents();
    }

    function bindItemEvents() {
        // Botões individuais "Marcar como lida"
        list.querySelectorAll('.notif-marcar-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.dataset.id;
                marcarComoLida(id);
                renderizar();
            });
        });

        // Clicar no item também marca como lida
        list.querySelectorAll('.notif-item').forEach(item => {
            item.addEventListener('click', () => {
                const id = item.dataset.id;
                if (id) { marcarComoLida(id); renderizar(); }
            });
            item.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    item.click();
                }
            });
        });

        // "Marcar todas como lidas"
        const btnTodas = header.querySelector('#notifMarcarTodas');
        if (btnTodas) {
            btnTodas.addEventListener('click', (e) => {
                e.stopPropagation();
                marcarTodasComoLidas(dados.map(a => String(a.id)));
                renderizar();
            });
        }
    }

    // Render inicial
    renderizar();

    // Toggle do painel
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const aberto = panel.style.display !== 'none';
        panel.style.display = aberto ? 'none' : 'block';
    });

    // Fecha ao clicar fora
    document.addEventListener('click', (e) => {
        if (!btn.contains(e.target) && !panel.contains(e.target)) {
            panel.style.display = 'none';
        }
    });

    // Botão fechar
    const closeBtn = document.getElementById('notifClose');
    if (closeBtn) {
        closeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            panel.style.display = 'none';
        });
    }
}
