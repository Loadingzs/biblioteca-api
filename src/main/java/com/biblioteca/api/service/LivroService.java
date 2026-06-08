package com.biblioteca.api.service;

import com.biblioteca.api.domain.Livro;
import com.biblioteca.api.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service  // ← ADICIONE ESTA LINHA

public class LivroService {
    
    @Autowired
    private LivroRepository livroRepository;
    
    public LivroService() {
        this.livroRepository = new LivroRepository();
    }
    // Método auxiliar para normalizar ISBN (remove hífens e espaços)
    private String normalizarIsbn(String isbn) {
        if (isbn == null) return null;
        return isbn.replaceAll("[-\\s]", ""); // Remove hífens e espaços
    }
    // ==================== REGRAS DE NEGÓCIO ====================
    
    // Validar dados do livro antes de salvar
    private boolean validarLivro(Livro livro) {
        if (livro == null) {
            System.err.println("❌ Livro não pode ser nulo");
            return false;
        }
        
        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            System.err.println("❌ Título é obrigatório");
            return false;
        }
        
        if (livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
            System.err.println("❌ Autor é obrigatório");
            return false;
        }
        
        if (livro.getIsbn() == null || livro.getIsbn().trim().isEmpty()) {
            System.err.println("❌ ISBN é obrigatório");
            return false;
        }
        
        return true;
    }
    
    // Verificar se ISBN já existe (evitar duplicação)
    // Substitua o método isbnJaExiste no LivroService.java por este:

    public boolean isbnJaExiste(String isbn) {
        String isbnNormalizado = normalizarIsbn(isbn);

        List<Livro> livros = livroRepository.buscarTodos();
        for (Livro livro : livros) {
            String isbnExistente = normalizarIsbn(livro.getIsbn());
            if (isbnExistente != null && isbnExistente.equals(isbnNormalizado)) {
                return true;
            }
        }
        return false;
    }

    public boolean cadastrarLivro(Livro livro) {
        if (!validarLivro(livro)) {
            return false;
        }

        // Verifica ignorando hífens
        if (isbnJaExiste(livro.getIsbn())) {
            System.err.println("❌ ISBN já cadastrado (ignorando hífens): " + livro.getIsbn());
            return false;
        }

        livro.setDisponivel(true);
        return livroRepository.salvar(livro);
    }
    
    public List<Livro> listarTodosLivros() {
        return livroRepository.buscarTodos();
    }
    
    public Livro buscarLivroPorId(int id) {
        if (id <= 0) {
            System.err.println("❌ ID inválido");
            return null;
        }
        return livroRepository.buscarPorId(id);
    }
    
    public boolean atualizarLivro(Livro livro) {
        if (!validarLivro(livro)) {
            return false;
        }
        
        if (livro.getId() <= 0) {
            System.err.println("❌ ID do livro inválido para atualização");
            return false;
        }
        
        // Verificar se o livro existe
        Livro existente = livroRepository.buscarPorId(livro.getId());
        if (existente == null) {
            System.err.println("❌ Livro não encontrado para atualização");
            return false;
        }
        
        return livroRepository.atualizar(livro);
    }
    
    public boolean deletarLivro(int id) {
        if (id <= 0) {
            System.err.println("❌ ID inválido");
            return false;
        }
        
        // Verificar se o livro existe
        Livro livro = livroRepository.buscarPorId(id);
        if (livro == null) {
            System.err.println("❌ Livro não encontrado para exclusão");
            return false;
        }
        
        // TODO: Verificar se o livro não está emprestado antes de deletar
        // (será implementado quando tivermos o EmprestimoService)
        
        return livroRepository.deletar(id);
    }
    
    // ==================== MÉTODOS DE BUSCA ESPECÍFICOS ====================
    
    public List<Livro> buscarLivrosPorTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            System.err.println("❌ Título não pode ser vazio");
            return List.of(); // Retorna lista vazia
        }
        return livroRepository.buscarPorTitulo(titulo);
    }
    
    public List<Livro> buscarLivrosPorAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            System.err.println("❌ Autor não pode ser vazio");
            return List.of();
        }
        return livroRepository.buscarPorAutor(autor);
    }
    
    public List<Livro> buscarLivrosPorIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("❌ ISBN não pode ser vazio");
            return List.of();
        }
        return livroRepository.buscarPorIsbn(isbn);
    }
    
    public List<Livro> buscarLivrosDisponiveis() {
        List<Livro> todos = livroRepository.buscarTodos();
        return todos.stream()
                .filter(Livro::isDisponivel)
                .toList();
    }
    
    public boolean verificarDisponibilidade(int idLivro) {
        Livro livro = buscarLivroPorId(idLivro);
        if (livro == null) {
            return false;
        }
        return livro.isDisponivel();
    }
}