package com.biblioteca.api.repository;

import com.biblioteca.api.domain.Livro;
import com.biblioteca.api.infrastructure.ConnectionFactory;
import org.springframework.stereotype.Repository;  // ← ADICIONE
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository  // ← ADICIONE ESTA LINHA

public class LivroRepository {
    
    // ==================== CREATE ====================
    public boolean salvar(Livro livro) {
        String sql = "INSERT INTO livros (titulo, autor, isbn, disponivel) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setString(3, livro.getIsbn());
            stmt.setBoolean(4, livro.isDisponivel());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        livro.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Livro salvo: " + livro.getTitulo());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao salvar livro: " + e.getMessage());
            return false;
        }
    }
    
    // Buscar livro por ISBN exato (comparação estrita)
public Livro buscarPorIsbnExato(String isbn) {
    String sql = "SELECT * FROM livros WHERE isbn = ?";
    Livro livro = null;
    
    try (Connection conn = ConnectionFactory.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, isbn);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                livro = criarLivroFromResultSet(rs);
            }
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Erro ao buscar livro por ISBN exato: " + e.getMessage());
    }
    
    return livro;
}
    
    // ==================== READ (TODOS) ====================
    public List<Livro> buscarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                livros.add(criarLivroFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar livros: " + e.getMessage());
        }
        
        return livros;
    }
    
    // ==================== READ (POR ID) ====================
    public Livro buscarPorId(int id) {
        String sql = "SELECT * FROM livros WHERE id = ?";
        Livro livro = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    livro = criarLivroFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar livro por ID: " + e.getMessage());
        }
        
        return livro;
    }
    
    // ==================== UPDATE ====================
    public boolean atualizar(Livro livro) {
        String sql = "UPDATE livros SET titulo = ?, autor = ?, isbn = ?, disponivel = ? WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setString(3, livro.getIsbn());
            stmt.setBoolean(4, livro.isDisponivel());
            stmt.setInt(5, livro.getId());
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Livro atualizado: " + livro.getTitulo());
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar livro: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== DELETE ====================
    public boolean deletar(int id) {
        String sql = "DELETE FROM livros WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Livro deletado, ID: " + id);
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao deletar livro: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    public List<Livro> buscarPorTitulo(String titulo) {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE titulo LIKE ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + titulo + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(criarLivroFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar livro por título: " + e.getMessage());
        }
        
        return livros;
    }
    
    public List<Livro> buscarPorAutor(String autor) {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE autor LIKE ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + autor + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(criarLivroFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar livro por autor: " + e.getMessage());
        }
        
        return livros;
    }
    
    public List<Livro> buscarPorIsbn(String isbn) {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE isbn LIKE ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + isbn + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(criarLivroFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar livro por ISBN: " + e.getMessage());
        }
        
        return livros;
    }
    
    public boolean atualizarDisponibilidade(int idLivro, boolean disponivel) {
        String sql = "UPDATE livros SET disponivel = ? WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, disponivel);
            stmt.setInt(2, idLivro);
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            String status = disponivel ? "disponível" : "indisponível";
            if (sucesso) {
                System.out.println("✅ Disponibilidade do livro ID " + idLivro + " atualizada para: " + status);
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar disponibilidade: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    private Livro criarLivroFromResultSet(ResultSet rs) throws SQLException {
        Livro livro = new Livro();
        livro.setId(rs.getInt("id"));
        livro.setTitulo(rs.getString("titulo"));
        livro.setAutor(rs.getString("autor"));
        livro.setIsbn(rs.getString("isbn"));
        livro.setDisponivel(rs.getBoolean("disponivel"));
        
        if (rs.getDate("data_cadastro") != null) {
            livro.setDataCadastro(rs.getDate("data_cadastro").toLocalDate());
        }
        
        return livro;
    }
}