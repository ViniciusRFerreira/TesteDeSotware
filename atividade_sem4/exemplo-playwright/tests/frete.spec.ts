import { test, expect } from '@playwright/test';

type Caso = {
  descricao: string;
  cep: string;
  valor: string;
  valido: boolean;
  frete?: number; // valor esperado do frete quando a entrada é válida (0 = grátis)
};

const casos: Caso[] = [
  // --- Entradas válidas: classes de equivalência do CEP e valores-limite do frete ---
  {
    descricao: 'CEP iniciado por 8 e valor abaixo de R$200 — frete R$15,00',
    cep: '80000000',
    valor: '100',
    valido: true,
    frete: 15,
  },
  {
    descricao: 'CEP não iniciado por 8 e valor abaixo de R$200 — frete R$25,00',
    cep: '01310100',
    valor: '100',
    valido: true,
    frete: 25,
  },
  {
    descricao: 'valor R$199,99 — limite imediatamente abaixo do frete grátis',
    cep: '01310100',
    valor: '199,99',
    valido: true,
    frete: 25,
  },
  {
    descricao: 'valor R$200,00 — limite mínimo do frete grátis',
    cep: '01310100',
    valor: '200',
    valido: true,
    frete: 0,
  },
  {
    descricao: 'valor bem acima de R$200 — frete grátis',
    cep: '80000000',
    valor: '500',
    valido: true,
    frete: 0,
  },
  {
    descricao: 'valor R$0,01 — limite mínimo aceito (maior que zero)',
    cep: '80000000',
    valor: '0,01',
    valido: true,
    frete: 15,
  },

  // --- Entradas inválidas: classes de equivalência inválidas ---
  {
    descricao: 'CEP com 7 dígitos — abaixo do tamanho exigido',
    cep: '8000000',
    valor: '100',
    valido: false,
  },
  {
    descricao: 'CEP com 9 dígitos — acima do tamanho exigido',
    cep: '800000000',
    valor: '100',
    valido: false,
  },
  {
    descricao: 'CEP vazio',
    cep: '',
    valor: '100',
    valido: false,
  },
  {
    descricao: 'CEP com letras',
    cep: 'abcdefgh',
    valor: '100',
    valido: false,
  },
  {
    descricao: 'valor vazio',
    cep: '80000000',
    valor: '',
    valido: false,
  },
  {
    descricao: 'valor igual a zero — não é maior que zero',
    cep: '80000000',
    valor: '0',
    valido: false,
  },
  {
    descricao: 'valor negativo',
    cep: '80000000',
    valor: '-50',
    valido: false,
  },
  {
    descricao: 'valor com três casas decimais',
    cep: '80000000',
    valor: '10,123',
    valido: false,
  },
  {
    descricao: 'valor não numérico',
    cep: '80000000',
    valor: 'abc',
    valido: false,
  },
];

test.describe('cálculo de frete', () => {
  for (const caso of casos) {
    test(caso.descricao, async ({ page }) => {
      await page.goto('/frete');
      await page.getByLabel('CEP').fill(caso.cep);
      await page.getByLabel('Valor do pedido').fill(caso.valor);
      await page.getByRole('button', { name: 'Calcular frete' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();

      if (!caso.valido) {
        await expect(resultado).toHaveText('Dados inválidos');
        await expect(resultado).toHaveAttribute('role', 'alert');
        return;
      }

      const textoEsperado =
        caso.frete === 0
          ? 'Frete grátis'
          : `Frete: R$ ${caso.frete!.toFixed(2).replace('.', ',')}`;

      await expect(resultado).toHaveText(textoEsperado);
      await expect(resultado).toHaveAttribute('role', 'status');
      await expect(resultado).toHaveClass('success');
    });
  }
});
