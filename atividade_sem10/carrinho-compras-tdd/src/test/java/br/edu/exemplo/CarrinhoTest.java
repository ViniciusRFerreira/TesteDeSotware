package br.edu.exemplo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarrinhoTest {

    // Requisito 1 — Carrinho vazio
    @Test
    void carrinhoRecemCriadoDeveTerTotalZero() {
        Carrinho carrinho = new Carrinho();

        assertEquals(0.0, carrinho.getTotal(), 0.001);
    }

    // Requisito 2 — Adicionar um produto
    @Test
    void deveAdicionarUmProdutoECalcularTotal() {
        Carrinho carrinho = new Carrinho();
        Produto teclado = new Produto("Teclado", 100.0);

        carrinho.adicionar(teclado);

        assertEquals("Teclado", teclado.getNome());
        assertEquals(100.0, carrinho.getTotal(), 0.001);
    }

    // Requisito 3 — Adicionar vários produtos
    @Test
    void deveAdicionarVariosProdutosESomarPrecos() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Teclado", 100.0));
        carrinho.adicionar(new Produto("Mouse", 50.0));

        assertEquals(150.0, carrinho.getTotal(), 0.001);
    }

    // Requisito 4 — Remover produto
    @Test
    void deveRemoverProdutoDoCarrinho() {
        Carrinho carrinho = new Carrinho();
        Produto teclado = new Produto("Teclado", 100.0);
        Produto mouse = new Produto("Mouse", 50.0);
        carrinho.adicionar(teclado);
        carrinho.adicionar(mouse);

        carrinho.remover(mouse);

        assertEquals(100.0, carrinho.getTotal(), 0.001);
    }

    @Test
    void removerProdutoInexistenteNaoDeveAlterarTotal() {
        Carrinho carrinho = new Carrinho();
        Produto teclado = new Produto("Teclado", 100.0);
        Produto mouse = new Produto("Mouse", 50.0);
        carrinho.adicionar(teclado);

        carrinho.remover(mouse);

        assertEquals(100.0, carrinho.getTotal(), 0.001);
    }

    // Requisito 5 — Produto com preço inválido (e produto nulo)
    @Test
    void naoDevePermitirAdicionarProdutoComPrecoNegativo() {
        Carrinho carrinho = new Carrinho();
        Produto tecladoInvalido = new Produto("Teclado", -100.0);

        assertThrows(IllegalArgumentException.class, () -> carrinho.adicionar(tecladoInvalido));
        assertEquals(0.0, carrinho.getTotal(), 0.001);
    }

    @Test
    void naoDevePermitirAdicionarProdutoNulo() {
        Carrinho carrinho = new Carrinho();

        assertThrows(IllegalArgumentException.class, () -> carrinho.adicionar(null));
        assertEquals(0.0, carrinho.getTotal(), 0.001);
    }

    @Test
    void devePermitirAdicionarProdutoComPrecoZero() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Brinde", 0.0));

        assertEquals(0.0, carrinho.getTotal(), 0.001);
    }

    // Requisito 6 — Desconto de 10% para compras >= R$ 500,00 e valores de fronteira
    @Test
    void deveAplicarDescontoDeDezPorCentoParaSubtotalDeSeiscentosReais() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Monitor", 600.0));

        assertEquals(540.0, carrinho.getTotal(), 0.001);
    }

    @Test
    void naoDeveAplicarDescontoNoLimiteInferiorDe499e99() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Headset", 499.99));

        assertEquals(499.99, carrinho.getTotal(), 0.001);
    }

    @Test
    void deveAplicarDescontoNoLimiteExatoDe500Reais() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Cadeira", 500.00));

        assertEquals(450.00, carrinho.getTotal(), 0.001);
    }

    @Test
    void deveAplicarDescontoNoLimiteSuperiorDe500e01() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(new Produto("Teclado Mecânico", 500.01));

        assertEquals(450.009, carrinho.getTotal(), 0.001);
    }

    @Test
    void deveRemoverDescontoQuandoSubtotalFicaAbaixoDe500AposRemocao() {
        Carrinho carrinho = new Carrinho();
        Produto monitor = new Produto("Monitor", 450.0);
        Produto mouse = new Produto("Mouse", 100.0);
        carrinho.adicionar(monitor);
        carrinho.adicionar(mouse);
        assertEquals(495.0, carrinho.getTotal(), 0.001);

        carrinho.remover(mouse);

        assertEquals(450.0, carrinho.getTotal(), 0.001);
    }
}
