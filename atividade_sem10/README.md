# Semana 10 — Prática de TDD com Cobertura Total

**Aluno:** Vinicius Ramon Ferreira  
**Disciplina:** Teste e Qualidade de Software  
**Tecnologias:** Java 17, Maven, JUnit 5, JaCoCo 0.8.13  

---

## Visão Geral

Esta entrega reúne os quatro projetos Maven propostos na Semana 10, desenvolvidos seguindo o ciclo **TDD (RED → GREEN → REFACTOR)** até a implementação completa de todos os requisitos e **100% de cobertura** (instruções, ramos/branches, linhas, complexidade ciclomática e métodos) medida com o plugin **JaCoCo**.

### Resumo de Execução e Cobertura (JaCoCo)

| Projeto | Classe(s) | Testes Executados | Instruções | Branches | Linhas | Complexidade | Métodos |
|---|---|---:|---:|---:|---:|---:|---:|
| `bubblesort-tdd` | `BubbleSort` | 8 | 100% (83/83) | 100% (12/12) | 100% (18/18) | 100% (9/9) | 100% (3/3) |
| `carrinho-compras-tdd` | `Carrinho`, `Produto` | 13 | 100% (81/81) | 100% (8/8) | 100% (23/23) | 100% (11/11) | 100% (7/7) |
| `conta-bancaria-tdd-maven` | `ContaBancaria` | 8 | 100% (36/36) | 100% (6/6) | 100% (10/10) | 100% (7/7) | 100% (4/4) |
| `estoque-tdd` | `Produto` | 10 | 100% (37/37) | 100% (6/6) | 100% (12/12) | 100% (8/8) | 100% (5/5) |
| **Total** | **5 classes** | **39** | **100% (237/237)** | **100% (32/32)** | **100% (63/63)** | **100% (35/35)** | **100% (19/19)** |

---

## Detalhamento por Projeto

### 1. `bubblesort-tdd`

- **Restrição respeitada:** Os três casos de teste originalmente fornecidos (`deveOrdenarVetorDesordenado`, `deveManterVetorJaOrdenado`, `deveOrdenarVetorComValoresRepetidos`) foram mantidos integralmente sem alteração.
- **Fase RED:** Ao executar `mvn test` inicialmente, 2 de 3 testes falharam (`deveOrdenarVetorDesordenado` e `deveOrdenarVetorComValoresRepetidos`), confirmando o estado inicial antes da implementação.
- **Fase GREEN & REFACTOR:** Implementado o algoritmo Bubble Sort em `BubbleSort.ordenar()` com verificação defensiva para vetor `null` ou com tamanho `<= 1` e parada antecipada (`boolean trocou`) quando não ocorrem trocas em uma passagem.
- **Testes adicionais para cobertura total:** vetor em ordem decrescente (`[5, 4, 3, 2, 1]`), números negativos (`[0, -3, 7, -1, 2]`), elemento único (`[42]`), vetor vazio (`[]`) e vetor nulo (`null`).

### 2. `carrinho-compras-tdd`

- **Desenvolvimento guiado pelos 6 requisitos de `REQUISITOS.md`:**
  1. **Requisito 1 (Carrinho vazio):** `carrinhoRecemCriadoDeveTerTotalZero` — total inicial `R$ 0,00`.
  2. **Requisito 2 (Adicionar um produto):** `deveAdicionarUmProdutoECalcularTotal` — Teclado (`R$ 100,00`) totaliza `R$ 100,00` (também valida `Produto.getNome()`).
  3. **Requisito 3 (Adicionar vários produtos):** `deveAdicionarVariosProdutosESomarPrecos` — Teclado (`R$ 100,00`) + Mouse (`R$ 50,00`) = `R$ 150,00`.
  4. **Requisito 4 (Remover produto):** `deveRemoverProdutoDoCarrinho` e `removerProdutoInexistenteNaoDeveAlterarTotal`.
  5. **Requisito 5 (Produto com preço inválido):** Adotou-se lançar `IllegalArgumentException` ao tentar adicionar produto com preço negativo (`-100,00`) ou referência `null`, garantindo que o estado do carrinho não seja corrompido (comportamento explícito e seguro, mantendo `Produto` inalterado). Também testado o limite `R$ 0,00` (brinde).
  6. **Requisito 6 (Desconto de 10% para compras >= R$ 500,00):** Testado o exemplo de `R$ 600,00` (`-> R$ 540,00`), os valores de fronteira pedidos no enunciado (`R$ 499,99`, `R$ 500,00`, `R$ 500,01`) e a remoção de item que faz o subtotal voltar abaixo de `R$ 500,00`.

### 3. `conta-bancaria-tdd-maven`

- **Fase RED:** Ao executar `mvn test` inicialmente, todos os 4 testes fornecidos falharam (`4/4 Failures`), pois `depositar()` e `sacar()` não alteravam `saldo`.
- **Fase GREEN & REFACTOR:** Implementados `depositar(double valor)` (aceita apenas `valor > 0`) e `sacar(double valor)` (aceita apenas `valor > 0 && valor <= saldo`), preservando o contrato definido pelo teste `naoDevePermitirSaqueMaiorQueSaldo` (operações inválidas são ignoradas sem alterar o saldo).
- **Testes adicionais para cobertura total:** saldo inicial zero, depósito zero/negativo, saque zero/negativo e saque exato do saldo total (`valor == saldo`).

### 4. `estoque-tdd`

- **Fase RED:** Ao executar `mvn test` inicialmente, 4 de 6 testes fornecidos falharam (`deveAdicionarProdutoAoEstoque`, `deveAcumularQuantidadeEmEstoque`, `deveRetirarProdutoDoEstoque`, `naoDeveRetirarQuantidadeMaiorQueEstoque`).
- **Fase GREEN & REFACTOR:** Implementados `adicionarEstoque(int quantidade)` (rejeita `quantidade <= 0`, conforme Requisito 6) e `retirarEstoque(int quantidade)` (exige `quantidade > 0 && quantidade <= this.quantidade`, conforme Requisitos 4 e 5).
- **Testes adicionais para cobertura total:** consulta ao nome (`getNome()`), tentativa de entrada com quantidade `0`, tentativa de retirada com quantidade `0` ou negativa e retirada total do estoque disponível.

---

## Como Executar

Em cada subprojeto, execute:

```bash
mvn clean test
```

O relatório HTML do JaCoCo é gerado automaticamente em `target/site/jacoco/index.html` e o resumo em CSV em `target/site/jacoco/jacoco.csv`.
