package com.biblioteca.api.service;

import com.biblioteca.api.repository.EmprestimoRepository;
import com.biblioteca.api.repository.LivroRepository;
import com.biblioteca.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;

class EmprestimoServiceTest {

    @Test
    void deveRecusarEmprestimoComDiasInvalidos() {
        EmprestimoRepository emprestimoRepository = Mockito.mock(EmprestimoRepository.class);
        LivroRepository livroRepository = Mockito.mock(LivroRepository.class);
        UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);

        EmprestimoService service = new EmprestimoService(emprestimoRepository, livroRepository, usuarioRepository);

        assertFalse(service.realizarEmprestimo(1, 1, 0));
        verifyNoInteractions(emprestimoRepository);
        verifyNoInteractions(livroRepository);
        verifyNoInteractions(usuarioRepository);
    }
}
