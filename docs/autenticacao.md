# Autenticação e migração de uma instalação existente

## O que mudou

- O login verifica o hash BCrypt da conta, sem senha universal de demonstração.
- As rotas da API exigem token válido, exceto `POST /api/auth/login`.
- A autorização consulta a conta no banco em cada requisição.
- A aplicação utiliza um único DataSource, configurado pelas variáveis de ambiente do README.
- O JSON de usuários não inclui `senha`, nem mesmo para administradores.
- A configuração não contém senha de banco nem chave JWT padrão.

## Instalação com banco já existente

Os livros, leitores e empréstimos não precisam ser apagados. O esquema utilizado inclui:

- `livros`: `id`, `titulo`, `autor`, `isbn`, `disponivel` e `data_cadastro`.
- `usuarios`: `id`, `nome`, `email`, `telefone`, `senha`, `role` e `data_cadastro`.
- `emprestimos`: os campos de referência, datas e status do `database/schema.sql`.

Se a base veio da versão desktop/refatorada e não possui os campos de autenticação, adicione **somente as colunas ausentes**, após conferir o esquema:

```sql
ALTER TABLE usuarios ADD COLUMN senha VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN role VARCHAR(20) DEFAULT 'USER';
```

Não execute esses comandos se as colunas já existirem. O projeto não aplica migrações automaticamente; `spring.sql.init.mode` fica em `never` na execução normal.

Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e uma nova `JWT_SECRET`. A chave antiga estava no código e deve ser substituída.

## Acesso administrativo inicial

Crie uma nova conta administrativa com o bootstrap descrito no README, escolhendo um e-mail não utilizado na base.

O bootstrap é desligado por padrão e recusa sobrescrever um cadastro existente. Depois da criação, desative `BOOTSTRAP_ADMIN_ENABLED` e remova `BOOTSTRAP_ADMIN_PASSWORD` do ambiente. Ele não é um mecanismo de redefinição de senha.

## Contas antigas e redefinição de senha

Senhas em texto simples, vazias ou nulas não são aceitas pelo login. Elas não são convertidas automaticamente nem aceitas como alternativa ao BCrypt.

Entre com o novo administrador, abra **Usuários**, edite a conta antiga e preencha uma nova senha de acesso. A senha será codificada antes de persistir. Deixar o campo vazio preserva a senha existente.

A edição via API preserva o perfil da conta. O cadastro de novos leitores sempre usa `USER`; o campo `role` enviado no JSON não permite promover uma conta.

Somente `ADMIN` e `USER` são reconhecidos. Um perfil nulo ou desconhecido é recusado na autenticação.

## Validade dos tokens

O padrão é uma hora, ajustável por `JWT_EXPIRATION` em milissegundos. Alterar `JWT_SECRET` invalida os tokens anteriores.

Excluir a conta impede novas requisições com seu token. Alterações de perfil são consideradas na requisição seguinte. A troca de senha não revoga tokens já emitidos: eles continuam sujeitos à validade configurada e às permissões atuais da conta.

O logout remove o token do navegador. Não há lista de revogação, refresh token ou recuperação de senha nesta versão.

## Origem das chamadas

A interface usa a mesma origem da aplicação e caminhos relativos `/api`. Não há liberação genérica de CORS. A API usa o cabeçalho Bearer e não cria uma sessão de login por cookie.

## Verificação independente do banco local

Os testes usam um esquema H2 separado em `src/test/resources/schema-test.sql`. A configuração em `src/test/resources/application.properties` não depende das variáveis nem da senha do MySQL.
