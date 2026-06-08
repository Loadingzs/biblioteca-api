package com.biblioteca.api.domain;

import java.time.LocalDate;

public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String genero;
    private boolean disponivel;
    private LocalDate dataCadastro;
    
    // Construtor vazio (necessário para alguns frameworks)
    public Livro() {}
    
    // Construtor completo (sem id, para novos livros)
    public Livro(String titulo, String autor, String isbn, String genero, boolean disponivel) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.genero = genero;
        this.disponivel = disponivel;
        this.dataCadastro = LocalDate.now();
    }
    
    // Construtor com id (para livros existentes no banco)
    public Livro(int id, String titulo, String autor, String isbn, String genero, boolean disponivel, LocalDate dataCadastro) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.genero = genero;
        this.disponivel = disponivel;
        this.dataCadastro = dataCadastro;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public void setAutor(String autor) {
        this.autor = autor;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public boolean isDisponivel() {
        return disponivel;
    }
    
    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
    
    public LocalDate getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    @Override
    public String toString() {
        return "Livro [id=" + id + ", titulo=" + titulo + ", autor=" + autor + 
               ", isbn=" + isbn + ", genero=" + genero + ", disponivel=" + disponivel + 
               ", dataCadastro=" + dataCadastro + "]";
    }
}