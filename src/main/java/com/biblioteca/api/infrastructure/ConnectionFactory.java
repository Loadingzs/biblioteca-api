package com.biblioteca.api.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.stereotype.Component;  // ← ADICIONE
import java.sql.*;

@Component  // ← ADICIONE ESTA LINHA
public class ConnectionFactory {
    
    // Configurações para o banco sistema_biblioteca
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_biblioteca";
    private static final String USER = "root"; // altere se necessário
    private static final String PASSWORD = "1234"; // altere se necessário
    
    // Método para obter conexão
    public static Connection getConnection() {
        try {
            // Registrar o driver (não é mais necessário em versões recentes do JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Criar e retornar a conexão
            return DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL não encontrado!");
            System.err.println("Adicione o arquivo mysql-connector-java.jar ao classpath");
            throw new RuntimeException("Driver não encontrado", e);
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco de dados: " + e.getMessage());
            System.err.println("Verifique se:");
            System.err.println("1. O MySQL está rodando");
            System.err.println("2. O banco 'sistema_biblioteca' existe");
            System.err.println("3. Usuário e senha estão corretos");
            throw new RuntimeException("Erro de conexão", e);
        }
    }
    
    // Método para testar a conexão
    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conexão com 'sistema_biblioteca' estabelecida com sucesso!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Falha na conexão: " + e.getMessage());
            return false;
        }
    }
    
    // Método para fechar conexão (opcional - try-with-resources já faz isso)
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Conexão fechada");
            } catch (SQLException e) {
                System.err.println("❌ Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}