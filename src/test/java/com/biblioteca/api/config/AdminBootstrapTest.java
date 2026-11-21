package com.biblioteca.api.config;

import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    @Test
    void disabledBootstrapDoesNotTouchDatabase() {
        new AdminBootstrap(usuarios, passwords, false, "", "").run();
        verifyNoInteractions(usuarios);
    }

    @Test
    void validatesConfigurationBeforeAccessingDatabase() {
        assertThrows(IllegalStateException.class,
                () -> new AdminBootstrap(usuarios, passwords, true, "invalido", "curta").run());
        verifyNoInteractions(usuarios);
    }

    @Test
    void refusesToOverwriteOrPromoteExistingAccount() {
        when(usuarios.emailExiste("admin@example.com")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> new AdminBootstrap(
                usuarios, passwords, true, "admin@example.com", "SenhaInicialValida!").run());
        verify(usuarios, never()).salvar(any());
        verify(usuarios, never()).atualizarSenha(anyInt(), anyString());
    }

    @Test
    void createsAdminWithEncodedPasswordAndChecksPersistence() {
        when(usuarios.salvar(any())).thenReturn(true);
        new AdminBootstrap(usuarios, passwords, true, "admin@example.com", "SenhaInicialValida!").run();
        var capture = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).salvar(capture.capture());
        assertEquals("ADMIN", capture.getValue().getRole());
        assertTrue(passwords.matches("SenhaInicialValida!", capture.getValue().getSenha()));
    }

    @Test
    void failsIfAdminCannotBeSaved() {
        assertThrows(IllegalStateException.class, () -> new AdminBootstrap(
                usuarios, passwords, true, "admin@example.com", "SenhaInicialValida!").run());
    }

    @Test
    void requiresOwnJwtSecretAndPositiveLifetime() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("", 1000));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("curta", 1000));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("x".repeat(32), 0));
    }
}
