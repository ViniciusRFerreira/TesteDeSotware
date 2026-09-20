package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacaoTest {

    // As duas decisões são independentes: existem quatro combinações de
    // entrada (V/V, V/F, F/V, F/F). Os quatro testes abaixo percorrem cada
    // uma delas, não apenas os dois ramos de cada `if` isoladamente.

    @Test
    void deveDarZeroPontosQuandoNaoEntregouNemParticipou() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, false);

        assertEquals(0, resultado);
    }

    @Test
    void deveDarDoisPontosQuandoEntregouMasNaoParticipou() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, false);

        assertEquals(2, resultado);
    }

    @Test
    void deveDarUmPontoQuandoNaoEntregouMasParticipou() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, true);

        assertEquals(1, resultado);
    }

    @Test
    void deveDarTresPontosQuandoEntregouEParticipou() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, true);

        assertEquals(3, resultado);
    }
}
