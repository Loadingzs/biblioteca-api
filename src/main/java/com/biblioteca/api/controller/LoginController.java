package com.biblioteca.api.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");
        
        boolean autenticado = "admin@biblioteca.com".equals(email) && "admin123".equals(senha);
        
        return Map.of(
            "success", autenticado,
            "message", autenticado ? "Login realizado com sucesso" : "Email ou senha inválidos"
        );
    }
}