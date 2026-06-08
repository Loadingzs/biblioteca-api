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

// ===== FUNÇÕES GLOBAIS =====
function sair() {
    alert('🔓 Logout realizado com sucesso!');
    window.location.href = 'login.html';
}

function navegarPara(pagina) {
    window.location.href = pagina;
}

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
        // Carregar estatísticas
        const responseLivros = await fetch(`${API_URL}/livros`);
        const responseUsuarios = await fetch(`${API_URL}/usuarios`);
        const responseEmprestimosAtivos = await fetch(`${API_URL}/emprestimos/ativos`);
        const responseEmprestimos = await fetch(`${API_URL}/emprestimos`);
        
        const livros = await responseLivros.json();
        const usuarios = await responseUsuarios.json();
        const emprestimosAtivos = await responseEmprestimosAtivos.json();
        const todosEmprestimos = await responseEmprestimos.json();
        
        // Atualizar cards
        document.getElementById('totalLivros').innerText = livros.length;
        document.getElementById('totalUsuarios').innerText = usuarios.length;
        document.getElementById('emprestimosAtivos').innerText = emprestimosAtivos.length;
        document.getElementById('statLivros').innerText = livros.length;
        document.getElementById('statEmprestimos').innerText = emprestimosAtivos.length;
        
        // Calcular multas pendentes
        let multasPendentes = 0;
        todosEmprestimos.forEach(emp => {
            if (emp.status === 'ATRASADO') multasPendentes += 10;
        });
        document.getElementById('statMultas').innerText = `R$ ${multasPendentes.toFixed(2)}`;
        
        // Carregar últimos empréstimos
        const ultimos = todosEmprestimos.slice(-5);
        const tabelaBody = document.getElementById('tabelaEmprestimos');
        if (tabelaBody) {
            tabelaBody.innerHTML = '';
            for (const emp of ultimos.reverse()) {
                const usuario = await buscarUsuarioPorId(emp.usuarioId);
                const livro = await buscarLivroPorId(emp.livroId);
                const row = tabelaBody.insertRow();
                let statusClass = '';
                if (emp.status === 'ATIVO') statusClass = 'status-ativo';
                else if (emp.status === 'ATRASADO') statusClass = 'status-atrasado';
                else statusClass = 'status-devolvido';
                row.innerHTML = `
                    <td>${usuario?.nome || 'N/A'}</td>
                    <td>${livro?.titulo || 'N/A'}</td>
                    <td>${emp.dataEmprestimo || '-'}</td>
                    <td class="${statusClass}">${emp.status || '-'}</td>
                `;
            }
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