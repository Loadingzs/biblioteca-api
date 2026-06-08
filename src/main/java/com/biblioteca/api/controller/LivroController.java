package com.biblioteca.api.controller;

import com.biblioteca.api.domain.Livro;
import com.biblioteca.api.service.LivroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
@CrossOrigin(origins = "*")
public class LivroController {

    @Autowired
    private LivroService livroService;

    @GetMapping
    public List<Livro> listarTodos() {
        return livroService.listarTodosLivros();
    }

    @GetMapping("/{id}")
    public Livro buscarPorId(@PathVariable int id) {
        return livroService.buscarLivroPorId(id);
    }

    @PostMapping
    public boolean cadastrar(@RequestBody Livro livro) {
        return livroService.cadastrarLivro(livro);
    }

    @PutMapping("/{id}")
    public boolean atualizar(@PathVariable int id, @RequestBody Livro livro) {
        livro.setId(id);
        return livroService.atualizarLivro(livro);
    }

    @DeleteMapping("/{id}")
    public boolean deletar(@PathVariable int id) {
        return livroService.deletarLivro(id);
    }
}