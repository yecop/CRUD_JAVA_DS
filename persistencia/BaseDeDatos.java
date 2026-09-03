package persistencia;
import modelo.Producto;
import modelo.Cliente;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseDeDatos implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Producto> productos = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();

    public List<Producto> getProductos() { return productos; }
    public List<Cliente> getClientes() { return clientes; }
}