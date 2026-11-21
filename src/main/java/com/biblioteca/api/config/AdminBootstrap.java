package com.biblioteca.api.config;

import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final boolean enabled;
    private final String email;
    private final String password;

    public AdminBootstrap(UsuarioRepository usuarios, PasswordEncoder passwords,
            @Value("${app.bootstrap.enabled:false}") boolean enabled,
            @Value("${app.bootstrap.email:}") String email,
            @Value("${app.bootstrap.password:}") String password) {
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.enabled = enabled;
        this.email = email.trim();
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || password.isBlank() || password.length() < 12 || bytes > 72) {
            throw new IllegalStateException("Defina BOOTSTRAP_ADMIN_EMAIL e uma senha de 12 caracteres a 72 bytes.");
        }
        if (usuarios.emailExiste(email)) {
            throw new IllegalStateException("O e-mail de bootstrap já existe. Desative o bootstrap ou use outro e-mail.");
        }
        Usuario admin = new Usuario("Administrador", email, "Não informado",
                passwords.encode(password), "ADMIN");
        if (!usuarios.salvar(admin)) {
            throw new IllegalStateException("Não foi possível criar o administrador. Confira a configuração e o esquema do banco.");
        }
    }
}
