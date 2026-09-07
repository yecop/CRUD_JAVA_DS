package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Punto unico de configuracion y acceso a la conexion MySQL de la aplicacion.
public class ConexionDB {
    private static Connection conexion;
    private static final String URL = "jdbc:mysql://localhost:3306/bd_comidas_rapidas";
    private static final String USER = "root"; // Cambia si tu usuario es distinto
    private static final String PASSWORD = "root"; // Cambia si tienes contraseña

    // Impide crear objetos de una clase que solo ofrece metodos estaticos.
    private ConexionDB() {}

    public static Connection getConexion() {
        // La conexion se crea bajo demanda y se reutiliza mientras la aplicacion esta activa.
        if (conexion == null) {
            try {
                // Esta línea carga el driver explícitamente
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Se crea una unica conexion compartida por los DAO de la aplicacion.
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