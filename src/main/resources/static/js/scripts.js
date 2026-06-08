// js/scripts.js
const API_URL = '/api';

// ===== LOADING / SPINNER =====
function mostrarLoading() {
    // Verificar se já existe um overlay
    if (document.getElementById('globalLoading')) return;
    
    const overlay = document.createElement('div');
    overlay.id = 'globalLoading';
    overlay.className = 'loading-overlay';
    overlay.innerHTML = `
        <div class="loading-container">
            <div class="spinner"></div>
            <div class="loading-text">Carregando...</div>
        </div>
    `;
    document.body.appendChild(overlay);
}

function esconderLoading() {
    const overlay = document.getElementById('globalLoading');
    if (overlay) {
        overlay.remove();
    }
}

function mostrarLoadingTabela(tabelaId) {
    const tabela = document.getElementById(tabelaId);
    if (tabela) {
        tabela.innerHTML = `<tr><td colspan="10" class="table-loading">
            <div class="spinner-small"></div><br>
            Carregando dados...
        </td></tr>`;
    }
}

// ===== TOASTS =====
function _ensureToastContainer() {
    let c = document.querySelector('.toast-container');
    if (!c) {
        c = document.createElement('div');
        c.className = 'toast-container';
        document.body.appendChild(c);
    }
    return c;
}

function showToast(type = 'info', title = null, message = '', duration = 3000) {
    const container = _ensureToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.style.setProperty('--toast-duration', duration + 'ms');

    const icon = document.createElement('div');
    icon.className = 'toast-icon';
    icon.textContent = type === 'success' ? '✅' : type === 'error' ? '❌' : type === 'warning' ? '⚠️' : 'ℹ️';

    const content = document.createElement('div');
    content.className = 'toast-content';
    if (title) content.innerHTML = `<div class="toast-title">${title}</div><div class="toast-message">${message || ''}</div>`;
    else content.innerHTML = `<div class="toast-message">${message || ''}</div>`;

    toast.appendChild(icon);
    toast.appendChild(content);
    container.appendChild(toast);

    let hideTimeout = setTimeout(() => removeToast(toast), duration);

    function removeToast(t) {
        t.style.transition = 'opacity 160ms, transform 160ms';
        t.style.opacity = '0';
        t.style.transform = 'translateX(10px) scale(.98)';
        setTimeout(() => t.remove(), 180);
    }

    toast.addEventListener('click', () => { clearTimeout(hideTimeout); removeToast(toast); });
    toast.addEventListener('mouseenter', () => clearTimeout(hideTimeout));
    toast.addEventListener('mouseleave', () => { hideTimeout = setTimeout(() => removeToast(toast), 1500); });

    return toast;
}

function toastSuccess(title, message, duration) { return showToast('success', title, message, duration); }
function toastError(title, message, duration) { return showToast('error', title, message, duration); }
function toastInfo(title, message, duration) { return showToast('info', title, message, duration); }

// ===== MODAL =====
function _ensureGlobalModal() {
    let modal = document.getElementById('globalModal');
    if (modal) return modal;

    modal = document.createElement('div');
    modal.id = 'globalModal';
    modal.className = 'modal';
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    modal.style.display = 'none';

    modal.innerHTML = `
        <div class="modal-content">
            <h3 class="modal-title"></h3>
            <div class="modal-body"></div>
            <div class="modal-actions" style="display:flex;gap:0.5rem;justify-content:flex-end;margin-top:1rem;">
                <button class="btn-cancel modal-cancel">Cancelar</button>
                <button class="btn-save modal-ok">OK</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    // close when clicking outside content
    modal.addEventListener('click', (e) => { if (e.target === modal) hideModal(modal); });
    return modal;
}

function showModal(options = {}) {
    const modal = _ensureGlobalModal();
    const titleEl = modal.querySelector('.modal-title');
    const bodyEl = modal.querySelector('.modal-body');
    const okBtn = modal.querySelector('.modal-ok');
    const cancelBtn = modal.querySelector('.modal-cancel');

    titleEl.textContent = options.title || '';
    if (typeof options.html !== 'undefined') bodyEl.innerHTML = options.html;
    else bodyEl.textContent = options.text || '';

    if (options.showCancel === false) cancelBtn.style.display = 'none'; else cancelBtn.style.display = '';
    okBtn.textContent = options.okText || 'OK';
    cancelBtn.textContent = options.cancelText || 'Cancelar';

    return new Promise((resolve) => {
        function cleanup() {
            okBtn.removeEventListener('click', onOk);
            cancelBtn.removeEventListener('click', onCancel);
        }
        function onOk() { hideModal(modal); cleanup(); resolve(true); }
        function onCancel() { hideModal(modal); cleanup(); resolve(false); }
        okBtn.addEventListener('click', onOk);
        cancelBtn.addEventListener('click', onCancel);
        showModalElement(modal);
    });
}

function showAlert(title, text) {
    return showModal({ title: title || '', html: `<pre style="white-space:pre-wrap;word-wrap:break-word;margin:0;font-family:inherit;">${escapeHtml(text || '')}</pre>`, showCancel: false, okText: 'Fechar' });
}

function showConfirm(text, title) {
    return showModal({ title: title || 'Confirmação', text: text || '', okText: 'Sim', cancelText: 'Não', showCancel: true });
}

function showModalElement(modal) {
    modal.style.display = 'flex';
}

function hideModal(modal) {
    modal.style.display = 'none';
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"']/g, function (s) {
        return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[s];
    });
}

// ===== AUTHENTICATION =====
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');
    options.headers = {
        ...options.headers,
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
    };
    return fetch(url, options);
}

function checkAuth() {
    const token = localStorage.getItem('token');
    const isLoginPage = window.location.pathname.includes('login.html') || window.location.pathname.endsWith('.html') === false;
    
    if (!token && !isLoginPage) {
        window.location.href = 'login.html';
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    showToast('success', null, '🔓 Logout realizado com sucesso!');
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 600);
}


// ===== FUNÇÕES GLOBAIS =====
function sair() {
    logout();
}

// ===== TRANSIÇÃO DE PÁGINAS =====
function navegarPara(pagina) {
    const transition = document.getElementById('pageTransition');
    if (transition) {
        transition.classList.add('active');
        setTimeout(() => {
            window.location.href = pagina;
        }, 300);
    } else {
        window.location.href = pagina;
    }
}

// Adicionar efeito fade-in ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    document.body.classList.add('fade-in');
    
    // Remover a classe após a animação
    setTimeout(() => {
        document.body.classList.remove('fade-in');
    }, 500);
});

// ===== LOGIN =====
async function fazerLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });
        
        const resultado = await response.json();
        
        if (resultado.success) {
            alert('✅ Login realizado com sucesso!');
            window.location.href = 'index.html';
        } else {
            alert('❌ Email ou senha inválidos! Use: admin@biblioteca.com / admin123');
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('❌ Erro de conexão com o servidor. Verifique se o back-end está rodando.');
    }
    
    return false;
}

// ===== DASHBOARD =====
async function carregarDashboard() {
    try {
        const [responseLivros, responseUsuarios, responseEmprestimosAtivos, responseEmprestimos] = await Promise.all([
            fetch(`${API_URL}/livros`),
            fetch(`${API_URL}/usuarios`),
            fetch(`${API_URL}/emprestimos/ativos`),
            fetch(`${API_URL}/emprestimos`)
        ]);

        const livros = await responseLivros.json();
        const usuarios = await responseUsuarios.json();
        const emprestimosAtivos = await responseEmprestimosAtivos.json();
        const todosEmprestimos = await responseEmprestimos.json();

        document.getElementById('totalLivros').innerText = livros.length;
        document.getElementById('totalUsuarios').innerText = usuarios.length;
        document.getElementById('emprestimosAtivos').innerText = emprestimosAtivos.length;
        document.getElementById('statLivros').innerText = livros.length;
        document.getElementById('statEmprestimos').innerText = emprestimosAtivos.length;

        let multasPendentes = 0;
        todosEmprestimos.forEach(emp => {
            if (emp.status === 'ATRASADO') multasPendentes += 10;
        });
        document.getElementById('statMultas').innerText = `R$ ${multasPendentes.toFixed(2)}`;

        const ultimos = todosEmprestimos.slice(-5).reverse();
        const tabelaBody = document.getElementById('tabelaEmprestimos');
        if (tabelaBody) {
            const fragment = document.createDocumentFragment();
            const linhas = await Promise.all(ultimos.map(async (emp) => {
                const [usuario, livro] = await Promise.all([
                    buscarUsuarioPorId(emp.usuarioId),
                    buscarLivroPorId(emp.livroId)
                ]);

                let statusClass = '';
                if (emp.status === 'ATIVO') statusClass = 'status-ativo';
                else if (emp.status === 'ATRASADO') statusClass = 'status-atrasado';
                else statusClass = 'status-devolvido';

                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${usuario?.nome || 'N/A'}</td>
                    <td>${livro?.titulo || 'N/A'}</td>
                    <td>${emp.dataEmprestimo || '-'}</td>
                    <td class="${statusClass}">${emp.status || '-'}</td>
                `;
                return row;
            }));

            linhas.forEach(row => fragment.appendChild(row));
            tabelaBody.innerHTML = '';
            tabelaBody.appendChild(fragment);
        }
    } catch (error) {
        console.error('Erro ao carregar dashboard:', error);
    }
}

async function buscarUsuarioPorId(id) {
    try {
        const response = await fetch(`${API_URL}/usuarios/${id}`);
        return await response.json();
    } catch { return null; }
}

async function buscarLivroPorId(id) {
    try {
        const response = await fetch(`${API_URL}/livros/${id}`);
        return await response.json();
    } catch { return null; }
}

// Inicializar dashboard
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('index.html') || window.location.pathname === '/' || window.location.pathname.endsWith('/')) {
        carregarDashboard();
    }
    
    const anoElement = document.getElementById('anoAtual');
    if (anoElement) {
        anoElement.innerText = new Date().getFullYear();
    }
});

// ===== TEMA DARK/LIGHT =====
function initTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        document.getElementById('themeToggle').textContent = '☀️';
    } else {
        document.body.classList.remove('dark-mode');
        document.getElementById('themeToggle').textContent = '🌙';
    }
}

function toggleTheme() {
    if (document.body.classList.contains('dark-mode')) {
        document.body.classList.remove('dark-mode');
        localStorage.setItem('theme', 'light');
        document.getElementById('themeToggle').textContent = '🌙';
    } else {
        document.body.classList.add('dark-mode');
        localStorage.setItem('theme', 'dark');
        document.getElementById('themeToggle').textContent = '☀️';
    }
}

// Inicializar tema ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
});