package persistencia;

import modelo.Ingrediente;
import modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Implementacion JDBC para productos y la tabla intermedia de sus recetas.
public class ProductoDAOImpl implements IProductoDAO {
    private Connection conexion;
    private IIngredienteDAO ingredienteDAO; 

    public ProductoDAOImpl() {
        this.conexion = ConexionDB.getConexion();
        this.ingredienteDAO = new IngredienteDAOImpl();
    }

    @Override
    public boolean insertar(Producto producto) {
        // Se insertan primero los datos del producto para obtener su ID generado.
        String sqlProducto = "INSERT INTO productos (nombre, categoria, precio_base) VALUES (?, ?, ?)";
        String sqlReceta = "INSERT INTO receta_producto (id_producto, id_ingrediente, cantidad) VALUES (?, ?, ?)";
        
        try {
            // Producto y receta forman una sola operacion: ambos se guardan o ninguno.
            conexion.setAutoCommit(false); // Iniciar transacción

            int idProductoGenerado = 0;
            try (PreparedStatement ps = conexion.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getCategoria());
                ps.setDouble(3, producto.getPrecioBase());
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idProductoGenerado = rs.getInt(1);
                }
            }

            // Insertar los ingredientes de la receta
            try (PreparedStatement psReceta = conexion.prepareStatement(sqlReceta)) {
                for (Map.Entry<Ingrediente, Integer> entry : producto.getRecetaBase().entrySet()) {
                    psReceta.setInt(1, idProductoGenerado);
                    psReceta.setInt(2, entry.getKey().getId());
                    psReceta.setInt(3, entry.getValue());
                    psReceta.addBatch();
                }
                psReceta.executeBatch();
            }

            conexion.commit(); // Confirma el producto y todas sus filas de receta.
            return true;
        } catch (SQLException e) {
            // Si falla cualquiera de los INSERT, se deshacen tambien los anteriores.
            try { conexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error al insertar producto y receta: " + e.getMessage());
            return false;
        } finally {
            try { conexion.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Producto> listar() {
        // Cada producto se carga junto con los ingredientes que forman su receta.
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Producto p = new Producto(rs.getInt("id"), rs.getString("nombre"), 
                                          rs.getString("categoria"), rs.getDouble("precio_base"));
                cargarRecetaAProducto(p);
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private void cargarRecetaAProducto(Producto p) {
        // La tabla receta_producto relaciona un producto con cantidades de ingredientes.
        String sql = "SELECT id_ingrediente, cantidad FROM receta_producto WHERE id_producto=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingrediente ing = ingredienteDAO.obtenerPorId(rs.getInt("id_ingrediente"));
                    if (ing != null) {
                        p.agregarIngredienteAReceta(ing, rs.getInt("cantidad"));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public boolean actualizar(Producto producto) {
        // Actualizar un producto implica sincronizar sus datos y reemplazar su receta completa.
        String sqlProducto = "UPDATE productos SET nombre=?, categoria=?, precio_base=? WHERE id=?";
        String sqlBorrarRecetaVieja = "DELETE FROM receta_producto WHERE id_producto=?";
        String sqlInsertarRecetaNueva = "INSERT INTO receta_producto (id_producto, id_ingrediente, cantidad) VALUES (?, ?, ?)";

        try {
            conexion.setAutoCommit(false); // Transacción para garantizar consistencia

            // La receta no se modifica parcialmente: se reemplaza completa dentro de la transaccion.
            // 1. Actualizar datos base del producto
            try (PreparedStatement ps = conexion.prepareStatement(sqlProducto)) {
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getCategoria());
                ps.setDouble(3, producto.getPrecioBase());
                ps.setInt(4, producto.getId());
                ps.executeUpdate();
            }

            // 2. Borrar la receta anterior
            try (PreparedStatement psDelete = conexion.prepareStatement(sqlBorrarRecetaVieja)) {
                psDelete.setInt(1, producto.getId());
                psDelete.executeUpdate();
            }

            // 3. Insertar la nueva receta actualizada
            try (PreparedStatement psInsert = conexion.prepareStatement(sqlInsertarRecetaNueva)) {
                for (Map.Entry<Ingrediente, Integer> entry : producto.getRecetaBase().entrySet()) {
                    psInsert.setInt(1, producto.getId());
                    psInsert.setInt(2, entry.getKey().getId());
                    psInsert.setInt(3, entry.getValue());
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }

            conexion.commit();
            return true;
        } catch (SQLException e) {
            try { conexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        } finally {
            try { conexion.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public boolean eliminar(int id) {
        // La base de datos se encarga de borrar la receta automáticamente si configuraste ON DELETE CASCADE
        String sql = "DELETE FROM productos WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false; 
        }
    }
}