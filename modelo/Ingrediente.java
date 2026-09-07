package modelo;

// Entidad de inventario utilizada directamente por las recetas y las ventas.
public class Ingrediente {
    private int id;
    private String nombre;
    private int stock;
    private double precioExtra;

    public Ingrediente(int id, String nombre, int stock, double precioExtra) {
        this.id = id; this.nombre = nombre; this.stock = stock; this.precioExtra = precioExtra;
    }

    // Reduce existencias solo cuando hay suficiente inventario disponible.
    public void reducirStock(int cantidad) {
        // La validacion se mantiene en el modelo para impedir que el stock quede negativo.
        if (this.stock >= cantidad) this.stock -= cantidad;
        else throw new IllegalArgumentException("Sin stock de: " + nombre);
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public double getPrecioExtra() { return precioExtra; }
    public void setStock(int stock) { this.stock = stock; }
}