package modelo;
import java.util.HashMap;
import java.util.Map;

public class Producto {
    private int id;
    private String nombre;
    private String categoria;
    private double precioBase;
    
    // La clave es el ingrediente y el valor indica cuantas unidades requiere la receta.
    private Map<Ingrediente, Integer> recetaBase;

    public Producto(int id, String nombre, String categoria, double precioBase) {
        this.id = id; this.nombre = nombre; this.categoria = categoria; this.precioBase = precioBase;
        this.recetaBase = new HashMap<>();
    }

    public void agregarIngredienteAReceta(Ingrediente ing, int cantidad) {
        // put reemplaza la cantidad si el ingrediente ya estaba incluido en la receta.
        this.recetaBase.put(ing, cantidad);
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecioBase() { return precioBase; }
    public Map<Ingrediente, Integer> getRecetaBase() { return recetaBase; }
}