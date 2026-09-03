package modelo;

public class Pizza extends Producto {
    private String tamaño;
    private String tipoMasa;

    public Pizza(int id, String nombre, double precioBase, String tamaño, String tipoMasa) {
        super(id, nombre, "Pizzas", precioBase);
        this.tamaño = tamaño;
        this.tipoMasa = tipoMasa;
    }

    public String getTamaño() { return tamaño; }
    public String getTipoMasa() { return tipoMasa; }

    @Override
    public String obtenerDetallesPreparacion() {
        return "Horno: Pizza " + tamaño + " con masa " + tipoMasa;
    }
}