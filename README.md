# 📚 Biblioteca API — Java e Spring Boot

Aplicação de gerenciamento de biblioteca com **API REST, páginas web e persistência MySQL**. Evolui o núcleo refatorado do projeto para expor livros, usuários e empréstimos por HTTP.

## Funcionalidades

- Cadastro, listagem, edição e exclusão de livros e usuários.
- Empréstimos, listagem de ativos, renovação e devolução.
- Controle de disponibilidade, limite de três empréstimos ativos/atrasados por usuário e multa de R$ 2,00 por dia de atraso.
- Endpoint de login com emissão de JWT.
- Páginas HTML com CSS e JavaScript servidas pela aplicação.

## Tecnologias

| Área | Implementação |
| --- | --- |
| Linguagem | Java 17, conforme `pom.xml` |
| Servidor | Spring Boot 4.0.6, Spring Web |
| Persistência | JDBC, MySQL Connector/J e SQL nos repositórios |
| Token | JJWT 0.12.3 |
| Interface | HTML, CSS e JavaScript |
| Build e testes | Maven Wrapper, JUnit 5 e Mockito |

O starter de JPA está no `pom.xml`, mas as operações de dados atuais usam JDBC. O esquema abaixo deve ser preparado manualmente.

## Executar localmente

### 1. Clonar

Tenha JDK 17 ou superior, Git e MySQL instalados.

```bash
git clone https://github.com/Loadingzs/biblioteca-api.git
cd biblioteca-api
```

### 2. Preparar uma base de desenvolvimento

Execute no MySQL:

```sql
CREATE DATABASE IF NOT EXISTS sistema_biblioteca CHARACTER SET utf8mb4;
USE sistema_biblioteca;

CREATE TABLE livros (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    isbn VARCHAR(50) UNIQUE,
    disponivel BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    senha VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE emprestimos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_livro INT NOT NULL,
    id_usuario INT NOT NULL,
    data_emprestimo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_devolucao_prevista DATE NOT NULL,
    data_devolucao_real DATE,
    status ENUM('ATIVO', 'FINALIZADO', 'ATRASADO') DEFAULT 'ATIVO',
    FOREIGN KEY (id_livro) REFERENCES livros(id),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);
```

As colunas `senha` e `role` são utilizadas pelo `UsuarioRepository` desta versão. Se já usa um banco das versões anteriores, confira essas diferenças antes de reutilizá-lo.

### 3. Configurar as duas conexões

A aplicação possui duas configurações que precisam apontar para o mesmo banco:

| Arquivo | Valores a ajustar |
| --- | --- |
| `src/main/resources/application.properties` | `spring.datasource.url`, `spring.datasource.username` e `spring.datasource.password` |
| `src/main/java/com/biblioteca/api/infrastructure/ConnectionFactory.java` | `URL`, `USER` e `PASSWORD` usados pelo JDBC |

Em `application.properties`, configure também `jwt.secret` com uma chave aleatória própria de pelo menos 32 bytes e `jwt.expiration` com a duração em milissegundos. Não publique suas credenciais locais.

### 4. Iniciar

No PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
sh ./mvnw spring-boot:run
```

Acesse <http://localhost:8080> ou a página <http://localhost:8080/login.html>.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/api/auth/login` | Emitir token de demonstração |
| GET / POST | `/api/livros` | Listar / cadastrar livros |
| GET / PUT / DELETE | `/api/livros/{id}` | Buscar / atualizar / excluir livro |
| GET / POST | `/api/usuarios` | Listar / cadastrar usuários |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Buscar / atualizar / excluir usuário |
| GET / POST | `/api/emprestimos` | Listar / realizar empréstimo |
| GET | `/api/emprestimos/ativos` | Listar ativos e atrasados |
| PUT | `/api/emprestimos/devolucao/{id}` | Registrar devolução |
| PUT | `/api/emprestimos/renovar/{id}` | Renovar empréstimo |

Exemplo de corpo JSON para `POST /api/emprestimos`, usando IDs existentes:

```json
{
  "livroId": 1,
  "usuarioId": 1,
  "dias": 7
}
```

Para renovar, envie `{"dias": 7}`. Muitas operações retornam `true` ou `false`; o tratamento de erros HTTP ainda pode ser padronizado.

## Login: comportamento atual

`POST /api/auth/login` recebe `email` e `senha`. O e-mail precisa existir em `usuarios`, e a implementação atual compara a senha com o valor demonstrativo `admin123`, em vez de validar a senha armazenada.

`JwtFilter` ainda apenas encaminha a requisição: a presença do JWT não significa que as rotas estejam protegidas. A autenticação é uma implementação parcial e deve evoluir antes de disponibilizar dados reais.

## Estrutura

```text
src/main/
├── java/com/biblioteca/api/
│   ├── config/          # JWT e filtro
│   ├── controller/      # Endpoints REST
│   ├── domain/          # Modelos
│   ├── dto/             # Requisição/resposta de login
│   ├── infrastructure/  # Conexão JDBC
│   ├── repository/      # SQL e persistência
│   └── service/         # Regras de negócio
└── resources/
    ├── application.properties
    └── static/          # Páginas, CSS e JavaScript

src/test/java/com/biblioteca/api/
```

## Testes

O teste de serviço existente verifica a recusa de prazo de empréstimo inválido sem acessar os repositórios:

```powershell
.\mvnw.cmd "-Dtest=EmprestimoServiceTest" test
```

Para a suíte completa, incluindo o carregamento do contexto Spring, prepare também a configuração do banco e execute:

```powershell
.\mvnw.cmd test
```

No Linux/macOS, substitua `.\mvnw.cmd` por `sh ./mvnw`.

## Histórico e próximos passos

A sequência do projeto é **[desktop](https://github.com/Loadingzs/sistema-biblioteca) → [núcleo refatorado](https://github.com/Loadingzs/sistema-biblioteca-web) → API web**.

Próximos passos: completar autenticação e autorização, unificar a configuração de conexão, ampliar testes, adicionar transações e documentar a API com OpenAPI.

## Licença

O README original declara licença MIT; o arquivo `LICENSE` ainda não está incluído no repositório.

## Autor

**Maykon Dias** · [GitHub](https://github.com/Loadingzs) · [LinkedIn](https://www.linkedin.com/in/maykondias/)
