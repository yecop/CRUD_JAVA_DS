package persistencia;
import modelo.Cliente;
import modelo.Empresa;
import modelo.PersonaNatural;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Implementacion JDBC del CRUD de clientes y de la conversion entre subtipos.
public class ClienteDAOImpl implements IClienteDAO {
    private Connection conexion;

    public ClienteDAOImpl() {
        this.conexion = ConexionDB.getConexion();
    }

    @Override
    public boolean insertar(Cliente cliente) {
        // PersonaNatural y Empresa comparten columnas, pero guardan distintos identificadores.
        String sql = "INSERT INTO clientes (tipo, telefono, correo, identificacion, nombre_razon) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Cliente es abstracto; el tipo concreto determina que dato identifica al cliente.
            if (cliente instanceof PersonaNatural) {
                ps.setString(1, "PERSONA");
                ps.setString(4, ((PersonaNatural) cliente).getCedula());
                ps.setString(5, ((PersonaNatural) cliente).getNombreCompleto());
            } else if (cliente instanceof Empresa) {
                ps.setString(1, "EMPRESA");
                ps.setString(4, ((Empresa) cliente).getNit());
                ps.setString(5, ((Empresa) cliente).getRazonSocial());
            }
            
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getCorreo());
            
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) cliente.setId(rs.getInt(1)); // Asignar el ID autogenerado al objeto
                }
                return true;
            }
        } catch (SQLException e) { System.err.println("Error al guardar cliente: " + e.getMessage()); }
        return false;
    }

    @Override
    public Cliente buscarPorIdentificacion(String identificacion) {
        // La identificacion permite encontrar al cliente durante una venta en el POS.
        String sql = "SELECT * FROM clientes WHERE identificacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("tipo");
                    // Se reconstruye la subclase correcta para conservar el comportamiento del modelo.
                    if (tipo.equals("PERSONA")) {
                        return new PersonaNatural(rs.getInt("id"), rs.getString("telefono"), 
                                rs.getString("correo"), rs.getString("identificacion"), rs.getString("nombre_razon"));
                    } else {
                        return new Empresa(rs.getInt("id"), rs.getString("telefono"), 
                                rs.getString("correo"), rs.getString("identificacion"), rs.getString("nombre_razon"));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    @Override
    public List<Cliente> listar() {
        // El tipo almacenado decide que subclase se agrega a la lista resultante.
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                if (rs.getString("tipo").equals("PERSONA")) {
                    lista.add(new PersonaNatural(rs.getInt("id"), rs.getString("telefono"), 
                            rs.getString("correo"), rs.getString("identificacion"), rs.getString("nombre_razon")));
                } else {
                    lista.add(new Empresa(rs.getInt("id"), rs.getString("telefono"), 
                            rs.getString("correo"), rs.getString("identificacion"), rs.getString("nombre_razon")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        // Ahora sí actualizamos la columna 'tipo' para evitar inconsistencias
        String sql = "UPDATE clientes SET tipo=?, telefono=?, correo=?, identificacion=?, nombre_razon=? WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (cliente instanceof PersonaNatural) {
                ps.setString(1, "PERSONA");
                ps.setString(4, ((PersonaNatural) cliente).getCedula());
                ps.setString(5, ((PersonaNatural) cliente).getNombreCompleto());
            } else {
                ps.setString(1, "EMPRESA");
                ps.setString(4, ((Empresa) cliente).getNit());
                ps.setString(5, ((Empresa) cliente).getRazonSocial());
            }
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getCorreo());
            ps.setInt(6, cliente.getId()); // El ID va al final para el WHERE
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false; 
        }
    }

    @Override
    public boolean eliminar(int id) {
        // Si existen ventas relacionadas, las restricciones de la base pueden impedir el borrado.
        String sql = "DELETE FROM clientes WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}