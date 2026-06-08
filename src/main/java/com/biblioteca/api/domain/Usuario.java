package com.biblioteca.api.domain;

import java.time.LocalDate;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataCadastro;
    
    // Construtor vazio (necessário para alguns frameworks)
    public Usuario() {}
    
    // Construtor sem id (para novos usuários)
    public Usuario(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
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
    
    public LocalDate getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    // Validação de email (regra de negócio - será movida para Service depois)
    public boolean isEmailValido() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nome=" + nome + ", email=" + email + 
               ", telefone=" + telefone + ", dataCadastro=" + dataCadastro + "]";
    }
}