package com.biblioteca.api.repository;

import com.biblioteca.api.domain.Usuario;  // ← ADICIONE ESTE IMPORT
import com.biblioteca.api.infrastructure.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {
    
    // ==================== CREATE ====================
    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, telefone) VALUES (?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Usuário salvo: " + usuario.getNome());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("❌ Email já cadastrado: " + usuario.getEmail());
            } else {
                System.err.println("❌ Erro ao salvar usuário: " + e.getMessage());
            }
            return false;
        }
    }
    
    // ==================== READ (TODOS) ====================
    public List<Usuario> buscarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(criarUsuarioFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar usuários: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    // ==================== READ (POR ID) ====================
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        Usuario usuario = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = criarUsuarioFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar usuário por ID: " + e.getMessage());
        }
        
        return usuario;
    }
    
    // ==================== UPDATE ====================
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, telefone = ? WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            stmt.setInt(4, usuario.getId());
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Usuário atualizado: " + usuario.getNome());
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== DELETE ====================
    public boolean deletar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            boolean sucesso = rowsAffected > 0;
            
            if (sucesso) {
                System.out.println("✅ Usuário deletado, ID: " + id);
            }
            
            return sucesso;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao deletar usuário: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
    public List<Usuario> buscarPorNome(String nome) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE nome LIKE ? ORDER BY nome";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nome + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(criarUsuarioFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar usuário por nome: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        Usuario usuario = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = criarUsuarioFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar usuário por email: " + e.getMessage());
        }
        
        return usuario;
    }
    
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao verificar email: " + e.getMessage());
        }
        
        return false;
    }
    
    public int contar() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar usuários: " + e.getMessage());
        }
        
        return 0;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    private Usuario criarUsuarioFromResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefone(rs.getString("telefone"));
        
        if (rs.getDate("data_cadastro") != null) {
            usuario.setDataCadastro(rs.getDate("data_cadastro").toLocalDate());
        }
        
        return usuario;
    }
}