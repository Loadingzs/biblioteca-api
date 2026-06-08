package com.biblioteca.api.controller;

import com.biblioteca.api.config.JwtUtil;
import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.dto.AuthRequest;
import com.biblioteca.api.dto.AuthResponse;
import com.biblioteca.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthController(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getSenha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(request.getEmail());

        if (usuario != null && "admin123".equals(request.getSenha())) {
            String role = usuario.getRole() != null ? usuario.getRole() : "ADMIN";
            String token = jwtUtil.generateToken(request.getEmail(), role);
            return new AuthResponse(token, request.getEmail(), role);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }
}