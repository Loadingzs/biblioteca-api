package com.biblioteca.api.service;

import com.biblioteca.api.repository.EmprestimoRepository;
import com.biblioteca.api.repository.LivroRepository;
import com.biblioteca.api.repository.UsuarioRepository;
import com.biblioteca.api.domain.Emprestimo;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class EmprestimoServiceTest {

    private final EmprestimoRepository emprestimos = Mockito.mock(EmprestimoRepository.class);
    private final LivroRepository livros = Mockito.mock(LivroRepository.class);
    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final EmprestimoService service = new EmprestimoService(emprestimos, livros, usuarios);

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void deveRecusarRenovacaoSemDiasPositivos(int dias) {
        LocalDate prazo = LocalDate.of(2026, 12, 10);
        Emprestimo emprestimo = new Emprestimo(1, 2, prazo.minusDays(7), prazo);
        when(emprestimos.buscarPorId(42)).thenReturn(emprestimo);
        when(emprestimos.atualizar(emprestimo)).thenReturn(true);

        assertFalse(service.renovarEmprestimo(42, dias));
        assertEquals(prazo, emprestimo.getDataDevolucaoPrevista());
        verifyNoInteractions(emprestimos, livros, usuarios);
    }

    @Test
    void deveAcrescentarDiasAoPrazoExistenteNaRenovacao() {
        LocalDate prazo = LocalDate.of(2026, 12, 10);
        Emprestimo emprestimo = new Emprestimo(1, 2, prazo.minusDays(7), prazo);
        emprestimo.setId(42);
        when(emprestimos.buscarPorId(42)).thenReturn(emprestimo);
        when(emprestimos.atualizar(emprestimo)).thenReturn(true);

        assertTrue(service.renovarEmprestimo(42, 7));

        assertEquals(prazo.plusDays(7), emprestimo.getDataDevolucaoPrevista());
        assertEquals("ATIVO", emprestimo.getStatus());
        verify(emprestimos).atualizar(emprestimo);
        verifyNoInteractions(livros, usuarios);
    }

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
