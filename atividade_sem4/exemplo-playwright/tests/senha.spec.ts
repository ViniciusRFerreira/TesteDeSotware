import { test, expect } from '@playwright/test';

type Caso = {
  descricao: string;
  senha: string;
};

// Regra da página: 8 a 20 caracteres, ao menos 1 maiúscula, 1 minúscula,
// 1 número, sem espaços. Casos abaixo cobrem classes de equivalência
// inválidas e os valores-limite de tamanho (7/8 e 20/21).
const casosInvalidos: Caso[] = [
  { descricao: 'senha com 7 caracteres — abaixo do limite mínimo', senha: 'Abc12xy' },
  { descricao: 'senha com 21 caracteres — acima do limite máximo', senha: 'Abcdefghij12345678901' },
  { descricao: 'senha vazia', senha: '' },
  { descricao: 'senha sem letra maiúscula', senha: 'abc12345' },
  { descricao: 'senha sem letra minúscula', senha: 'ABC12345' },
  { descricao: 'senha sem número', senha: 'Abcdefgh' },
  { descricao: 'senha com espaço', senha: 'Abc 1234' },
];

test.describe('cadastro de senha — formato inválido', () => {
  for (const caso of casosInvalidos) {
    test(caso.descricao, async ({ page }) => {
      await page.goto('/senha');
      await page.getByLabel('Nova senha').fill(caso.senha);
      await page.getByLabel('Confirmar senha').fill(caso.senha);
      await page.getByRole('button', { name: 'Cadastrar senha' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText('Senha fora do padrão');
      await expect(resultado).toHaveAttribute('role', 'alert');
    });
  }
});

test.describe('cadastro de senha — formato válido, limites de tamanho', () => {
  test('senha com 8 caracteres — limite mínimo aceito', async ({ page }) => {
    const senha = 'Abc123xy'; // 8 caracteres
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha);
    await page.getByLabel('Confirmar senha').fill(senha);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('Senha cadastrada');
    await expect(resultado).toHaveAttribute('role', 'status');
    await expect(resultado).toHaveClass('success');
  });

  test('senha com 20 caracteres — limite máximo aceito', async ({ page }) => {
    const senha = 'Abcdefghij1234567890'; // 20 caracteres
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha);
    await page.getByLabel('Confirmar senha').fill(senha);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('Senha cadastrada');
    await expect(resultado).toHaveAttribute('role', 'status');
    await expect(resultado).toHaveClass('success');
  });
});

test.describe('cadastro de senha — confirmação', () => {
  test('confirmação diferente da senha', async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill('Abc123xy');
    await page.getByLabel('Confirmar senha').fill('Abc123yz');
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('As senhas não coincidem');
    await expect(resultado).toHaveAttribute('role', 'alert');
  });

  test('cadastro concluído com sucesso limpa o formulário', async ({ page }) => {
    const senha = 'Abc123xy';
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha);
    await page.getByLabel('Confirmar senha').fill(senha);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('Senha cadastrada');
    await expect(page.getByLabel('Nova senha')).toHaveValue('');
    await expect(page.getByLabel('Confirmar senha')).toHaveValue('');
  });
});
