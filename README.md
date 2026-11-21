# 📚 Biblioteca API — Java e Spring Boot

Gerenciamento de biblioteca com **API REST, interface web e MySQL**. O projeto reúne livros, leitores e empréstimos, com autenticação JWT, senhas BCrypt e permissões verificadas no servidor.

[![Testes](https://github.com/Loadingzs/biblioteca-api/actions/workflows/tests.yml/badge.svg)](https://github.com/Loadingzs/biblioteca-api/actions/workflows/tests.yml)

## Funcionalidades

- Cadastro, consulta, edição e exclusão de livros e leitores.
- Empréstimos, renovação, devolução e controle de disponibilidade.
- Limite de três empréstimos ativos/atrasados por leitor.
- Multa de R$ 2,00 por dia de atraso na devolução.
- Login por e-mail e senha armazenada com BCrypt.
- Validação da assinatura e expiração dos tokens JWT.
- Perfis de administração e leitura, conferidos com os dados atuais do banco.
- Páginas de dashboard, catálogo, usuários, empréstimos e relatórios.

## Tecnologias

| Área | Ferramentas |
| --- | --- |
| Back-end | Java 17, Spring Boot 4.0.6, Spring Web, Spring Security |
| Persistência | Spring JDBC, DataSource compartilhado e MySQL |
| Autenticação | BCrypt e JJWT 0.12.3 |
| Interface | HTML, CSS e JavaScript |
| Testes | JUnit 5, Mockito, H2 e testes de JavaScript com Node.js |
| Build e CI | Maven Wrapper e GitHub Actions |

## Começar

### 1. Clonar e preparar o banco

Tenha JDK 17 ou superior, Git e MySQL instalados.

```bash
git clone https://github.com/Loadingzs/biblioteca-api.git
cd biblioteca-api
mysql -u root -p
```

No cliente MySQL, para uma **base nova**:

```sql
SOURCE database/schema.sql;
```

O script cria `sistema_biblioteca` e as tabelas. Para uma base existente, veja [a orientação de migração](docs/autenticacao.md); não reaplique a criação das tabelas.

### 2. Configurar o ambiente

A conexão é definida somente por `application.properties`, que lê as variáveis abaixo. Os três repositórios recebem o mesmo `DataSource` do Spring.

| Variável | Uso | Padrão |
| --- | --- | --- |
| `DB_URL` | URL JDBC do MySQL | `jdbc:mysql://localhost:3306/sistema_biblioteca` |
| `DB_USERNAME` | Usuário do banco | `root` |
| `DB_PASSWORD` | Senha do banco | Obrigatória |
| `JWT_SECRET` | Chave própria de pelo menos 32 bytes | Obrigatória |
| `JWT_EXPIRATION` | Validade do token em milissegundos | `3600000` (uma hora) |
| `PORT` | Porta HTTP | `8080` |

Exemplo no PowerShell, substituindo a senha pelo seu valor local:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "SUA_SENHA_LOCAL"

$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$rng.Dispose()
```

A chave gerada vale para esse processo de desenvolvimento. Para manter tokens válidos entre reinicializações, preserve a mesma chave em um gerenciador de segredos ou configuração privada.

No Linux/macOS, use `export NOME="valor"`. O `.env.example` serve de referência; arquivos `.env` não são carregados automaticamente.

### 3. Criar o primeiro administrador

Na primeira execução, habilite explicitamente o bootstrap e escolha um **e-mail ainda não cadastrado**:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = "true"
$env:BOOTSTRAP_ADMIN_EMAIL = "seu-email@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "SUBSTITUA_POR_UMA_SENHA_PROPRIA"
.\mvnw.cmd spring-boot:run
```

A senha inicial precisa ter pelo menos 12 caracteres e no máximo 72 bytes. O bootstrap cria a conta com BCrypt; não altera nem promove contas existentes.

Após a criação, interrompa com `Ctrl+C`, desabilite o bootstrap e remova a senha inicial da sessão:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = "false"
Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, execute com `sh ./mvnw spring-boot:run` e use `unset BOOTSTRAP_ADMIN_PASSWORD`.

Abra <http://localhost:8080/login.html> e entre com a conta criada.

## Permissões

| Recurso | ADMIN | USER |
| --- | --- | --- |
| Consultar catálogo de livros | Sim | Sim |
| Criar, editar e excluir livros | Sim | Não |
| Consultar e gerenciar leitores | Sim | Não |
| Consultar e gerenciar empréstimos | Sim | Não |

As páginas enviam `Authorization: Bearer <token>` nas chamadas da API. Respostas `401` encerram a sessão na interface e pedem novo login. Os dados de perfil salvos no navegador são apenas uma indicação visual: a autorização efetiva ocorre no servidor.

Na tela **Usuários**, a senha é opcional. Sem senha, o cadastro funciona como leitor para empréstimos, sem acesso ao sistema. Com senha de 8 caracteres a 72 bytes, a conta pode consultar o catálogo.

A API cria contas `USER`, ignora tentativas de cadastrar `ADMIN` e preserva o perfil nas edições. Senha vazia/omitida na edição mantém a senha anterior; uma nova senha válida substitui o hash.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/api/auth/login` | Login público |
| GET / POST | `/api/livros` | Listar / cadastrar livros |
| GET / PUT / DELETE | `/api/livros/{id}` | Buscar / atualizar / excluir livro |
| GET / POST | `/api/usuarios` | Listar / cadastrar leitores |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Buscar / atualizar / excluir leitor |
| GET / POST | `/api/emprestimos` | Listar / realizar empréstimo |
| GET | `/api/emprestimos/ativos` | Listar ativos e atrasados |
| PUT | `/api/emprestimos/devolucao/{id}` | Registrar devolução |
| PUT | `/api/emprestimos/renovar/{id}` | Renovar empréstimo |

Login:

```json
{"email": "seu-email@example.com", "senha": "sua-senha"}
```

Empréstimo, com IDs existentes e token de administrador:

```json
{"livroId": 1, "usuarioId": 1, "dias": 7}
```

Para renovar, envie `{"dias": 7}`. As operações de negócio mantêm o retorno `true`/`false`; a autenticação usa `400` para entrada inválida, `401` para credenciais/token inválidos e `403` para falta de permissão.

## Testar

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
node --test src/test/js/auth.test.cjs
```

No Linux/macOS, substitua `.\mvnw.cmd` por `sh ./mvnw`. Os testes Java usam **H2 em memória**, sem credenciais ou conexão com o MySQL local.

A suíte cobre login, assinatura/expiração de JWT, permissões, proteção dos hashes, criação/edição de credenciais, bootstrap e persistência pelos três repositórios. Os testes JavaScript verificam o envio do token, tratamento de sessão e scripts das páginas.

O workflow em [Actions](https://github.com/Loadingzs/biblioteca-api/actions) executa essas verificações nos pushes e pull requests, sem segredos externos.

## Estrutura

```text
database/schema.sql              # Esquema inicial MySQL
src/main/java/com/biblioteca/api/
├── config/                      # Segurança, JWT e bootstrap
├── controller/                  # Endpoints REST
├── domain/                      # Modelos; senha não é serializada
├── dto/                         # Login
├── repository/                  # JDBC com DataSource injetado
└── service/                     # Regras e codificação de senhas
src/main/resources/
├── application.properties       # Configuração por ambiente
└── static/                      # Interface web
src/test/                        # Testes Java, JavaScript e esquema H2
docs/autenticacao.md              # Migração e comportamento das credenciais
```

## Evolução

**[Desktop](https://github.com/Loadingzs/sistema-biblioteca) → [núcleo refatorado](https://github.com/Loadingzs/sistema-biblioteca-web) → API web.**

Próximos passos: transações nas operações de empréstimo, padronização dos erros de negócio, documentação OpenAPI e recuperação de senha. Para hospedagem, configure HTTPS, segredos próprios e limites de tentativas de login.

## Licença

O README original declara licença MIT; o arquivo `LICENSE` ainda não está incluído no repositório.

## Autor

**Maykon Dias** · [GitHub](https://github.com/Loadingzs) · [LinkedIn](https://www.linkedin.com/in/maykondias/)
