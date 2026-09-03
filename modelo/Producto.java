package modelo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Usamos 'protected' para que las clases hijas tengan acceso
    protected int id;
    protected String nombre;
    protected String categoria;
    protected double precioBase;
    protected List<String> ingredientes;

    public Producto(int id, String nombre, String categoria, double precioBase) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioBase = precioBase;
        this.ingredientes = new ArrayList<>();
    }

    // Método común para todas las comidas
    public void agregarIngrediente(String ingrediente) {
        this.ingredientes.add(ingrediente);
    }

    // MÉTODO ABSTRACTO: Obliga a cada comida a definir sus detalles
    public abstract String obtenerDetallesPreparacion();

    // Getters comunes
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecioBase() { return precioBase; }
    public List<String> getIngredientes() { return ingredientes; }
}