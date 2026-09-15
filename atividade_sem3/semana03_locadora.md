# SEMANA 03 — Geração de Casos de Teste com IA (Locadora de veículos)

* Aluno: Vinicius Ramon Ferreira
* Data: 25/08/2026

\---

## Etapa 1 — Entendimento do problema

* **Descrição:** sistema calcula o valor final de uma locação de veículo a partir de dias, valor da diária, km percorridos, contratação de seguro e idade do motorista.
* **Regras de negócio:** R1 (diárias = dias × valor da diária), R2 (franquia de 100km/dia; excedente a R$0,50/km), R3 (seguro opcional a R$35,00/dia), R4 (desconto sobre as diárias: 0% até 6 dias, 5% de 7 a 14 dias, 10% acima de 14 dias), R5 (taxa de R$150,00 se idade < 21 anos).
* **Restrições/entradas inválidas (R6):** dias ≤ 0, valor da diária ≤ 0, idade ≤ 17, km negativo → devem lançar exceção.
* **Assinatura:** `double calcularValorLocacao(int dias, double valorDiaria, int quilometrosPercorridos, boolean possuiSeguro, int idadeMotorista)`.

\---

## Etapa 2 — Casos de teste (sem código)

|ID|Entrada (dias, diária, km, seguro, idade)|Resultado esperado|Justificativa|
|-|-|-|-|
|CT-01|10, 120, 1200, true, 30|R$ 1.590,00|Exemplo do próprio enunciado — combina desconto (5%), excedente de km, seguro e idade sem taxa. Serve como caso "guarda-chuva".|
|CT-02|3, 100, 250, false, 25|R$ 300,00|Caminho básico: só diárias, sem excedente, sem seguro, sem desconto, sem taxa de idade.|
|CT-03|5, 100, 500, false, 25|R$ 500,00|Limite **inferior** de excedente: km exatamente igual à franquia (5×100=500) não deve gerar cobrança extra.|
|CT-04|5, 100, 501, false, 25|R$ 500,50|Limite **superior**: 1km acima da franquia já gera R$0,50 de excedente.|
|CT-05|6, 100, 400, false, 25|R$ 600,00|Limite superior da faixa "sem desconto" (até 6 dias).|
|CT-06|7, 100, 500, false, 25|R$ 665,00|Limite inferior da faixa de desconto de 5% (7 a 14 dias).|
|CT-07|14, 100, 1000, false, 25|R$ 1.330,00|Limite superior da faixa de desconto de 5%.|
|CT-08|15, 100, 1200, false, 25|R$ 1.350,00|Limite inferior da faixa de desconto de 10% (acima de 14 dias).|
|CT-09|4, 100, 300, true, 25|R$ 540,00|Isola o efeito do seguro (R$35,00/dia) sem outras regras interferindo.|
|CT-10|5, 100, 400, false, 20|R$ 650,00|Limite superior da faixa "idade < 21" — ainda paga a taxa de R$150,00.|
|CT-11|5, 100, 400, false, 21|R$ 500,00|Limite inferior da faixa "idade ≥ 21" — não paga taxa.|
|CT-12|5, 100, 400, false, 18|R$ 650,00|Idade mínima válida (18), mas ainda dentro da faixa que paga taxa (18 < 21) — isola a regra R6 (validade) da regra R5 (taxa).|
|CT-13|0, 100, 100, false, 25|Exceção|R6: dias ≤ 0.|
|CT-14|-1, 100, 100, false, 25|Exceção|R6: dias negativo (reforça a classe "dias inválido" com outro representante).|
|CT-15|5, 0, 100, false, 25|Exceção|R6: valor da diária ≤ 0.|
|CT-16|5, 100, 100, false, 17|Exceção|R6: idade no limite inválido (17 anos).|
|CT-17|5, 100, -1, false, 25|Exceção|R6: quilometragem negativa.|

\---

## Etapa 3 — Revisão crítica dos casos

* **Erro identificado e corrigido durante a revisão:** na primeira versão eu havia descrito o CT-12 (idade 18) como "sem taxa adicional", por engano associando a idade mínima válida (R6: idade > 17) com a ausência de taxa. Re-lendo R5 ("idade **menor que 21 anos**"), 18 ainda está nessa faixa — então o resultado correto é **R$ 650,00**, não R$ 500,00. Caso corrigido na tabela acima e revalidado.
* **Cobertura por regra:** R1 é exercitada em todos os casos válidos; R2 tem os dois limites (CT-03/CT-04); R3 é isolado em CT-09; R4 tem os quatro limites das três faixas de desconto (CT-05 a CT-08); R5 tem os dois limites (CT-10/CT-11) mais o caso combinado com validade mínima (CT-12); R6 tem um representante para cada condição de invalidez (CT-13 a CT-17).
* **Casos considerados e descartados por redundância:** "dias = 1, sem nenhum outro fator" — mesma classe de CT-02 (caminho básico), não isola nada novo. "Idade muito acima de 21 (ex.: 40 anos)" — mesma classe de CT-11 (idade ≥ 21), não muda o resultado nem o caminho.
* **Ponto de atenção para a implementação:** a ordem de aplicação importa — o desconto de R4 incide **somente** sobre `valorDiarias`, antes de somar seguro, excedente e taxa de idade (R4 é explícito: "não incide sobre seguro nem quilometragem excedente"). Isso foi conferido caso a caso ao calcular CT-01 e CT-09.

\---

## Etapa 4 — Implementação em JUnit 5

### `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>br.edu.cesu</groupId>
    <artifactId>locadora-veiculos</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.11.0</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.0</version>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.13</version>
                <executions>
                    <execution>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### `src/main/java/br/edu/cesu/locadora/CalculadoraLocacao.java`

```java
package br.edu.cesu.locadora;

public class CalculadoraLocacao {

    public double calcularValorLocacao(
            int dias,
            double valorDiaria,
            int quilometrosPercorridos,
            boolean possuiSeguro,
            int idadeMotorista) {

        if (dias <= 0 || valorDiaria <= 0 || idadeMotorista <= 17 || quilometrosPercorridos < 0) {
            throw new IllegalArgumentException("Entrada inválida para cálculo de locação.");
        }

        double valorDiarias = dias \* valorDiaria;

        int franquia = dias \* 100;
        int excedente = quilometrosPercorridos > franquia ? quilometrosPercorridos - franquia : 0;
        double valorExcedente = excedente \* 0.5;

        double valorSeguro = possuiSeguro ? dias \* 35.0 : 0.0;

        double desconto;
        if (dias >= 7 \&\& dias <= 14) {
            desconto = valorDiarias \* 0.05;
        } else if (dias > 14) {
            desconto = valorDiarias \* 0.10;
        } else {
            desconto = 0.0;
        }

        double taxaIdade = idadeMotorista < 21 ? 150.0 : 0.0;

        double total = (valorDiarias - desconto) + valorSeguro + valorExcedente + taxaIdade;
        return Math.round(total \* 100.0) / 100.0;
    }
}
```

### `src/test/java/br/edu/cesu/locadora/CalculadoraLocacaoTest.java`

```java
package br.edu.cesu.locadora;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraLocacaoTest {

    private final CalculadoraLocacao calculadora = new CalculadoraLocacao();

    @ParameterizedTest(name = "\[{index}] dias={0} diaria={1} km={2} seguro={3} idade={4} -> {5}")
    @CsvSource({
        // dias, valorDiaria, km, possuiSeguro, idadeMotorista, esperado
        "10, 120, 1200, true,  30, 1590.00",  // CT-01 exemplo do enunciado
        "3,  100, 250,  false, 25, 300.00",   // CT-02 caminho básico
        "5,  100, 500,  false, 25, 500.00",   // CT-03 limite inferior excedente
        "5,  100, 501,  false, 25, 500.50",   // CT-04 limite superior excedente
        "6,  100, 400,  false, 25, 600.00",   // CT-05 limite sem desconto
        "7,  100, 500,  false, 25, 665.00",   // CT-06 limite inferior desconto 5%
        "14, 100, 1000, false, 25, 1330.00",  // CT-07 limite superior desconto 5%
        "15, 100, 1200, false, 25, 1350.00",  // CT-08 limite inferior desconto 10%
        "4,  100, 300,  true,  25, 540.00",   // CT-09 seguro isolado
        "5,  100, 400,  false, 20, 650.00",   // CT-10 limite com taxa de idade
        "5,  100, 400,  false, 21, 500.00",   // CT-11 limite sem taxa de idade
        "5,  100, 400,  false, 18, 650.00",   // CT-12 idade mínima válida, ainda com taxa
    })
    @DisplayName("calcula o valor da locação para entradas válidas")
    void calculaValorLocacaoParaEntradasValidas(
            int dias, double valorDiaria, int km, boolean possuiSeguro, int idade, double esperado) {
        double resultado = calculadora.calcularValorLocacao(dias, valorDiaria, km, possuiSeguro, idade);
        assertEquals(esperado, resultado, 0.0001);
    }

    @Test
    @DisplayName("CT-13: dias igual a zero lança exceção")
    void diasIgualAZeroLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularValorLocacao(0, 100, 100, false, 25));
    }

    @Test
    @DisplayName("CT-14: dias negativo lança exceção")
    void diasNegativoLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularValorLocacao(-1, 100, 100, false, 25));
    }

    @Test
    @DisplayName("CT-15: valor da diária igual a zero lança exceção")
    void valorDiariaZeroLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularValorLocacao(5, 0, 100, false, 25));
    }

    @Test
    @DisplayName("CT-16: idade no limite inválido (17) lança exceção")
    void idadeDezessete\_LancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularValorLocacao(5, 100, 100, false, 17));
    }

    @Test
    @DisplayName("CT-17: quilometragem negativa lança exceção")
    void quilometragemNegativaLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularValorLocacao(5, 100, -1, false, 25));
    }
}
```

\---

## Etapa 5 — Execução dos testes

Este sandbox não tem acesso ao Maven Central (só a alguns registries como npm/PyPI/crates), então não dá para rodar `mvn test` aqui. Antes de escrever o Java acima, validei **cada um dos 17 casos** com uma reimplementação idêntica da mesma lógica em JavaScript, executada de fato via Node — todos os 12 casos válidos bateram com o valor esperado e as 5 entradas inválidas lançaram exceção corretamente (incluindo o CT-12, onde encontrei e corrigi meu próprio erro de leitura da regra R5 antes de fechar a tabela).

No seu Windows 11, para rodar de verdade:

```powershell
cd caminho\\para\\locadora-veiculos
mvn clean test jacoco:report
```

Os 17 métodos de teste devem passar (12 parametrizados + 5 de exceção).

## Etapa 6 — Cobertura de código

Depois de `mvn clean test jacoco:report`, abra `target/site/jacoco/index.html`. Com os 17 casos acima, o método `calcularValorLocacao` deve fechar em:

* **Linhas/instruções:** 100% — todo o corpo do método é executado por algum caso.
* **Branches:** os quatro `if`/`else` (validação R6 combinada em um só `if` com `||`, faixa de excedente, as duas faixas de desconto, e a taxa de idade) têm os dois lados (verdadeiro/falso) exercitados pelos casos de limite (CT-03/04, CT-05/06/07/08, CT-10/11).
* Atenção ao `||` da validação R6: como é um único `if`, o JaCoCo mede a cobertura de branch da condição como um todo — os 5 casos de exceção (CT-13 a CT-17) cobrem cada uma das quatro subcondições isoladamente, mas confirme no relatório se o JaCoCo aponta os operandos do `||` como cobertos individualmente (depende de como a JVM faz o curto-circuito nesses casos).

## Etapa 7 — Refinamento

Se o relatório mostrar alguma linha ou branch descoberto (por exemplo, se algum curto-circuito do `||` não for exercitado por nenhum caso), os candidatos a adicionar seriam entradas que isolem cada subcondição sozinha nas duas ordens (ex.: `dias<=0` verdadeiro com as demais falsas, e vice-versa) — os casos CT-13 a CT-17 já foram desenhados exatamente para isso, então a expectativa é 100% sem necessidade de novos testes.

