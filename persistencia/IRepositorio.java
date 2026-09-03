package persistencia;
import java.util.List;

public interface IRepositorio<T> {
    void agregar(T entidad);
    void eliminar(int id);
    List<T> listar();
    int generarNuevoId();
}