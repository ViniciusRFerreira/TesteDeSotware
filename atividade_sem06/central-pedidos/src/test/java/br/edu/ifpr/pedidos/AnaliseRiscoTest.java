package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {

    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void deveRejeitarTotalNegativo() {
        // caminho AR01
        assertThrows(IllegalArgumentException.class,
                () -> risco.avaliar(new Cliente(false, false, 0), -1, false));
    }

    @Test
    void clienteBloqueadoERecusadoAntesDeQualquerOutraChecagem() {
        // caminho AR02
        String resultado = risco.avaliar(new Cliente(false, true, 3), 1_000, false);
        assertEquals("RECUSADO", resultado);
    }

    @Test
    void primeiraCompraComTotalAltoVaiPraRevisao() {
        // caminho AR03: primeiro operando do || já é verdadeiro
        String resultado = risco.avaliar(new Cliente(false, false, 0), 100_001, false);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void primeiraCompraExpressaVaiPraRevisaoMesmoComTotalBaixo() {
        // caminho AR04: primeiro operando falso, segundo (expresso) verdadeiro
        String resultado = risco.avaliar(new Cliente(false, false, 0), 1_000, true);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void primeiraCompraNormalEAprovada() {
        // caminho AR05: os dois operandos do || são falsos
        String resultado = risco.avaliar(new Cliente(false, false, 0), 1_000, false);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void clienteComHistoricoETotalAltoNaoVipVaiPraRevisao() {
        // caminho AR06: os dois operandos do && são verdadeiros
        String resultado = risco.avaliar(new Cliente(false, false, 2), 500_001, false);
        assertEquals("REVISAO", resultado);
    }

    @Test
    void clienteVipComTotalAltoEAprovadoMesmoAcimaDoLimite() {
        // caminho AR07: curto-circuito do && corta no segundo operando (!vip é falso)
        String resultado = risco.avaliar(new Cliente(true, false, 2), 500_001, false);
        assertEquals("APROVADO", resultado);
    }

    @Test
    void clienteComHistoricoETotalBaixoEAprovado() {
        // caminho AR08: primeiro operando do && já é falso
        String resultado = risco.avaliar(new Cliente(false, false, 2), 1_000, false);
        assertEquals("APROVADO", resultado);
    }
}
