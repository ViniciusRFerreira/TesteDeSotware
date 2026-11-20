# Semana 11 — Testes de Integração com Spring Boot e JUnit 5

**Aluno:** Vinicius Ramon Ferreira  
**Disciplina:** Teste e Qualidade de Software  
**Tecnologias:** Java 17 (executado com JDK 21), Maven, Spring Boot 3.3.13, Spring Data JPA, H2, JUnit 5 e MockMvc.

---

## Objetivo da Atividade

Implementar testes de integração reais (sem mocks para `ProdutoService` nem `ProdutoRepository`) validando a integração completa entre as camadas:

- **Exercício 1 (`ProdutoServiceIntegrationTest`):** `ProdutoService → ProdutoRepository → Hibernate/JPA → Banco H2 em memória`.
- **Exercício 2 (`ProdutoControllerIntegrationTest`):** `MockMvc (HTTP) → ProdutoController → ProdutoService → ProdutoRepository → Banco H2 em memória`.

---

## Casos de Teste Implementados

### Exercício 1 — `ProdutoServiceIntegrationTest`

| Caso | Método JUnit | Cenário / Entrada | Verificação Realizada | Status |
|---|---|---|---|---|
| **CT01** | `deveCadastrarERecuperarProduto` | `service.cadastrar("Teclado", 150.0)` | Confirma geração de ID (`assertNotNull`), consulta novamente no banco com `repository.findById(id)` e valida `nome = "Teclado"` e `preco = 150.0`. | Passou |
| **CT02** | `naoDevePersistirProdutoComNomeEmBranco` | `service.cadastrar("   ", 80.0)` | Confirma lançamento de `IllegalArgumentException("Nome obrigatório")` e verifica com `repository.count()` que nenhum registro foi inserido. | Passou |
| **CT03 (Desafio)** | `deveAtualizarPrecoPersistido` | Cadastra `"Mouse"` (`80.0`) e chama `service.atualizarPreco(id, 95.0)` | Executa `repository.flush()`, consulta novamente via `repository.findById(id)` e confirma persistência do preço `95.0`. | Passou |
| Extra 1 | `naoDevePersistirProdutoComPrecoInvalido` | `service.cadastrar("Monitor", 0.0)` | Confirma `IllegalArgumentException("Preço deve ser positivo")` e contagem inalterada no repositório. | Passou |
| Extra 2 | `deveLancarExcecaoAoBuscarProdutoInexistente` | `service.buscar(999999L)` | Confirma `IllegalArgumentException("Produto não encontrado")`. | Passou |
| Extra 3 | `naoDeveAtualizarComPrecoNaoPositivo` | Cadastra `"Headset"` (`200.0`) e tenta atualizar para `-10.0` | Confirma `IllegalArgumentException` e que o preço salvo no banco permanece `200.0`. | Passou |

### Exercício 2 — `ProdutoControllerIntegrationTest`

| Caso | Método JUnit | Requisição HTTP | Verificação Realizada | Status |
|---|---|---|---|---|
| **CT04** | `deveCadastrarProdutoPelaApi` | `POST /produtos` com `{"nome":"Mouse","preco":80.0}` | Verifica HTTP `200 OK`, `$.id` numérico, `$.nome == "Mouse"` e `$.preco == 80.0`. | Passou |
| **CT05** | `deveBuscarProdutoPelaApi` | `POST /produtos` com `{"nome":"Teclado","preco":150.0}`, extrai `id` via `ObjectMapper`/`JsonNode` e executa `GET /produtos/{id}` | Verifica HTTP `200 OK` e correspondência exata de `id`, `nome` e `preco` no JSON retornado pela consulta. | Passou |

---

## Evidência de Execução (`mvn clean test`)

```text
[INFO] Running br.edu.exemplo.ProdutoControllerIntegrationTest
INFO ... HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:produtos user=SA
INFO ... Started ProdutoControllerIntegrationTest in 3.71 seconds
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.exemplo.ProdutoControllerIntegrationTest

[INFO] Running br.edu.exemplo.ProdutoServiceIntegrationTest
INFO ... HikariPool-2 - Added connection conn10: url=jdbc:h2:mem:produtos user=SA
INFO ... Started ProdutoServiceIntegrationTest in 0.45 seconds
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.exemplo.ProdutoServiceIntegrationTest

[INFO] Results:
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Discussão e Isolamento dos Testes

### Exercício 1 — Persistência e Isolamento com `@Transactional`

1. **Por que `repository.findById()` fornece uma evidência diferente de verificar apenas o objeto retornado pelo serviço?**
   Verificar somente o objeto retornado por `service.cadastrar(...)` ou `service.atualizarPreco(...)` confirma apenas o estado do objeto Java em memória retornado pelo método. Ao realizar `repository.findById(id)` (e `repository.flush()` no caso da atualização), comprovamos que o mapeamento ORM (JPA/Hibernate), a geração da chave primária (`@GeneratedValue`) e os comandos SQL (`INSERT`/`UPDATE`/`SELECT`) realmente persistiram e recuperaram os dados corretamente no banco H2.

2. **O que `@Transactional` faz ao término de cada teste (isolamento)?**
   Em testes anotados com `@SpringBootTest` e `@Transactional`, o Spring TestContext Framework inicia automaticamente uma transação antes de cada método `@Test` e executa **rollback automático** ao final do método (independentemente de passar ou falhar). Isso garante **isolamento total** entre os testes: registros inseridos em CT01 ou CT03 não vazam para CT02 ou para testes seguintes, evitando dependência de ordem de execução e mantendo o banco H2 limpo a cada caso de teste.

### Exercício 2 — Fluxo HTTP com `MockMvc`

1. **Quais componentes são efetivamente exercitados?**
   Todo o pipeline real da aplicação Spring: `DispatcherServlet`, roteamento de URLs (`@RequestMapping`, `@PostMapping`, `@GetMapping`), desserialização e serialização JSON via Jackson (`ProdutoRequest` e `Produto`), `ProdutoController`, `ProdutoService`, `ProdutoRepository`, Hibernate/JPA e o banco de dados H2 em memória.

2. **Por que o `MockMvc` não precisa iniciar um servidor HTTP real?**
   O `MockMvc` simula a camada Servlet (`MockHttpServletRequest` e `MockHttpServletResponse`) diretamente em memória dentro do próprio processo da JVM, invocando o `DispatcherServlet` real do Spring MVC sem abrir portas TCP/HTTP (como Tomcat na porta 8080). Isso torna os testes mais rápidos, determinísticos e livres de conflito de porta, mantendo a fidelidade do fluxo MVC completo.

3. **Qual é a diferença entre esse teste e um teste unitário do controller com dependências simuladas?**
   Em um teste unitário (por exemplo, com `@WebMvcTest` + `@MockitoBean` ou instanciando o controller com mocks), o `ProdutoService` e o `ProdutoRepository` são substituídos por dublês; testa-se apenas o contrato HTTP/serialização do controller isoladamente. Já o teste de integração com `@SpringBootTest` + `@AutoConfigureMockMvc` exercita as implementações reais de todas as camadas até o banco H2, detectando erros de injeção de dependência, transações, mapeamento JPA, validações de negócio e persistência real que mocks mascarariam.

---

## Como Executar

```bash
mvn clean test
```
