package modelo;

public class Ingrediente {
    private int id;
    private String nombre;
    private int stock;
    private double precioExtra;

    public Ingrediente(int id, String nombre, int stock, double precioExtra) {
        this.id = id; this.nombre = nombre; this.stock = stock; this.precioExtra = precioExtra;
    }

    public void reducirStock(int cantidad) {
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