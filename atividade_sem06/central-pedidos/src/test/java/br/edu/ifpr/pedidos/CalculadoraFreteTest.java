package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {

    private final CalculadoraFrete calculadora = new CalculadoraFrete();
    private final Cliente comum = new Cliente(false, false, 1);
    private final Cliente vip = new Cliente(true, false, 1);

    private static Pedido pedido(String uf, boolean expresso, int pesoGramas, boolean fragil) {
        ItemPedido item = new ItemPedido("SKU1", 1_000, 1, 5, pesoGramas, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveRejeitarLiquidoNegativo() {
        // caminho CF01
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(pedido("PR", false, 1_000, false), comum, -1));
    }

    @Test
    void freteBaseDoParanaSemExcedenteDePeso() {
        // caminho CF02, também usado como baseline pros demais casos
        long resultado = calculadora.calcular(pedido("PR", false, 1_000, false), comum, 1_000);
        assertEquals(1_200, resultado);
    }

    @Test
    void freteBaseDeSpOuRj() {
        // caminho CF03: o switch trata SP e RJ como o mesmo bloco
        long resultado = calculadora.calcular(pedido("RJ", false, 1_000, false), comum, 1_000);
        assertEquals(2_000, resultado);
    }

    @Test
    void freteBaseDeOutroEstado() {
        // caminho CF04: ramo default do switch
        long resultado = calculadora.calcular(pedido("MG", false, 1_000, false), comum, 1_000);
        assertEquals(3_000, resultado);
    }

    @Test
    void excedenteDePesoComUmaIteracaoDoLaco() {
        // caminho CF05: 2500g - 2000g = 500g de excedente, cabe numa única iteração
        long resultado = calculadora.calcular(pedido("PR", false, 2_500, false), comum, 1_000);
        assertEquals(1_500, resultado);
    }

    @Test
    void excedenteDePesoComVariasIteracoesDoLaco() {
        // caminho CF06: 4500g - 2000g = 2500g de excedente, o laço roda 3 vezes (300 x 3 = 900)
        long resultado = calculadora.calcular(pedido("PR", false, 4_500, false), comum, 1_000);
        assertEquals(2_100, resultado);
    }

    @Test
    void liquidoAltoSemExpressoZeraFreteBase() {
        // caminho CF07: as duas condições do && são verdadeiras
        long resultado = calculadora.calcular(pedido("PR", false, 1_000, false), comum, 30_000);
        assertEquals(0, resultado);
    }

    @Test
    void liquidoAltoComExpressoNaoZeraEAindaSomaAdicional() {
        // caminho CF08: curto-circuito do && corta no segundo operando (!expresso é falso)
        long resultado = calculadora.calcular(pedido("PR", true, 1_000, false), comum, 30_000);
        assertEquals(2_700, resultado);
    }

    @Test
    void clienteVipPagaMetadeDoFrete() {
        // caminho CF09
        long resultado = calculadora.calcular(pedido("PR", false, 1_000, false), vip, 1_000);
        assertEquals(600, resultado);
    }

    @Test
    void itemFragilSomaAdicionalFixo() {
        // caminho CF10
        long resultado = calculadora.calcular(pedido("PR", false, 1_000, true), comum, 1_000);
        assertEquals(1_700, resultado);
    }
}
