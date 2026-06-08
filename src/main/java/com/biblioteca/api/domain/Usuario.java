package com.biblioteca.api.domain;

import java.time.LocalDate;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String senha;           // NOVO - senha criptografada
    private String role;            // NOVO - ADMIN ou USER
    private LocalDate dataCadastro;
    
    // Construtor vazio (necessário para alguns frameworks)
    public Usuario() {}
    
    // Construtor sem id (para novos usuários)
    public Usuario(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.role = "USER";  // Padrão: usuário comum
        this.dataCadastro = LocalDate.now();
    }
    
    // NOVO CONSTRUTOR com senha e role (para cadastro com autenticação)
    public Usuario(String nome, String email, String telefone, String senha, String role) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.role = role != null ? role : "USER";
        this.dataCadastro = LocalDate.now();
    }
    
    // Construtor com id (para usuários existentes no banco)
    public Usuario(int id, String nome, String email, String telefone, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
    }
    
    // NOVO CONSTRUTOR completo (para usuários existentes com senha e role)
    public Usuario(int id, String nome, String email, String telefone, String senha, String role, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.role = role;
        this.dataCadastro = dataCadastro;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    // NOVO GETTER E SETTER para senha
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    // NOVO GETTER E SETTER para role
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDate getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    // Validação de email
    public boolean isEmailValido() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    // NOVO - Verifica se é admin
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nome=" + nome + ", email=" + email + 
               ", telefone=" + telefone + ", role=" + role + 
               ", dataCadastro=" + dataCadastro + "]";
    }
}