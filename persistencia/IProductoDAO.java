package persistencia;
import modelo.Producto;
import java.util.List;

public interface IProductoDAO {
    boolean insertar(Producto producto);
    List<Producto> listar();
    boolean actualizar(Producto producto);
    boolean eliminar(int id);
}