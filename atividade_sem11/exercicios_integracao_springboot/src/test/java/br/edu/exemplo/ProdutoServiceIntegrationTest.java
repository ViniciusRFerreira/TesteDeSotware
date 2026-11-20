package br.edu.exemplo;

import br.edu.exemplo.model.Produto;
import br.edu.exemplo.repository.ProdutoRepository;
import br.edu.exemplo.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProdutoServiceIntegrationTest {

    @Autowired
    ProdutoService service;

    @Autowired
    ProdutoRepository repository;

    @Test
    void deveCadastrarERecuperarProduto() {
        // CT01 — Cadastro válido
        Produto salvo = service.cadastrar("Teclado", 150.0);

        assertNotNull(salvo.getId());

        Optional<Produto> recuperado = repository.findById(salvo.getId());
        assertTrue(recuperado.isPresent());
        assertEquals("Teclado", recuperado.get().getNome());
        assertEquals(150.0, recuperado.get().getPreco(), 0.001);
    }

    @Test
    void naoDevePersistirProdutoComNomeEmBranco() {
        // CT02 — Nome inválido
        long contagemInicial = repository.count();

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar("   ", 80.0)
        );

        assertEquals("Nome obrigatório", excecao.getMessage());
        assertEquals(contagemInicial, repository.count());
    }

    @Test
    void deveAtualizarPrecoPersistido() {
        // CT03 — Atualização (desafio)
        Produto salvo = service.cadastrar("Mouse", 80.0);
        assertNotNull(salvo.getId());

        service.atualizarPreco(salvo.getId(), 95.0);
        repository.flush();

        Optional<Produto> atualizado = repository.findById(salvo.getId());
        assertTrue(atualizado.isPresent());
        assertEquals("Mouse", atualizado.get().getNome());
        assertEquals(95.0, atualizado.get().getPreco(), 0.001);
    }

    @Test
    void naoDevePersistirProdutoComPrecoInvalido() {
        long contagemInicial = repository.count();

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar("Monitor", 0.0)
        );

        assertEquals("Preço deve ser positivo", excecao.getMessage());
        assertEquals(contagemInicial, repository.count());
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscar(999999L)
        );

        assertEquals("Produto não encontrado", excecao.getMessage());
    }

    @Test
    void naoDeveAtualizarComPrecoNaoPositivo() {
        Produto salvo = service.cadastrar("Headset", 200.0);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPreco(salvo.getId(), -10.0)
        );

        assertEquals("Preço deve ser positivo", excecao.getMessage());
        assertEquals(200.0, repository.findById(salvo.getId()).orElseThrow().getPreco(), 0.001);
    }
}
