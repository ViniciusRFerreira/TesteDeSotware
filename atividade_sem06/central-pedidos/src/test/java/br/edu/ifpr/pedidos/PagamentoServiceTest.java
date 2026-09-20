package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    // Stub simples: cada chamada consome uma resposta da fila. Um Boolean vira o
    // retorno de autorizar(); a marca INDISPONIVEL vira um IllegalStateException.
    private static final Object INDISPONIVEL = new Object();

    private static class ProcessadorFila implements ProcessadorPagamento {
        private final Deque<Object> respostas;
        final List<Long> chamadas = new java.util.ArrayList<>();

        ProcessadorFila(Object... respostas) {
            this.respostas = new ArrayDeque<>(List.of(respostas));
        }

        @Override
        public boolean autorizar(long totalCentavos) {
            chamadas.add(totalCentavos);
            Object resposta = respostas.poll();
            if (resposta == INDISPONIVEL) throw new IllegalStateException("indisponível");
            return (Boolean) resposta;
        }
    }

    @Test
    void deveRejeitarTotalNaoPositivo() {
        // caminho PS01
        ProcessadorFila processador = new ProcessadorFila(true);
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(processador).pagar(0, 3));
        assertTrue(processador.chamadas.isEmpty());
    }

    @Test
    void deveRejeitarMenosDeUmaTentativa() {
        // caminho PS02
        ProcessadorFila processador = new ProcessadorFila(true);
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(processador).pagar(100, 0));
        assertTrue(processador.chamadas.isEmpty());
    }

    @Test
    void deveRejeitarMaisDeTresTentativas() {
        // caminho PS03
        ProcessadorFila processador = new ProcessadorFila(true);
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(processador).pagar(100, 4));
        assertTrue(processador.chamadas.isEmpty());
    }

    @Test
    void aprovaNaPrimeiraTentativa() {
        // caminho PS04: retorno normal, o laço do/while não repete
        ProcessadorFila processador = new ProcessadorFila(true);
        boolean resultado = new PagamentoService(processador).pagar(100, 3);
        assertTrue(resultado);
        assertEquals(List.of(100L), processador.chamadas);
    }

    @Test
    void indisponibilidadeTemporariaPermiteNovaTentativaQueAprova() {
        // caminho PS05: cai no catch na primeira chamada, tenta de novo e aprova na segunda
        ProcessadorFila processador = new ProcessadorFila(INDISPONIVEL, true);
        boolean resultado = new PagamentoService(processador).pagar(100, 3);
        assertTrue(resultado);
        assertEquals(List.of(100L, 100L), processador.chamadas);
    }

    @Test
    void recusaDefinitivaNaoRepete() {
        // caminho PS06: retorno normal (false), o laço não repete mesmo tendo tentativas sobrando
        ProcessadorFila processador = new ProcessadorFila(false);
        boolean resultado = new PagamentoService(processador).pagar(100, 3);
        assertFalse(resultado);
        assertEquals(List.of(100L), processador.chamadas);
    }

    @Test
    void esgotarTentativasPorIndisponibilidadeRepetidaRetornaFalso() {
        // caminho PS07: o laço do/while roda até o limite (3) e depois retorna false
        ProcessadorFila processador = new ProcessadorFila(INDISPONIVEL, INDISPONIVEL, INDISPONIVEL);
        boolean resultado = new PagamentoService(processador).pagar(100, 3);
        assertFalse(resultado);
        assertEquals(List.of(100L, 100L, 100L), processador.chamadas);
    }

    @Test
    void outraExcecaoNaoCapturadaPropagaSemRepetir() {
        // reforça o contrato: só IllegalStateException aciona retentativa; outras exceções sobem direto.
        ProcessadorPagamento processador = total -> { throw new RuntimeException("erro grave"); };
        assertThrows(RuntimeException.class, () -> new PagamentoService(processador).pagar(100, 3));
    }
}
