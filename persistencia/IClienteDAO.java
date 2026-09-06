package persistencia;
import modelo.Cliente;
import java.util.List;

public interface IClienteDAO {
    boolean insertar(Cliente cliente);
    Cliente buscarPorIdentificacion(String identificacion);
    
    List<Cliente> listar();
    boolean actualizar(Cliente cliente);
    boolean eliminar(int id);
}