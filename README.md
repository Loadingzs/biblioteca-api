# 📚 Biblioteca Web - Sistema de Gerenciamento de Biblioteca

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

## 📋 Sobre o Projeto

Sistema completo de gerenciamento de biblioteca desenvolvido como **Projeto Integrador**. O sistema permite o controle de acervo, cadastro de usuários e gerenciamento de empréstimos de livros.

### ✨ Funcionalidades

| Módulo | Funcionalidades |
|--------|-----------------|
| **Livros** | Cadastro, edição, exclusão, busca por título/autor/ISBN |
| **Usuários** | Cadastro, edição, exclusão, busca por nome/email |
| **Empréstimos** | Registrar empréstimo, devolução, renovação, cálculo de multas |
| **Relatórios** | Estatísticas, livros disponíveis, empréstimos atrasados |

### 🎯 Regras de Negócio

- ✅ Máximo de **3 empréstimos ativos** por usuário
- ✅ Multa por atraso: **R$ 2,00 por dia**
- ✅ Validação de **ISBN duplicado** (ignora hífens)
- ✅ Validação de **email duplicado** e formato
- ✅ Controle automático de disponibilidade de livros

---

## 🛠️ Tecnologias Utilizadas

### Back-end
| Tecnologia | Versão |
|------------|--------|
| Java | 17+ |
| Spring Boot | 4.0.6 |
| Spring Web MVC | - |
| Spring Data JPA | - |
| Maven | - |

### Front-end
| Tecnologia | Descrição |
|------------|-----------|
| HTML5 | Estrutura das páginas |
| CSS3 | Estilização e responsividade |
| JavaScript | Interatividade e consumo da API |

### Banco de Dados
| Tecnologia | Descrição |
|------------|-----------|
| MySQL | 8.0+ |
| JDBC | Conexão e operações |

### Ferramentas
| Ferramenta | Uso |
|------------|-----|
| Git | Controle de versão |
| GitHub | Hospedagem do código |
| NetBeans | IDE de desenvolvimento |

---

## 📁 Estrutura do Projeto
biblioteca-api/
├── src/main/java/com/biblioteca/api/
│ ├── controller/ # REST Controllers (API endpoints)
│ ├── domain/ # Entidades (Livro, Usuario, Emprestimo)
│ ├── service/ # Regras de negócio
│ ├── repository/ # Acesso a dados (JDBC)
│ ├── infrastructure/ # Conexão com banco
│ └── config/ # Configurações (CORS)
├── src/main/resources/
│ ├── application.properties # Configurações do Spring
│ └── static/ # Front-end integrado
│ ├── index.html
│ ├── login.html
│ ├── livros.html
│ ├── usuarios.html
│ ├── emprestimos.html
│ ├── relatorios.html
│ ├── css/style.css
│ └── js/scripts.js
└── pom.xml # Dependências Maven

text

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

- [Java 17+](https://www.oracle.com/java/technologies/downloads/)
- [MySQL 8.0+](https://dev.mysql.com/downloads/)
- [NetBeans IDE](https://netbeans.apache.org/) (ou outra IDE)

### Passo 1: Clone o repositório

```bash
git clone https://github.com/Loadingzs/biblioteca-api.git
cd biblioteca-api
Passo 2: Configure o banco de dados
No MySQL, crie o banco e as tabelas:

sql
CREATE DATABASE sistema_biblioteca;
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
Passo 3: Configure a conexão
Edite src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_biblioteca
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI
Passo 4: Execute a aplicação
No NetBeans:

Clique com botão direito no projeto

Run ou Clean and Build + Run

No terminal:

bash
./mvnw spring-boot:run
Passo 5: Acesse o sistema
URL: http://localhost:8080

Login: admin@biblioteca.com

Senha: admin123

📊 API Endpoints
Método	Endpoint	Descrição
GET	/api/livros	Listar todos os livros
GET	/api/livros/{id}	Buscar livro por ID
POST	/api/livros	Cadastrar novo livro
PUT	/api/livros/{id}	Atualizar livro
DELETE	/api/livros/{id}	Excluir livro
GET	/api/usuarios	Listar todos os usuários
POST	/api/usuarios	Cadastrar novo usuário
GET	/api/emprestimos	Listar todos os empréstimos
POST	/api/emprestimos	Realizar empréstimo
PUT	/api/emprestimos/devolucao/{id}	Registrar devolução
🧪 Testes
O projeto inclui testes unitários JUnit:

bash
./mvnw test
Testes implementados:
✅ Cálculo de multa (sem atraso, com atraso de 3 e 10 dias)

✅ Validação de campos obrigatórios

✅ Validação de ISBN duplicado

🐛 Bugtracking
Issues registradas no GitHub:

Issue	Descrição	Status
#1	Adicionar README	✅ Concluído
#2	Validar ISBN duplicado no back-end	✅ Concluído
#3	Campos obrigatórios front-end	✅ Concluído
#4	Paginação nas tabelas	⏳ Pendente
#5	Loading/spinner	⏳ Pendente
📈 Melhorias Futuras
Paginação nas tabelas

Autenticação com JWT

Dashboard com gráficos estatísticos

Relatórios em PDF

Deploy na nuvem (Render/Railway)

👨‍💻 Autor
Maykon (Loadingzs)

GitHub: @Loadingzs

Projeto original desktop: sistema-biblioteca

📝 Licença
Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

🙏 Agradecimentos
Professores e orientadores do curso SENAC

Comunidade Java e Spring Boot

Desenvolvido com ❤️ como parte do Projeto Integrador