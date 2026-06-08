package com.biblioteca.api.domain;

import java.time.LocalDate;

public class Emprestimo {
    private int id;
    private int livroId;
    private int usuarioId;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoReal;
    private String status;  // "ATIVO", "DEVOLVIDO", "ATRASADO"
    
    // Construtor vazio (necessário para alguns frameworks)
    public Emprestimo() {}
    
    // Construtor para novo empréstimo (sem id e sem devolução)
    public Emprestimo(int livroId, int usuarioId, LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista) {
        this.livroId = livroId;
        this.usuarioId = usuarioId;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.status = "ATIVO";
    }
    
    // Construtor completo (para empréstimos existentes no banco)
    public Emprestimo(int id, int livroId, int usuarioId, LocalDate dataEmprestimo, 
                      LocalDate dataDevolucaoPrevista, LocalDate dataDevolucaoReal, String status) {
        this.id = id;
        this.livroId = livroId;
        this.usuarioId = usuarioId;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.status = status;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getLivroId() {
        return livroId;
    }
    
    public void setLivroId(int livroId) {
        this.livroId = livroId;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }
    
    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }
    
    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }
    
    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }
    
    public LocalDate getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }
    
    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) {
        this.dataDevolucaoReal = dataDevolucaoReal;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Emprestimo [id=" + id + ", livroId=" + livroId + ", usuarioId=" + usuarioId + 
               ", dataEmprestimo=" + dataEmprestimo + ", dataDevolucaoPrevista=" + dataDevolucaoPrevista + 
               ", dataDevolucaoReal=" + dataDevolucaoReal + ", status=" + status + "]";
    }
}