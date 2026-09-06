package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static Connection conexion;
    private static final String URL = "jdbc:mysql://localhost:3306/bd_comidas_rapidas";
    private static final String USER = "root"; // Cambia si tu usuario es distinto
    private static final String PASSWORD = "root"; // Cambia si tienes contraseña

    private ConexionDB() {}

    public static Connection getConexion() {
        if (conexion == null) {
            try {
                // Esta línea carga el driver explícitamente
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión a MySQL exitosa.");
            } catch (ClassNotFoundException e) {
                System.err.println("Error: No se encontró el driver JDBC de MySQL.");
            } catch (SQLException e) {
                System.err.println("Error de conexión: " + e.getMessage());
            }
        }
        return conexion;
    }
}