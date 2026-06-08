package com.biblioteca.api.service;

import com.biblioteca.api.domain.Emprestimo;
import com.biblioteca.api.domain.Livro;
import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.repository.EmprestimoRepository;
import com.biblioteca.api.repository.LivroRepository;
import com.biblioteca.api.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service  // ← ADICIONE ESTA LINHA

public class EmprestimoService {
    
    private EmprestimoRepository emprestimoRepository;
    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    
    public EmprestimoService() {
        this.emprestimoRepository = new EmprestimoRepository();
        this.livroRepository = new LivroRepository();
        this.usuarioRepository = new UsuarioRepository();
    }
    
    // ==================== REGRAS DE NEGÓCIO ====================
    
    // Validar dados do empréstimo
    private boolean validarEmprestimo(Emprestimo emprestimo) {
        if (emprestimo == null) {
            System.err.println("❌ Empréstimo não pode ser nulo");
            return false;
        }
        
        if (emprestimo.getLivroId() <= 0) {
            System.err.println("❌ ID do livro inválido");
            return false;
        }
        
        if (emprestimo.getUsuarioId() <= 0) {
            System.err.println("❌ ID do usuário inválido");
            return false;
        }
        
        if (emprestimo.getDataEmprestimo() == null) {
            System.err.println("❌ Data de empréstimo é obrigatória");
            return false;
        }
        
        if (emprestimo.getDataDevolucaoPrevista() == null) {
            System.err.println("❌ Data de devolução prevista é obrigatória");
            return false;
        }
        
        return true;
    }
    
    // Verificar se livro está disponível para empréstimo
    public boolean isLivroDisponivel(int livroId) {
        Livro livro = livroRepository.buscarPorId(livroId);
        if (livro == null) {
            return false;
        }
        return livro.isDisponivel();
    }
    
    // Verificar se usuário pode pegar mais empréstimos (limite de 3)
    public boolean usuarioPodePegarEmprestimo(int usuarioId) {
        List<Emprestimo> emprestimosAtivos = emprestimoRepository.buscarPorUsuario(usuarioId);
        long ativos = emprestimosAtivos.stream()
                .filter(e -> e.getStatus().equals("ATIVO") || e.getStatus().equals("ATRASADO"))
                .count();

        if (ativos >= 3) {
            System.err.println("❌ Usuário já possui 3 empréstimos ativos (limite máximo)");
            return false;
        }
        return true;
    }
    
    // Calcular multa (R$ 2,00 por dia de atraso)
    public double calcularMulta(Emprestimo emprestimo) {
        if (emprestimo.getDataDevolucaoReal() == null) {
            return 0;
        }
        
        LocalDate dataDevolucaoPrevista = emprestimo.getDataDevolucaoPrevista();
        LocalDate dataDevolucaoReal = emprestimo.getDataDevolucaoReal();
        
        if (dataDevolucaoReal.isAfter(dataDevolucaoPrevista)) {
            long diasAtraso = ChronoUnit.DAYS.between(dataDevolucaoPrevista, dataDevolucaoReal);
            return diasAtraso * 2.0; // R$ 2,00 por dia
        }
        
        return 0;
    }
    
    // Atualizar status do empréstimo baseado na data atual
    public void atualizarStatusEmprestimos() {
        List<Emprestimo> emprestimosAtivos = emprestimoRepository.buscarAtivos();
        LocalDate hoje = LocalDate.now();
        
        for (Emprestimo emprestimo : emprestimosAtivos) {
            if (emprestimo.getStatus().equals("ATIVO") && 
                hoje.isAfter(emprestimo.getDataDevolucaoPrevista())) {
                emprestimo.setStatus("ATRASADO");
                emprestimoRepository.atualizar(emprestimo);
                System.out.println("⚠️ Empréstimo ID " + emprestimo.getId() + " está atrasado!");
            }
        }
    }
    
    // ==================== OPERAÇÕES PRINCIPAIS ====================
    
    // Realizar novo empréstimo
    public boolean realizarEmprestimo(int livroId, int usuarioId, int diasParaDevolucao) {
        // Validar livro
        if (!isLivroDisponivel(livroId)) {
            System.err.println("❌ Livro não está disponível para empréstimo");
            return false;
        }
        
        // Validar usuário
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId);
        if (usuario == null) {
            System.err.println("❌ Usuário não encontrado");
            return false;
        }
        
        if (!usuarioPodePegarEmprestimo(usuarioId)) {
            return false;
        }
        
        // Criar empréstimo
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataDevolucaoPrevista = dataEmprestimo.plusDays(diasParaDevolucao);
        
        Emprestimo emprestimo = new Emprestimo(livroId, usuarioId, dataEmprestimo, dataDevolucaoPrevista);
        
        // Salvar empréstimo
        boolean sucesso = emprestimoRepository.salvar(emprestimo);
        
        if (sucesso) {
            // Atualizar disponibilidade do livro
            livroRepository.atualizarDisponibilidade(livroId, false);
            System.out.println("✅ Empréstimo realizado com sucesso!");
            System.out.println("   Livro ID: " + livroId + " | Usuário: " + usuario.getNome());
            System.out.println("   Devolução prevista: " + dataDevolucaoPrevista);
        }
        
        return sucesso;
    }
    
    // Registrar devolução
    public boolean registrarDevolucao(int emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(emprestimoId);

        if (emprestimo == null) {
            System.err.println("❌ Empréstimo não encontrado");
            return false;
        }

        if (emprestimo.getStatus().equals("FINALIZADO")) {
            System.err.println("❌ Este empréstimo já foi devolvido");
            return false;
        }

        LocalDate dataDevolucaoReal = LocalDate.now();
        double multa = calcularMulta(emprestimo);

        // Atualizar empréstimo
        emprestimo.setDataDevolucaoReal(dataDevolucaoReal);
        emprestimo.setStatus("FINALIZADO");  // ← Mudei de "DEVOLVIDO" para "FINALIZADO"

        boolean sucesso = emprestimoRepository.atualizar(emprestimo);

        if (sucesso) {
            // Devolver o livro (disponibilizar novamente)
            livroRepository.atualizarDisponibilidade(emprestimo.getLivroId(), true);

            System.out.println("✅ Devolução registrada com sucesso!");
            System.out.println("   Data de devolução: " + dataDevolucaoReal);

            if (multa > 0) {
                System.out.println("⚠️ Multa por atraso: R$ " + String.format("%.2f", multa));
            }
        }

        return sucesso;
    }
    
    // ==================== CRUD BÁSICO ====================
    
    public List<Emprestimo> listarTodosEmprestimos() {
        return emprestimoRepository.buscarTodos();
    }
    
    public Emprestimo buscarEmprestimoPorId(int id) {
        if (id <= 0) {
            System.err.println("❌ ID inválido");
            return null;
        }
        return emprestimoRepository.buscarPorId(id);
    }
    
    public List<Emprestimo> buscarEmprestimosPorUsuario(int usuarioId) {
        if (usuarioId <= 0) {
            System.err.println("❌ ID do usuário inválido");
            return List.of();
        }
        return emprestimoRepository.buscarPorUsuario(usuarioId);
    }
    
    public List<Emprestimo> buscarEmprestimosPorLivro(int livroId) {
        if (livroId <= 0) {
            System.err.println("❌ ID do livro inválido");
            return List.of();
        }
        return emprestimoRepository.buscarPorLivro(livroId);
    }
    
    public List<Emprestimo> buscarEmprestimosAtivos() {
        return emprestimoRepository.buscarAtivos();
    }
    
    public int getTotalEmprestimosAtivos() {
        return emprestimoRepository.contarEmprestimosAtivos();
    }
    
    // Renovar empréstimo (adicionar mais dias)
    public boolean renovarEmprestimo(int emprestimoId, int diasAdicionais) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(emprestimoId);
        
        if (emprestimo == null) {
            System.err.println("❌ Empréstimo não encontrado");
            return false;
        }
        
        if (!emprestimo.getStatus().equals("ATIVO")) {
            System.err.println("❌ Apenas empréstimos ativos podem ser renovados");
            return false;
        }
        
        LocalDate novaDataPrevista = emprestimo.getDataDevolucaoPrevista().plusDays(diasAdicionais);
        emprestimo.setDataDevolucaoPrevista(novaDataPrevista);
        
        boolean sucesso = emprestimoRepository.atualizar(emprestimo);
        
        if (sucesso) {
            System.out.println("✅ Empréstimo renovado com sucesso!");
            System.out.println("   Nova data de devolução: " + novaDataPrevista);
        }
        
        return sucesso;
    }
}