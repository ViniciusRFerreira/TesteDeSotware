package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveRejeitarSkuNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(null, 1_000, 1, 5, 500, false));
    }

    @Test
    void deveRejeitarSkuEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("   ", 1_000, 1, 5, 500, false));
    }

    @Test
    void deveAceitarPrecoNoLimiteInferior() {
        ItemPedido item = new ItemPedido("A", 1, 1, 5, 500, false);
        assertEquals(1, item.precoCentavos());
    }

    @Test
    void deveRejeitarPrecoZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 0, 1, 5, 500, false));
    }

    @Test
    void deveAceitarPrecoNoLimiteSuperior() {
        ItemPedido item = new ItemPedido("A", 1_000_000, 1, 5, 500, false);
        assertEquals(1_000_000, item.precoCentavos());
    }

    @Test
    void deveRejeitarPrecoAcimaDoLimiteSuperior() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000_001, 1, 5, 500, false));
    }

    @Test
    void deveAceitarQuantidadeZeroComoLinhaInativa() {
        ItemPedido item = new ItemPedido("A", 1_000, 0, 5, 500, false);
        assertEquals(0, item.quantidade());
    }

    @Test
    void deveRejeitarQuantidadeNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000, -1, 5, 500, false));
    }

    @Test
    void deveAceitarQuantidadeNoLimiteSuperior() {
        ItemPedido item = new ItemPedido("A", 1_000, 100, 100, 500, false);
        assertEquals(100, item.quantidade());
    }

    @Test
    void deveRejeitarQuantidadeAcimaDoLimiteSuperior() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000, 101, 200, 500, false));
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000, 1, -1, 500, false));
    }

    @Test
    void deveRejeitarPesoZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000, 1, 5, 0, false));
    }

    @Test
    void deveRejeitarPesoAcimaDoLimiteSuperior() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("A", 1_000, 1, 5, 100_001, false));
    }

    @Test
    void totalCentavosMultiplicaPrecoPelaQuantidade() {
        ItemPedido item = new ItemPedido("A", 1_500, 3, 5, 500, false);
        assertEquals(4_500, item.totalCentavos());
    }

    @Test
    void disponivelEVerdadeiroQuandoQuantidadeNaoPassaDoEstoque() {
        ItemPedido item = new ItemPedido("A", 1_000, 5, 5, 500, false);
        assertTrue(item.disponivel());
    }

    @Test
    void disponivelEFalsoQuandoQuantidadePassaDoEstoque() {
        ItemPedido item = new ItemPedido("A", 1_000, 6, 5, 500, false);
        assertFalse(item.disponivel());
    }
}
