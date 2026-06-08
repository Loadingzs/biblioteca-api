package com.biblioteca.api.service;

import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service  // ← ADICIONE ESTA LINHA

public class UsuarioService {
    
    private UsuarioRepository usuarioRepository;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }
    
    // ==================== REGRAS DE NEGÓCIO ====================
    
    // Validar dados do usuário
    private boolean validarUsuario(Usuario usuario) {
        if (usuario == null) {
            System.err.println("❌ Usuário não pode ser nulo");
            return false;
        }
        
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            System.err.println("❌ Nome é obrigatório");
            return false;
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            System.err.println("❌ Email é obrigatório");
            return false;
        }
        
        if (!isEmailValido(usuario.getEmail())) {
            System.err.println("❌ Email inválido: " + usuario.getEmail());
            return false;
        }
        
        if (usuario.getTelefone() == null || usuario.getTelefone().trim().isEmpty()) {
            System.err.println("❌ Telefone é obrigatório");
            return false;
        }
        
        return true;
    }
    
    // Validar formato do email
    private boolean isEmailValido(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
    
    // Verificar se email já está cadastrado
    public boolean emailJaCadastrado(String email) {
        return usuarioRepository.emailExiste(email);
    }
    
    // ==================== CRUD COM VALIDAÇÃO ====================
    
    public boolean cadastrarUsuario(Usuario usuario) {
        if (!validarUsuario(usuario)) {
            return false;
        }
        
        if (emailJaCadastrado(usuario.getEmail())) {
            System.err.println("❌ Email já cadastrado: " + usuario.getEmail());
            return false;
        }
        
        return usuarioRepository.salvar(usuario);
    }
    
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.buscarTodos();
    }
    
    public Usuario buscarUsuarioPorId(int id) {
        if (id <= 0) {
            System.err.println("❌ ID inválido");
            return null;
        }
        return usuarioRepository.buscarPorId(id);
    }
    
    public boolean atualizarUsuario(Usuario usuario) {
        if (!validarUsuario(usuario)) {
            return false;
        }
        
        if (usuario.getId() <= 0) {
            System.err.println("❌ ID do usuário inválido para atualização");
            return false;
        }
        
        // Verificar se o usuário existe
        Usuario existente = usuarioRepository.buscarPorId(usuario.getId());
        if (existente == null) {
            System.err.println("❌ Usuário não encontrado para atualização");
            return false;
        }
        
        // Se o email foi alterado, verificar se já não está em uso por outro usuário
        if (!existente.getEmail().equals(usuario.getEmail()) && 
            emailJaCadastrado(usuario.getEmail())) {
            System.err.println("❌ Email já está em uso por outro usuário: " + usuario.getEmail());
            return false;
        }
        
        return usuarioRepository.atualizar(usuario);
    }
    
    public boolean deletarUsuario(int id) {
        if (id <= 0) {
            System.err.println("❌ ID inválido");
            return false;
        }
        
        // Verificar se o usuário existe
        Usuario usuario = usuarioRepository.buscarPorId(id);
        if (usuario == null) {
            System.err.println("❌ Usuário não encontrado para exclusão");
            return false;
        }
        
        // TODO: Verificar se o usuário não tem empréstimos ativos antes de deletar
        // (será implementado quando tivermos o EmprestimoService)
        
        return usuarioRepository.deletar(id);
    }
    
    // ==================== MÉTODOS DE BUSCA ESPECÍFICOS ====================
    
    public List<Usuario> buscarUsuariosPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("❌ Nome não pode ser vazio");
            return List.of();
        }
        return usuarioRepository.buscarPorNome(nome);
    }
    
    public Usuario buscarUsuarioPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("❌ Email não pode ser vazio");
            return null;
        }
        return usuarioRepository.buscarPorEmail(email);
    }
    
    public int getTotalUsuarios() {
        return usuarioRepository.contar();
    }
}