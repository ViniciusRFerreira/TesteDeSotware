package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {

    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveRejeitarSubtotalNegativo() {
        // caminho PD01: primeira decisão do método, subtotal < 0.
        assertThrows(IllegalArgumentException.class,
                () -> politica.calcular(new Cliente(false, false, 1), -1, null));
    }

    @Test
    void naoDaDescontoParaClienteComumComSubtotalBaixoSemCupom() {
        // caminho PD02: cliente comum, abaixo de R$ 500,00, sem cupom (cupom == null encerra o método).
        long resultado = politica.calcular(new Cliente(false, false, 1), 10_00, null);
        assertEquals(0, resultado);
    }

    @Test
    void daDezPorCentoParaClienteVip() {
        // caminho PD03: ramo vip do if/else-if, sem cupom.
        long resultado = politica.calcular(new Cliente(true, false, 1), 10_00, null);
        assertEquals(100, resultado);
    }

    @Test
    void daCincoPorCentoParaClienteComumComSubtotalAltoSemCupom() {
        // caminho PD04: else-if do subtotal >= R$ 500,00, sem cupom.
        long resultado = politica.calcular(new Cliente(false, false, 1), 60_000, null);
        assertEquals(3_000, resultado);
    }

    @Test
    void cupomEmBrancoEquivaleANaoInformarCupom() {
        // caminho PD05: cupom != null mas isBlank() == true, então também retorna direto.
        long resultado = politica.calcular(new Cliente(false, false, 1), 10_00, "   ");
        assertEquals(0, resultado);
    }

    @Test
    void cupomBemVindoSomaDoisMilCentavosParaPrimeiraCompraComSubtotalSuficiente() {
        // caminho PD06: entra no switch, cai em BEMVINDO, e as duas condições do if elegível são verdadeiras.
        // O cupom é passado com espaços e minúsculo para confirmar o trim()+toUpperCase().
        long resultado = politica.calcular(new Cliente(false, false, 0), 10_000, " bemvindo ");
        assertEquals(2_000, resultado);
    }

    @Test
    void cupomBemVindoNaoSomaSeClienteJaComprouAntes() {
        // caminho PD07: primeira condição do if (comprasAnteriores == 0) é falsa.
        long resultado = politica.calcular(new Cliente(false, false, 2), 10_000, "BEMVINDO");
        assertEquals(0, resultado);
    }

    @Test
    void cupomBemVindoNaoSomaSeSubtotalForInsuficiente() {
        // caminho PD08: primeira condição verdadeira, segunda (subtotal >= 10000) falsa.
        long resultado = politica.calcular(new Cliente(false, false, 0), 9_999, "BEMVINDO");
        assertEquals(0, resultado);
    }

    @Test
    void cupomExtraDezSomaDezPorCentoComSubtotalSuficiente() {
        // caminho PD09: switch cai em EXTRA10, condição verdadeira.
        long resultado = politica.calcular(new Cliente(false, false, 1), 20_000, "EXTRA10");
        assertEquals(2_000, resultado);
    }

    @Test
    void cupomExtraDezNaoSomaComSubtotalInsuficiente() {
        // caminho PD10: switch cai em EXTRA10, condição falsa.
        long resultado = politica.calcular(new Cliente(false, false, 1), 19_999, "EXTRA10");
        assertEquals(0, resultado);
    }

    @Test
    void cupomDesconhecidoLancaExcecao() {
        // caminho PD11: ramo default do switch.
        assertThrows(IllegalArgumentException.class,
                () -> politica.calcular(new Cliente(false, false, 1), 10_000, "FRETEGRATIS"));
    }

    @Test
    void descontoCombinadoRespeitaOTetoDeVintePorCento() {
        // caminho PD12: vip (10%) + BEMVINDO elegível (+2000) passa do teto de 20% e é cortado.
        // subtotal 10000: 10% = 1000, +2000 = 3000, mas o teto é 2000.
        long resultado = politica.calcular(new Cliente(true, false, 0), 10_000, "BEMVINDO");
        assertEquals(2_000, resultado);
    }
}
