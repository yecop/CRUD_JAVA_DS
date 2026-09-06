package persistencia;

import modelo.Ingrediente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDAOImpl implements IIngredienteDAO {
    private Connection conexion;

    public IngredienteDAOImpl() {
        this.conexion = ConexionDB.getConexion();
    }

    @Override
    public boolean insertar(Ingrediente ing) {
        String sql = "INSERT INTO ingredientes (nombre, stock, precio_extra) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, ing.getNombre());
            ps.setInt(2, ing.getStock());
            ps.setDouble(3, ing.getPrecioExtra());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar ingrediente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Ingrediente> listar() {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingredientes";
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Ingrediente(
                    rs.getInt("id"), rs.getString("nombre"), 
                    rs.getInt("stock"), rs.getDouble("precio_extra")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean actualizar(Ingrediente ing) {
        String sql = "UPDATE ingredientes SET nombre=?, stock=?, precio_extra=? WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, ing.getNombre());
            ps.setInt(2, ing.getStock());
            ps.setDouble(3, ing.getPrecioExtra());
            ps.setInt(4, ing.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM ingredientes WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public Ingrediente obtenerPorId(int id) {
        String sql = "SELECT * FROM ingredientes WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ingrediente(rs.getInt("id"), rs.getString("nombre"), 
                                           rs.getInt("stock"), rs.getDouble("precio_extra"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
}