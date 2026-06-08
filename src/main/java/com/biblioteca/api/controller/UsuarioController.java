package com.biblioteca.api.controller;

import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodosUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable int id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @PostMapping
    public boolean cadastrar(@RequestBody Usuario usuario) {
        return usuarioService.cadastrarUsuario(usuario);
    }

    @PutMapping("/{id}")
    public boolean atualizar(@PathVariable int id, @RequestBody Usuario usuario) {
        usuario.setId(id);
        return usuarioService.atualizarUsuario(usuario);
    }

    @DeleteMapping("/{id}")
    public boolean deletar(@PathVariable int id) {
        return usuarioService.deletarUsuario(id);
    }
}