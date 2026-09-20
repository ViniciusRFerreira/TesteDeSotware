package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private static ItemPedido item(int quantidade, int estoque, int pesoGramas, boolean fragil) {
        return new ItemPedido("SKU", 1_000, quantidade, estoque, pesoGramas, fragil);
    }

    @Test
    void deveRejeitarListaDeItensNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null));
    }

    @Test
    void deveRejeitarUfNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(List.of(item(1, 5, 500, false)), null, false, null));
    }

    @Test
    void listaVaziaEValidaNaConstrucao() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);
        assertEquals(0, pedido.itens().size());
    }

    @Test
    void deveRejeitarMaisDeCemItens() {
        List<ItemPedido> itens = new ArrayList<>();
        for (int i = 0; i < 101; i++) itens.add(item(1, 5, 500, false));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveAceitarUfComDuasLetrasMaiusculas() {
        Pedido pedido = new Pedido(List.of(item(1, 5, 500, false)), "SP", false, null);
        assertEquals("SP", pedido.uf());
    }

    @Test
    void deveRejeitarUfComMinuscula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(List.of(item(1, 5, 500, false)), "sp", false, null));
    }

    @Test
    void deveRejeitarUfComUmaLetra() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(List.of(item(1, 5, 500, false)), "P", false, null));
    }

    @Test
    void subtotalIgnoraItensComQuantidadeZero() {
        ItemPedido ativo = new ItemPedido("A", 1_000, 2, 5, 500, false);
        ItemPedido inativo = new ItemPedido("B", 5_000, 0, 5, 500, false);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);
        assertEquals(2_000, pedido.subtotalCentavos());
    }

    @Test
    void pesoSomaOPesoDeTodosOsItensPelaQuantidade() {
        ItemPedido a = new ItemPedido("A", 1_000, 2, 5, 500, false);
        ItemPedido b = new ItemPedido("B", 1_000, 1, 5, 300, false);
        Pedido pedido = new Pedido(List.of(a, b), "PR", false, null);
        assertEquals(1_300, pedido.pesoGramas());
    }

    @Test
    void temFragilEVerdadeiroSeAlgumItemAtivoForFragil() {
        ItemPedido fragilAtivo = new ItemPedido("A", 1_000, 1, 5, 500, true);
        Pedido pedido = new Pedido(List.of(fragilAtivo), "PR", false, null);
        assertTrue(pedido.temFragil());
    }

    @Test
    void temFragilIgnoraItemFragilComQuantidadeZero() {
        ItemPedido fragilInativo = new ItemPedido("A", 1_000, 0, 5, 500, true);
        Pedido pedido = new Pedido(List.of(fragilInativo), "PR", false, null);
        assertFalse(pedido.temFragil());
    }

    @Test
    void estoqueSuficienteEVerdadeiroQuandoTodosOsItensEstaoDisponiveis() {
        Pedido pedido = new Pedido(List.of(item(2, 5, 500, false), item(1, 1, 500, false)), "PR", false, null);
        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void estoqueSuficienteEFalsoQuandoAlgumItemNaoEstaDisponivel() {
        Pedido pedido = new Pedido(List.of(item(2, 5, 500, false), item(3, 1, 500, false)), "PR", false, null);
        assertFalse(pedido.estoqueSuficiente());
    }
}
