package com.biblioteca.api.controller;

import com.biblioteca.api.domain.Emprestimo;
import com.biblioteca.api.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emprestimos")
@CrossOrigin(origins = "*")
public class EmprestimoController {

    @Autowired
    private EmprestimoService emprestimoService;

    @GetMapping
    public List<Emprestimo> listarTodos() {
        return emprestimoService.listarTodosEmprestimos();
    }

    @GetMapping("/ativos")
    public List<Emprestimo> listarAtivos() {
        return emprestimoService.buscarEmprestimosAtivos();
    }

    @PostMapping
    public boolean realizarEmprestimo(@RequestBody Map<String, Object> dados) {
        int livroId = (int) dados.get("livroId");
        int usuarioId = (int) dados.get("usuarioId");
        int dias = (int) dados.get("dias");
        return emprestimoService.realizarEmprestimo(livroId, usuarioId, dias);
    }

    @PutMapping("/devolucao/{id}")
    public boolean registrarDevolucao(@PathVariable int id) {
        return emprestimoService.registrarDevolucao(id);
    }

    @PutMapping("/renovar/{id}")
    public boolean renovar(@PathVariable int id, @RequestBody Map<String, Integer> dados) {
        int dias = dados.get("dias");
        return emprestimoService.renovarEmprestimo(id, dias);
    }
}