package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        // Preparar: criar o objeto que será testado.
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(8);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarNotaAlunoComMediaQuatro() {
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(4);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaDois() {
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(2);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveCalcularMediaIgualCinco() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(5, 5);

        assertEquals(5, resultado);
    }

    // --- testes acrescentados ---

    @Test
    void deveCalcularMediaComPartesDecimais() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(7, 8);

        // notas de exemplo cuja média tem parte decimal (7,5)
        assertEquals(7.5, resultado, 0.0001);
    }

    @Test
    void deveAprovarComMediaExatamenteSete() {
        // limite inferior da faixa APROVADO
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(7);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarComMediaImediatamenteAbaixoDeSete() {
        // limite superior da faixa RECUPERACAO
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(6.99);

        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarComMediaImediatamenteAbaixoDeQuatro() {
        // limite superior da faixa REPROVADO
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(3.99);

        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveContarZeroAprovadosComArrayVazio() {
        // zero iterações do laço
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {});

        assertEquals(0, resultado);
    }

    @Test
    void deveContarUmAprovadoComArrayDeUmElemento() {
        // uma iteração, elemento aprovado
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {8});

        assertEquals(1, resultado);
    }

    @Test
    void deveContarZeroAprovadosComArrayDeUmElementoReprovado() {
        // uma iteração, elemento não aprovado
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {5});

        assertEquals(0, resultado);
    }

    @Test
    void deveContarAprovadoComMediaExatamenteSeteNoArray() {
        // reforça o limite (>= 7) dentro do laço
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {7});

        assertEquals(1, resultado);
    }

    @Test
    void deveContarVariosAprovadosEntreReprovados() {
        // várias iterações, mistura de aprovados e reprovados
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {8, 5, 7, 3, 9});

        assertEquals(3, resultado);
    }
}
