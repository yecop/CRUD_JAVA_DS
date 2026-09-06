package persistencia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VentaDAOImpl implements IVentaDAO {
    private Connection conexion;

    public VentaDAOImpl() { this.conexion = ConexionDB.getConexion(); }

    @Override
    public boolean registrarVenta(int idCliente, int idProducto, double total, Map<Integer, Integer> recetaFinal) {
        String sqlVenta = "INSERT INTO ventas (id_cliente, total) VALUES (?, ?)";
        // Inyectamos el id_producto para saber qué compró
        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, id_ingrediente, cantidad_usada, subtotal_extra) VALUES (?, ?, ?, ?, 0)";
        
        try {
            conexion.setAutoCommit(false);
            int idVenta = 0;
            
            try (PreparedStatement ps = conexion.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setDouble(2, total);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) idVenta = rs.getInt(1); }
            }
            
            try (PreparedStatement psDet = conexion.prepareStatement(sqlDetalle)) {
                for (Map.Entry<Integer, Integer> entry : recetaFinal.entrySet()) {
                    psDet.setInt(1, idVenta);
                    psDet.setInt(2, idProducto); // Guardamos el producto
                    psDet.setInt(3, entry.getKey());
                    psDet.setInt(4, entry.getValue());
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }
            conexion.commit();
            return true;
        } catch (SQLException e) {
            try { conexion.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { conexion.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    @Override
    public List<String[]> obtenerHistorialVentas() {
        List<String[]> historial = new ArrayList<>();
        
        // Magia de MySQL: GROUP_CONCAT agrupa los ingredientes en un solo texto separado por comas
        String sql = "SELECT v.id, v.fecha, c.nombre_razon, p.nombre as producto, " +
                     "GROUP_CONCAT(CONCAT(i.nombre, ' (x', dv.cantidad_usada, ')') SEPARATOR ', ') as detalle, " +
                     "v.total " +
                     "FROM ventas v " +
                     "JOIN clientes c ON v.id_cliente = c.id " +
                     "JOIN detalle_ventas dv ON dv.id_venta = v.id " +
                     "JOIN productos p ON dv.id_producto = p.id " +
                     "JOIN ingredientes i ON dv.id_ingrediente = i.id " +
                     "GROUP BY v.id, v.fecha, c.nombre_razon, p.nombre, v.total " +
                     "ORDER BY v.fecha DESC";
                     
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                // Envolvemos el resultado en HTML para que la tabla lo muestre en varias líneas bonito
                String detallesHtml = "<html><body style='width: 230px; padding: 2px;'>" +
                                      "<b>" + rs.getString("producto") + "</b><br>" +
                                      "<font color='gray'>" + rs.getString("detalle") + "</font>" +
                                      "</body></html>";

                historial.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("fecha"),
                    rs.getString("nombre_razon"),
                    detallesHtml, // Nueva columna con todo el detalle de la receta
                    "$" + rs.getDouble("total")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return historial;
    }
}