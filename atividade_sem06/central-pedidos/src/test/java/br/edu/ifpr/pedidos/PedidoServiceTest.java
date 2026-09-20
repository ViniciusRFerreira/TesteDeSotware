package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {
    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        // 1. Preparar: cliente comum, uma compra anterior e item disponível de R$ 100,00.
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        // Simula o pagamento e registra as cobranças, sem banco ou serviço externo.
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        // 2. Executar: percorrer um caminho completo do fechamento.
        ResultadoPedido resultado = service.fechar(pedido, cliente);

        // 3. Verificar: sem desconto; frete de R$ 12,00; total de R$ 112,00.
        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            // A lista comprova uma única cobrança, com o valor correto.
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void clienteBloqueadoRetornaSemCalcularNadaESemChamarPagamento() {
        // caminho PF01: primeiro retorno antecipado do método, antes de olhar os itens.
        Cliente cliente = new Cliente(false, true, 5);
        ItemPedido item = new ItemPedido("A", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("BLOQUEADO", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void pedidoComTodosOsItensInativosLancaExcecao() {
        // caminho PF02: subtotal fecha em zero (quantidade 0 no único item).
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("A", 10_000, 0, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        PedidoService service = new PedidoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente));
    }

    @Test
    void estoqueInsuficienteRetornaSemChamarPagamento() {
        // caminho PF03: quantidade pedida maior que o estoque do item.
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("A", 10_000, 10, 2, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("SEM_ESTOQUE", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void riscoEmRevisaoNaoChamaPagamentoMasCalculaOsValores() {
        // caminho PF04: primeira compra + entrega expressa força REVISAO na AnaliseRisco.
        Cliente cliente = new Cliente(false, false, 0);
        ItemPedido item = new ItemPedido("A", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", true, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("REVISAO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(2_700L, resultado.freteCentavos()),
            () -> assertEquals(12_700L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void pagamentoRecusadoRetornaStatusCorrespondente() {
        // caminho PF06: aprova a análise de risco, mas o processador recusa a cobrança.
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("A", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        PedidoService service = new PedidoService(total -> false);

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
            () -> assertEquals(11_200L, resultado.totalCentavos())
        );
    }
}
