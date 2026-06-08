package com.biblioteca.api.repository;

import com.biblioteca.api.domain.Emprestimo;  // ← ADICIONE ESTE IMPORT
import com.biblioteca.api.infrastructure.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoRepository {
    
    // ==================== CREATE ====================
    public boolean salvar(Emprestimo emprestimo) {
        String sql = "INSERT INTO emprestimos (id_livro, id_usuario, data_emprestimo, data_devolucao_prevista, status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, emprestimo.getLivroId());
            stmt.setInt(2, emprestimo.getUsuarioId());
            stmt.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataEmprestimo().atStartOfDay()));
            stmt.setDate(4, Date.valueOf(emprestimo.getDataDevolucaoPrevista()));
            stmt.setString(5, emprestimo.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        emprestimo.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Empréstimo salvo - Livro ID: " + emprestimo.getLivroId() + 
                                   ", Usuário ID: " + emprestimo.getUsuarioId());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao salvar empréstimo: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== READ (TODOS) ====================
    public List<Emprestimo> buscarTodos() {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos ORDER BY data_emprestimo DESC";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                emprestimos.add(criarEmprestimoFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar empréstimos: " + e.getMessage());
        }
        
        return emprestimos;
    }
    
    // ==================== READ (POR ID) ====================
    public Emprestimo buscarPorId(int id) {
        String sql = "SELECT * FROM emprestimos WHERE id = ?";
        Emprestimo emprestimo = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    emprestimo = criarEmprestimoFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar empréstimo por ID: " + e.getMessage());
        }
        
        return emprestimo;
    }
    
    // ==================== UPDATE ====================
    public boolean atualizar(Emprestimo emprestimo) {
        String sql = "UPDATE emprestimos SET id_livro = ?, id_usuario = ?, data_emprestimo = ?, " +
                     "data_devolucao_prevista = ?, data_devolucao_real = ?, status = ? WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, emprestimo.getLivroId());
            stmt.setInt(2, emprestimo.getUsuarioId());
            stmt.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataEmprestimo().atStartOfDay()));
            stmt.setDate(4, Date.valueOf(emprestimo.getDataDevolucaoPrevista()));
            
            if (emprestimo.getDataDevolucaoReal() != null) {
                stmt.setDate(5, Date.valueOf(emprestimo.getDataDevolucaoReal()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setString(6, emprestimo.getStatus());
            stmt.setInt(7, emprestimo.getId());
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Empréstimo atualizado, ID: " + emprestimo.getId());
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar empréstimo: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== DELETE ====================
    public boolean deletar(int id) {
        String sql = "DELETE FROM emprestimos WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Empréstimo deletado, ID: " + id);
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao deletar empréstimo: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    // Buscar empréstimos por usuário
    public List<Emprestimo> buscarPorUsuario(int usuarioId) {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE id_usuario = ? ORDER BY data_emprestimo DESC";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(criarEmprestimoFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar empréstimos por usuário: " + e.getMessage());
        }
        
        return emprestimos;
    }
    
    // Buscar empréstimos por livro
    public List<Emprestimo> buscarPorLivro(int livroId) {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE id_livro = ? ORDER BY data_emprestimo DESC";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, livroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(criarEmprestimoFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar empréstimos por livro: " + e.getMessage());
        }
        
        return emprestimos;
    }
    
    // Buscar empréstimos ativos (não devolvidos)

    public List<Emprestimo> buscarAtivos() {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE status IN ('ATIVO', 'ATRASADO') ORDER BY data_devolucao_prevista ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                emprestimos.add(criarEmprestimoFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar empréstimos ativos: " + e.getMessage());
        }

        return emprestimos;
    }
    
    // Registrar devolução
    public boolean registrarDevolucao(int emprestimoId, Date dataDevolucaoReal, String status) {
        String sql = "UPDATE emprestimos SET data_devolucao_real = ?, status = ? WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataDevolucaoReal);
            stmt.setString(2, status);
            stmt.setInt(3, emprestimoId);
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Devolução registrada para empréstimo ID: " + emprestimoId);
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao registrar devolução: " + e.getMessage());
            return false;
        }
    }
    
    // Verificar se usuário tem empréstimos ativos
    public boolean usuarioTemEmprestimosAtivos(int usuarioId) {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE id_usuario = ? AND status IN ('ATIVO', 'ATRASADO')";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao verificar empréstimos ativos do usuário: " + e.getMessage());
        }
        
        return false;
    }
    
    // Contar empréstimos ativos
    public int contarEmprestimosAtivos() {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE status IN ('ATIVO', 'ATRASADO')";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar empréstimos ativos: " + e.getMessage());
        }
        
        return 0;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    private Emprestimo criarEmprestimoFromResultSet(ResultSet rs) throws SQLException {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(rs.getInt("id"));
        emprestimo.setLivroId(rs.getInt("id_livro"));
        emprestimo.setUsuarioId(rs.getInt("id_usuario"));
        
        // data_emprestimo é TIMESTAMP, converter para LocalDate
        Timestamp timestamp = rs.getTimestamp("data_emprestimo");
        if (timestamp != null) {
            emprestimo.setDataEmprestimo(timestamp.toLocalDateTime().toLocalDate());
        }
        
        emprestimo.setDataDevolucaoPrevista(rs.getDate("data_devolucao_prevista").toLocalDate());
        
        Date dataDevolucaoReal = rs.getDate("data_devolucao_real");
        if (dataDevolucaoReal != null) {
            emprestimo.setDataDevolucaoReal(dataDevolucaoReal.toLocalDate());
        }
        
        emprestimo.setStatus(rs.getString("status"));
        
        return emprestimo;
    }
}