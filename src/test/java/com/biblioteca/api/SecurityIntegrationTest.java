package com.biblioteca.api;

import com.biblioteca.api.config.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtUtil jwt;
    @Value("${jwt.secret}") String secret;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    final String password = "MinhaSenhaDeTeste!";

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM emprestimos");
        jdbc.update("DELETE FROM livros");
        jdbc.update("DELETE FROM usuarios");
        insertUser("admin@example.com", passwords.encode(password), "ADMIN");
        insertUser("leitor@example.com", passwords.encode(password), "USER");
    }

    void insertUser(String email, String hash, String role) {
        jdbc.update("INSERT INTO usuarios(nome,email,telefone,senha,role) VALUES (?,?,?,?,?)",
                "Teste", email, "1900000000", hash, role);
    }

    HttpResponse<String> request(String method, String path, String token, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> login(String email, String pass) throws Exception {
        return request("POST", "/api/auth/login", null, Map.of("email", email, "senha", pass));
    }

    String adminToken() {
        return jwt.generateToken("admin@example.com", "ADMIN");
    }

    @Test
    void requiresAuthenticationButKeepsLoginPagePublic() throws Exception {
        for (String path : new String[]{"/api/livros", "/api/usuarios", "/api/emprestimos"}) {
            assertEquals(401, request("GET", path, null, null).statusCode(), path);
        }
        assertEquals(200, request("GET", "/login.html", null, null).statusCode());
    }

    @Test
    void loginChecksStoredBcryptAndTokenAuthenticatesFollowingRequest() throws Exception {
        var response = login("admin@example.com", password);
        assertEquals(200, response.statusCode());
        var token = json.readTree(response.body()).get("token").asText();
        assertEquals(200, request("GET", "/api/usuarios", token, null).statusCode());
        assertEquals(401, login("admin@example.com", "admin123").statusCode());
        assertEquals(401, login("inexistente@example.com", password).statusCode());
    }

    @Test
    void rejectsLegacyPlaintextAndAccountsWithoutPassword() throws Exception {
        insertUser("antigo@example.com", "admin123", "ADMIN");
        insertUser("sem-senha@example.com", null, "USER");
        assertEquals(401, login("antigo@example.com", "admin123").statusCode());
        assertEquals(401, login("sem-senha@example.com", password).statusCode());
    }

    @Test
    void invalidLoginInputReturnsBadRequest() throws Exception {
        assertEquals(400, login("", password).statusCode());
        assertEquals(400, login("admin@example.com", " ").statusCode());
        assertEquals(400, login("admin@example.com", "x".repeat(73)).statusCode());
        assertEquals(400, request("POST", "/api/auth/login", null, Map.of()).statusCode());
    }

    @Test
    void rejectsTamperedExpiredAndIncompleteTokens() throws Exception {
        String token = adminToken();
        String[] parts = token.split("\\.");
        String signature = (parts[2].charAt(0) == 'a' ? "b" : "a") + parts[2].substring(1);
        assertEquals(401, request("GET", "/api/livros",
                parts[0] + "." + parts[1] + "." + signature, null).statusCode());
        var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder().subject("admin@example.com")
                .expiration(new Date(System.currentTimeMillis() - 60_000)).signWith(key).compact();
        String noExpiration = Jwts.builder().subject("admin@example.com").signWith(key).compact();
        String noSubject = Jwts.builder().expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key).compact();
        for (String invalid : new String[]{expired, noExpiration, noSubject, "invalido"}) {
            assertEquals(401, request("GET", "/api/livros", invalid, null).statusCode());
        }
    }

    @Test
    void readerCanOnlyReadCatalogAndCannotPromoteSelf() throws Exception {
        String token = jwt.generateToken("leitor@example.com", "ADMIN");
        assertEquals(200, request("GET", "/api/livros", token, null).statusCode());
        assertEquals(403, request("GET", "/api/usuarios", token, null).statusCode());
        assertEquals(403, request("GET", "/api/emprestimos", token, null).statusCode());
        assertEquals(403, request("POST", "/api/livros", token, Map.of()).statusCode());
        assertEquals(403, request("PUT", "/api/usuarios/1", token,
                Map.of("role", "ADMIN")).statusCode());
    }

    @Test
    void permissionsReflectDatabaseAndDeletedAccountsLoseAccess() throws Exception {
        String token = adminToken();
        jdbc.update("UPDATE usuarios SET role='USER' WHERE email='admin@example.com'");
        assertEquals(403, request("GET", "/api/usuarios", token, null).statusCode());
        jdbc.update("DELETE FROM usuarios WHERE email='admin@example.com'");
        assertEquals(401, request("GET", "/api/livros", token, null).statusCode());
    }

    @Test
    void createsBcryptReaderWithoutAcceptingAdminRoleOrLeakingHash() throws Exception {
        var response = request("POST", "/api/usuarios", adminToken(), Map.of(
                "nome", "Novo leitor", "email", "novo@example.com", "telefone", "1900000000",
                "senha", password, "role", "ADMIN"));
        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
        var row = jdbc.queryForMap("SELECT senha,role FROM usuarios WHERE email='novo@example.com'");
        assertTrue(passwords.matches(password, (String) row.get("senha")));
        assertEquals("USER", row.get("role"));
        String users = request("GET", "/api/usuarios", adminToken(), null).body();
        assertFalse(users.contains("\"senha\""));
        assertFalse(users.contains("$2a$"));
        assertEquals(200, login("novo@example.com", password).statusCode());
    }

    @Test
    void updatesCredentialsWithoutChangingRoleAndRetainsPasswordWhenOmitted() throws Exception {
        int id = jdbc.queryForObject("SELECT id FROM usuarios WHERE email='admin@example.com'", Integer.class);
        String hash = jdbc.queryForObject("SELECT senha FROM usuarios WHERE id=?", String.class, id);
        Map<String, Object> update = Map.of("nome", "Administrador alterado", "email", "admin@example.com",
                "telefone", "1900000000", "role", "USER");
        assertEquals("true", request("PUT", "/api/usuarios/" + id, adminToken(), update).body());
        assertEquals(hash, jdbc.queryForObject("SELECT senha FROM usuarios WHERE id=?", String.class, id));
        assertEquals("ADMIN", jdbc.queryForObject("SELECT role FROM usuarios WHERE id=?", String.class, id));
        var withPassword = new java.util.HashMap<>(update);
        withPassword.put("senha", "NovaSenhaValida!");
        assertEquals("true", request("PUT", "/api/usuarios/" + id, adminToken(), withPassword).body());
        assertEquals(401, login("admin@example.com", password).statusCode());
        assertEquals(200, login("admin@example.com", "NovaSenhaValida!").statusCode());
    }

    @Test
    void supportsBorrowersWithoutLoginAndRefusesShortOrOverlongPasswords() throws Exception {
        var reader = new java.util.HashMap<String, Object>(Map.of(
                "nome", "Sem acesso", "email", "cadastro@example.com", "telefone", "1900000000"));
        assertEquals("true", request("POST", "/api/usuarios", adminToken(), reader).body());
        for (String invalid : new String[]{"curta", "x".repeat(73), " ".repeat(12)}) {
            reader.put("email", "invalido@example.com");
            reader.put("senha", invalid);
            assertEquals("false", request("POST", "/api/usuarios", adminToken(), reader).body());
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email='invalido@example.com'", Integer.class));
        }
    }

    @Test
    void sharedDatasourcePersistsBooksUsersAndLoans() throws Exception {
        assertEquals("true", request("POST", "/api/livros", adminToken(), Map.of(
                "titulo", "Livro de teste", "autor", "Autor", "isbn", "9780000000001",
                "disponivel", true)).body());
        int book = jdbc.queryForObject("SELECT id FROM livros WHERE isbn='9780000000001'", Integer.class);
        int reader = jdbc.queryForObject("SELECT id FROM usuarios WHERE email='leitor@example.com'", Integer.class);
        assertEquals("true", request("POST", "/api/emprestimos", adminToken(),
                Map.of("livroId", book, "usuarioId", reader, "dias", 7)).body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos", Integer.class));
        assertFalse(jdbc.queryForObject("SELECT disponivel FROM livros WHERE id=?", Boolean.class, book));
    }

    @Test
    void unavailableBookRemainsUnavailableAfterCreationAndCannotBeBorrowed() throws Exception {
        var response = request("POST", "/api/livros", adminToken(), Map.of(
                "titulo", "Livro em manutenção", "autor", "Autor", "isbn", "9780000000002",
                "disponivel", false));
        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
        int book = jdbc.queryForObject("SELECT id FROM livros WHERE isbn='9780000000002'", Integer.class);
        assertFalse(jdbc.queryForObject("SELECT disponivel FROM livros WHERE id=?", Boolean.class, book));

        var catalog = json.readTree(request("GET", "/api/livros/" + book, adminToken(), null).body());
        assertFalse(catalog.get("disponivel").asBoolean());
        int reader = jdbc.queryForObject("SELECT id FROM usuarios WHERE email='leitor@example.com'", Integer.class);
        var loan = request("POST", "/api/emprestimos", adminToken(),
                Map.of("livroId", book, "usuarioId", reader, "dias", 7));
        assertEquals(200, loan.statusCode());
        assertEquals("false", loan.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos", Integer.class));
    }

    @Test
    void bookWithoutAvailabilityKeepsDefaultAvailable() throws Exception {
        var response = request("POST", "/api/livros", adminToken(), Map.of(
                "titulo", "Livro sem status", "autor", "Autor", "isbn", "9780000000003"));
        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
        assertTrue(jdbc.queryForObject("SELECT disponivel FROM livros WHERE isbn='9780000000003'", Boolean.class));
    }
}
