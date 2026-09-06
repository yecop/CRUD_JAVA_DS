package persistencia;
import modelo.Ingrediente;
import java.util.List;

public interface IIngredienteDAO {
    boolean insertar(Ingrediente ingrediente);
    List<Ingrediente> listar();
    boolean actualizar(Ingrediente ingrediente);
    boolean eliminar(int id);
    Ingrediente obtenerPorId(int id);
}