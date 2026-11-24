package com.biblioteca.api.service;

import com.biblioteca.api.domain.Livro;
import com.biblioteca.api.repository.LivroRepository;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LivroServiceTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cadastroPreservaDisponibilidadeInformada(boolean disponivel) {
        LivroRepository repository = mock(LivroRepository.class);
        LivroService service = new LivroService(repository);
        Livro livro = new Livro("Livro de teste", "Autor", "9780000000001", null, disponivel);
        when(repository.buscarTodos()).thenReturn(List.of());
        when(repository.salvar(livro)).thenReturn(true);

        assertTrue(service.cadastrarLivro(livro));

        assertEquals(disponivel, livro.isDisponivel());
        verify(repository).salvar(livro);
    }
}
