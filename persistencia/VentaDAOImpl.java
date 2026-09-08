package persistencia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Implementacion JDBC para registrar ventas y consultar su historial.
public class VentaDAOImpl implements IVentaDAO {
    private Connection conexion;

    public VentaDAOImpl() { this.conexion = ConexionDB.getConexion(); }

    @Override
    public boolean registrarVenta(int idCliente, int idProducto, double total, Map<Integer, Integer> recetaFinal) {
        // La venta tiene una cabecera y varias filas de detalle asociadas por id_venta.
        String sqlVenta = "INSERT INTO ventas (id_cliente, total) VALUES (?, ?)";
        // Inyectamos el id_producto para saber qué compró
        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, id_ingrediente, cantidad_usada, subtotal_extra) VALUES (?, ?, ?, ?, 0)";
        
        try {
            // La venta y su detalle deben conservarse juntos para que el historial sea consistente.
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
            // Evita dejar una venta sin detalle si falla alguno de los INSERT.
            try { conexion.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { conexion.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    @Override
    public List<String[]> obtenerHistorialVentas() {
        // La consulta combina cliente, producto e ingredientes para formar una fila visible en la tabla.
        List<String[]> historial = new ArrayList<>();
        
        //agrupa los ingredientes en un solo texto separado por comas
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
                    detallesHtml, // todo el detalle de la receta
                    "$" + rs.getDouble("total")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return historial;
    }
}