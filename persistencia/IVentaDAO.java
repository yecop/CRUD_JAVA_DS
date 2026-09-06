package persistencia;
import java.util.List;
import java.util.Map;

public interface IVentaDAO {
    boolean registrarVenta(int idCliente, int idProducto, double total, Map<Integer, Integer> recetaFinal);
    List<String[]> obtenerHistorialVentas();
}