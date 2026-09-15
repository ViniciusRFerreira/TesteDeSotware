# Atividade 02 — SEMANA 01 — LoginLab

# 1\. Plano de teste — LoginLab

## Identificação

* Produto/build: LoginLab · build 1.0.0
* Equipe: Vinicius Ramon Ferreira 
* Data: 13/08/2026

## Objetivo e decisão apoiada

Verificar se a função de autenticação do LoginLab cumpre as regras RN-LOGIN-01 a
RN-LOGIN-09, para decidir se o build 1.0.0 pode ser liberado ou se há defeitos que
bloqueiam a liberação — em especial no tratamento de credenciais inválidas e no
conteúdo das mensagens exibidas.

## Escopo

* Autenticação via formulário (`#login-form`): campos usuário e senha.
* As nove regras de negócio RN-LOGIN-01 a RN-LOGIN-09.
* Comportamento do botão "Limpar sessão".
* Conteúdo da mensagem exibida e do estado "Sessão criada".

## Fora de escopo

* Persistência real de sessão (o app usa apenas estado em memória/DOM).
* Cadastro ou alteração de contas (a massa de dados é fixa em `logic.js`).
* Aspectos visuais/CSS e responsividade.
* Segurança de transporte (não há rede: tudo roda em `file://`/local).

## Riscos priorizados

|ID|Risco|Probabilidade|Impacto|Prioridade|
|-|-|-|-|-|
|R1|Mensagem de erro revelar se um usuário existe (enumeração de contas)|Alta (já observado no código)|Alto|Alta|
|R2|Conta bloqueada autenticar indevidamente|Baixa|Alto|Alta|
|R3|Comparação de senha não diferenciar maiúsculas/minúsculas|Baixa|Médio|Média|
|R4|Falha de autenticação criar sessão mesmo assim|Baixa|Alto|Alta|

## Estratégia

* Nível: teste funcional de caixa-preta sobre o microprojeto (via UI).
* Tipo: teste de sistema/aceitação, dirigido a requisitos.
* Técnica: particionamento em classes de equivalência + análise de valor-limite qualitativo (campos obrigatórios vazios) + priorização por risco.
* Ordem de execução: casos de caminho feliz primeiro, depois classes inválidas em ordem de risco (bloqueado → inexistente → senha incorreta/caso → campos vazios), por fim o botão "Limpar sessão".

## Ambiente e massa

* `index.html` aberto diretamente no navegador (funciona via `file://`, sem servidor).
* Massa de dados fixa em `logic.js`: `aluno01/Teste@123` (ativo), `professor01/Aula@2026` (ativo), `bloqueado01/Segura@456` (bloqueado).
* Usuário inexistente usado nos testes: `usuario99` (qualquer valor fora do `Map` de contas serve).

## Critérios

* Entrada: build 1.0.0 disponível e ambiente configurado.
* Saída: todos os casos de teste executados e registrados.
* Suspensão: defeito bloqueador (ex.: sessão criada em autenticação inválida) interrompe a execução até correção.
* Retomada: após correção do defeito, reexecutar o caso que falhou e os casos relacionados ao mesmo requisito.

\---

# 2\. Classes de equivalência — LoginLab

## Campo usuário

|ID|Classe|Válida para autenticar?|Representante|Requisito|
|-|-|-|-|-|
|CE-U01|Usuário cadastrado e ativo|Sim (depende da senha)|`aluno01`|RN-LOGIN-02|
|CE-U02|Usuário cadastrado e bloqueado|Não|`bloqueado01`|RN-LOGIN-05|
|CE-U03|Usuário inexistente|Não|`usuario99`|RN-LOGIN-04|
|CE-U04|Usuário vazio|Não|`""`|RN-LOGIN-01|

## Campo senha

|ID|Classe|Válida para autenticar?|Representante|Requisito|
|-|-|-|-|-|
|CE-S01|Senha correta para o usuário informado|Sim (depende do usuário)|`Teste@123` (para `aluno01`)|RN-LOGIN-02|
|CE-S02|Senha incorreta, formato válido|Não|`SenhaErrada@1`|RN-LOGIN-03|
|CE-S03|Senha com diferença de maiúsculas/minúsculas|Não|`teste@123`|RN-LOGIN-08|
|CE-S04|Senha vazia|Não|`""`|RN-LOGIN-01|

> "Senha correta" não é uma propriedade isolada: `Teste@123` só é classe válida quando combinada com `aluno01`; para `professor01` a mesma string cai em CE-S02.

## Combinações selecionadas

|ID|Classe de usuário|Classe de senha|Resultado esperado|Justificativa|
|-|-|-|-|-|
|COMB-01|CE-U01 (ativo)|CE-S01 (correta)|`true`|Caminho feliz — cobre RN-LOGIN-02.|
|COMB-02|CE-U01 (ativo)|CE-S02 (incorreta)|`false`|Cobre RN-LOGIN-03.|
|COMB-03|CE-U01 (ativo)|CE-S03 (case diferente)|`false`|Cobre RN-LOGIN-08, isolado da classe "incorreta" genérica.|
|COMB-04|CE-U03 (inexistente)|CE-S02 (não vazia)|`false`|Cobre RN-LOGIN-04 **e** expõe se a mensagem é genérica (RN-LOGIN-06) — maior risco de segurança.|
|COMB-05|CE-U02 (bloqueado)|CE-S01 (correta p/ a conta)|`false`|Cobre RN-LOGIN-05: bloqueio deve prevalecer mesmo com senha certa.|
|COMB-06|CE-U04 (vazio)|CE-S02 (não vazia)|`false`|Cobre RN-LOGIN-01 pelo lado do usuário.|
|COMB-07|CE-U01 (ativo)|CE-S04 (vazia)|`false`|Cobre RN-LOGIN-01 pelo lado da senha.|
|COMB-08|CE-U02 (bloqueado)|CE-S02 (incorreta)|`false`|Cobre RN-LOGIN-09 (prioridade de regra: bloqueio deve ser checado antes da senha).|

## Combinações não selecionadas

* `professor01` + `Aula@2026`: mesma classe de `aluno01` + `Teste@123` (CE-U01 × CE-S01); redundante, omitido.
* Usuário inexistente + senha vazia: como o código valida `!usuario.trim() || !senha` antes de buscar a conta, esse caso sempre cai na mensagem "obrigatórios", não testando de fato a classe CE-U03; redundante com COMB-06/07.
* Usuário vazio + senha vazia: mesmo motivo acima, redundante com COMB-06/07.

\---

# 3\. Casos de teste — LoginLab

## CT-LOGIN-001 — Login válido com usuário ativo

* Requisitos: RN-LOGIN-02, RN-LOGIN-09
* Riscos: R4
* Prioridade: Alta
* Pré-condições: sessão limpa (clicar "Limpar sessão" antes)
* Usuário: `aluno01`
* Senha: `Teste@123`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e senha.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Bem-vindo, aluno01.`
* Sessão criada: sim

### Execução

* Resultado observado: `Bem-vindo, aluno01.` / sessão = sim
* Status: **Aprovado**
* Evidência: saída da função `autenticar('aluno01','Teste@123')` = `{autenticado:true, mensagem:'Bem-vindo, aluno01.', sessao:true}`

\---

## CT-LOGIN-002 — Senha incorreta para usuário ativo

* Requisitos: RN-LOGIN-03, RN-LOGIN-06, RN-LOGIN-09
* Riscos: R4
* Prioridade: Alta
* Pré-condições: sessão limpa
* Usuário: `aluno01`
* Senha: `SenhaErrada@1`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e senha.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Usuário ou senha inválidos`
* Sessão criada: não

### Execução

* Resultado observado: `Usuário ou senha inválidos.` / sessão = não
* Status: **Aprovado**
* Evidência: `{autenticado:false, mensagem:'Usuário ou senha inválidos.', sessao:false}`

\---

## CT-LOGIN-003 — Senha com diferença de maiúsculas/minúsculas

* Requisitos: RN-LOGIN-08, RN-LOGIN-06
* Riscos: R3
* Prioridade: Média
* Pré-condições: sessão limpa
* Usuário: `aluno01`
* Senha: `teste@123`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e senha (senha em minúsculas).
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Usuário ou senha inválidos`
* Sessão criada: não

### Execução

* Resultado observado: `Usuário ou senha inválidos.` / sessão = não
* Status: **Aprovado** (confirma que a comparação é *case-sensitive*, como exige RN-LOGIN-08)
* Evidência: `{autenticado:false, mensagem:'Usuário ou senha inválidos.', sessao:false}`

\---

## CT-LOGIN-004 — Usuário inexistente

* Requisitos: RN-LOGIN-04, **RN-LOGIN-06**
* Riscos: **R1**
* Prioridade: Alta
* Pré-condições: sessão limpa
* Usuário: `usuario99`
* Senha: `QualquerCoisa@1`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e senha.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Usuário ou senha inválidos` (mensagem genérica, conforme RN-LOGIN-06)
* Sessão criada: não

### Execução

* Resultado observado: **`Usuário não cadastrado.`** / sessão = não
* Status: **Reprovado**
* Evidência: `{autenticado:false, mensagem:'Usuário não cadastrado.', sessao:false}` — mensagem revela que o usuário não está cadastrado, violando RN-LOGIN-06. Ver DEF-LOGIN-001.

\---

## CT-LOGIN-005 — Conta bloqueada com senha correta

* Requisitos: RN-LOGIN-05
* Riscos: R2
* Prioridade: Alta
* Pré-condições: sessão limpa
* Usuário: `bloqueado01`
* Senha: `Segura@456`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e senha (senha correta da conta bloqueada).
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Conta bloqueada. Procure o suporte`
* Sessão criada: não

### Execução

* Resultado observado: `Conta bloqueada. Procure o suporte.` / sessão = não
* Status: **Aprovado**
* Evidência: `{autenticado:false, mensagem:'Conta bloqueada. Procure o suporte.', sessao:false}`

\---

## CT-LOGIN-006 — Campo usuário vazio

* Requisitos: RN-LOGIN-01
* Riscos: R4
* Prioridade: Média
* Pré-condições: sessão limpa
* Usuário: *(vazio)*
* Senha: `Teste@123`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Deixar usuário vazio e preencher a senha.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Usuário e senha são obrigatórios`
* Sessão criada: não

### Execução

* Resultado observado: `Usuário e senha são obrigatórios.` / sessão = não
* Status: **Aprovado**
* Evidência: `{autenticado:false, mensagem:'Usuário e senha são obrigatórios.', sessao:false}`

\---

## CT-LOGIN-007 — Campo senha vazio

* Requisitos: RN-LOGIN-01
* Riscos: R4
* Prioridade: Média
* Pré-condições: sessão limpa
* Usuário: `aluno01`
* Senha: *(vazia)*

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário e deixar a senha vazia.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Usuário e senha são obrigatórios`
* Sessão criada: não

### Execução

* Resultado observado: `Usuário e senha são obrigatórios.` / sessão = não
* Status: **Aprovado**
* Evidência: `{autenticado:false, mensagem:'Usuário e senha são obrigatórios.', sessao:false}`

\---

## CT-LOGIN-008 — Conta bloqueada com senha incorreta (prioridade de regra)

* Requisitos: RN-LOGIN-05, RN-LOGIN-09
* Riscos: R2
* Prioridade: Média
* Pré-condições: sessão limpa
* Usuário: `bloqueado01`
* Senha: `SenhaErrada@1`

### Passos

1. Acessar `index.html`.
2. Selecionar "Limpar sessão".
3. Preencher usuário bloqueado e uma senha incorreta.
4. Selecionar "Entrar".

### Resultado esperado

* Mensagem: `Conta bloqueada. Procure o suporte` (o bloqueio deve prevalecer mesmo com senha errada, sem vazar se a senha estava certa)
* Sessão criada: não

### Execução

* Resultado observado: `Conta bloqueada. Procure o suporte.` / sessão = não
* Status: **Aprovado** — confirma que a checagem de bloqueio ocorre antes da checagem de senha.
* Evidência: `{autenticado:false, mensagem:'Conta bloqueada. Procure o suporte.', sessao:false}`

\---

## CT-LOGIN-009 — Botão "Limpar sessão" restaura o estado inicial

* Requisitos: RN-LOGIN-09 (estado da UI/sessão)
* Riscos: R4
* Prioridade: Baixa
* Pré-condições: uma tentativa (válida ou inválida) já executada, com mensagem e sessão exibidas
* Usuário: N/A
* Senha: N/A

### Passos

1. Executar qualquer tentativa de login (ex.: CT-LOGIN-001).
2. Selecionar "Limpar sessão".
3. Observar os campos e a área de resultado.

### Resultado esperado

* Campos usuário e senha voltam vazios.
* Mensagem volta para "Nenhuma tentativa executada."
* Sessão criada: não

### Execução

* Resultado observado: campos limpos, mensagem "Nenhuma tentativa executada.", sessão = não
* Status: **Aprovado**
* Evidência: comportamento direto de `ui.js` (`form.reset()` + textos fixos no handler do botão `#limpar`)

\---

# 4\. Registro de execução — LoginLab

* Build: 1.0.0
* Navegador: \[preencher com o navegador usado na execução manual]
* Sistema operacional: Windows 11
* Executor: Vinicius Ramon Ferreira
* Data: 13/08/2026

|Caso|Usuário|Classe de senha|Esperado|Observado|Sessão|Status|Evidência|
|-|-|-|-|-|-|-|-|
|CT-LOGIN-001|aluno01|CE-S01 correta|Bem-vindo, aluno01.|Bem-vindo, aluno01.|sim|Aprovado|print/log da execução|
|CT-LOGIN-002|aluno01|CE-S02 incorreta|Usuário ou senha inválidos|Usuário ou senha inválidos.|não|Aprovado|print/log da execução|
|CT-LOGIN-003|aluno01|CE-S03 case diferente|Usuário ou senha inválidos|Usuário ou senha inválidos.|não|Aprovado|print/log da execução|
|CT-LOGIN-004|usuario99|CE-S02 (usuário inexistente)|Usuário ou senha inválidos|**Usuário não cadastrado.**|não|**Reprovado**|print/log — ver DEF-LOGIN-001|
|CT-LOGIN-005|bloqueado01|CE-S01 correta p/ conta|Conta bloqueada. Procure o suporte|Conta bloqueada. Procure o suporte.|não|Aprovado|print/log da execução|
|CT-LOGIN-006|(vazio)|CE-S01|Usuário e senha são obrigatórios|Usuário e senha são obrigatórios.|não|Aprovado|print/log da execução|
|CT-LOGIN-007|aluno01|CE-S04 vazia|Usuário e senha são obrigatórios|Usuário e senha são obrigatórios.|não|Aprovado|print/log da execução|
|CT-LOGIN-008|bloqueado01|CE-S02 incorreta|Conta bloqueada. Procure o suporte|Conta bloqueada. Procure o suporte.|não|Aprovado|print/log da execução|
|CT-LOGIN-009|—|—|UI volta ao estado inicial|UI volta ao estado inicial|não|Aprovado|print/log da execução|

## Resumo

* Total: 9
* Aprovados: 8
* Reprovados: 1 (CT-LOGIN-004)
* Bloqueados: 0

## Decisão de liberação

**Não recomendado liberar** o build 1.0.0 sem correção do defeito DEF-LOGIN-001:
a mensagem "Usuário não cadastrado." viola RN-LOGIN-06 e permite enumerar quais
usuários existem no sistema (falha de segurança de baixo esforço para explorar).
Os demais oito casos passaram sem divergência.

\---

# 5\. Relatório de defeito

## DEF-LOGIN-001 — Mensagem de erro revela que o usuário não está cadastrado

* Caso relacionado: CT-LOGIN-004
* Requisito: RN-LOGIN-06 (mensagem genérica `Usuário ou senha inválidos` para qualquer credencial inválida)
* Build: 1.0.0
* Severidade: Média (não compromete dados, mas facilita ataques de enumeração de contas)
* Prioridade: Alta (fácil de explorar, fácil de corrigir)

## Pré-condições

Nenhuma — basta acessar o formulário de login.

## Passos para reproduzir

1. Acessar `index.html`.
2. Informar um usuário que não existe na base (ex.: `usuario99`).
3. Informar qualquer senha não vazia.
4. Selecionar "Entrar".

## Resultado esperado

Mensagem genérica: `Usuário ou senha inválidos` (igual à exibida quando o usuário
existe mas a senha está errada), sem indicar se o usuário está ou não cadastrado.

## Resultado observado

Mensagem específica: `Usuário não cadastrado.` — revela a um atacante que aquele
nome de usuário não existe na base, permitindo mapear contas válidas por
tentativa e erro.

## Evidência

Trecho de `logic.js` (o próprio código já sinaliza o defeito):

```js
// Defeito didático intencional: RN-LOGIN-06 exige mensagem genérica.
if (!conta) {
  return {
    autenticado: false,
    mensagem: "Usuário não cadastrado.",
    sessao: false,
  };
}
```

Saída real da função: `autenticar('usuario99','QualquerCoisa@1')` →
`{autenticado:false, mensagem:'Usuário não cadastrado.', sessao:false}`.

## Testes de confirmação e regressão

* **Confirmação:** após a correção (trocar a mensagem desse bloco pela mesma
string usada no bloco de senha incorreta), reexecutar CT-LOGIN-004 e confirmar
que a mensagem passa a ser `Usuário ou senha inválidos`.
* **Regressão:** reexecutar CT-LOGIN-001, CT-LOGIN-002 e CT-LOGIN-003, para
garantir que a alteração não afetou os fluxos de usuário existente (válido,
senha incorreta e diferença de maiúsculas/minúsculas).

\---

# 6\. Parte 3 — Interpretação

1. **Quantas classes de usuário foram identificadas?** Quatro (CE-U01 a CE-U04): ativo, bloqueado, inexistente, vazio.
2. **Quantas classes de senha foram identificadas?** Quatro (CE-S01 a CE-S04): correta para o usuário, incorreta com formato válido, com diferença de maiúsculas/minúsculas, vazia.
3. **Por que não basta criar um caso `true` e um caso `false`?** Porque existem várias causas distintas de `false` (usuário inexistente, usuário bloqueado, senha errada, senha com case diferente, campos vazios), cada uma ligada a um requisito diferente (RN-LOGIN-03 a RN-LOGIN-08). Um único caso `false` não garante que cada regra individual foi implementada corretamente nem revela diferenças de mensagem entre elas — foi justamente essa diferença de mensagem que expôs o defeito.
4. **Quais combinações diferentes produziram `false`?** Senha incorreta (CT-002), diferença de maiúsculas/minúsculas (CT-003), usuário inexistente (CT-004), conta bloqueada com senha certa (CT-005) e com senha errada (CT-008), usuário vazio (CT-006) e senha vazia (CT-007).
5. **A mensagem exibida revela se um usuário está cadastrado?** Sim, atualmente revela: para usuário inexistente a mensagem é `Usuário não cadastrado.`, diferente da mensagem genérica usada para senha incorreta (`Usuário ou senha inválidos.`). Essa é a divergência didática (DEF-LOGIN-001).
6. **Alguma divergência deve bloquear a liberação?** Sim — DEF-LOGIN-001 viola uma regra de negócio explícita (RN-LOGIN-06) e tem implicação de segurança (enumeração de usuários), por isso a recomendação é não liberar o build 1.0.0 sem correção.

## Entregáveis — checklist

* \[x] Plano de teste
* \[x] Tabela de classes de equivalência
* \[x] Matriz de combinações selecionadas
* \[x] 9 casos de teste (dentro da faixa de 8 a 10 pedida)
* \[x] Registro de execução
* \[x] Evidências das divergências
* \[x] Relatório de defeito (DEF-LOGIN-001)
* \[x] Conclusão de liberação

