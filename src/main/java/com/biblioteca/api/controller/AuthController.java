package com.biblioteca.api.controller;

import com.biblioteca.api.config.JwtUtil;
import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.dto.AuthRequest;
import com.biblioteca.api.dto.AuthResponse;
import com.biblioteca.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final String dummyHash;

    public AuthController(JwtUtil jwtUtil, UsuarioRepository usuarios, PasswordEncoder passwords) {
        this.jwtUtil = jwtUtil;
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getSenha() == null || request.getSenha().isBlank()
                || request.getSenha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios");
        }
        Usuario usuario = usuarios.buscarPorEmail(request.getEmail().trim());
        String stored = usuario == null ? null : usuario.getSenha();
        boolean bcrypt = stored != null && stored.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
        boolean matches = passwords.matches(request.getSenha(), bcrypt ? stored : dummyHash);
        if (!bcrypt || !matches || usuario == null
                || !("ADMIN".equals(usuario.getRole()) || "USER".equals(usuario.getRole()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRole());
        return new AuthResponse(token, usuario.getEmail(), usuario.getRole());
    }
}
