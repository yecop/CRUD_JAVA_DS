package persistencia;
import modelo.Cliente;
import java.util.List;

// Operaciones de persistencia disponibles para clientes, sin exponer SQL a la vista.
public interface IClienteDAO {
    // Guarda una persona natural o una empresa y obtiene su ID generado.
    boolean insertar(Cliente cliente);
    // Busca un cliente por cedula o NIT.
    Cliente buscarPorIdentificacion(String identificacion);
    
    // Operaciones CRUD basicas del modulo de clientes.
    List<Cliente> listar();
    boolean actualizar(Cliente cliente);
    boolean eliminar(int id);
}