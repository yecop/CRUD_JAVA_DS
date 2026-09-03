package modelo;

public class Hamburguesa extends Producto {
    private String tipoCarne;
    private boolean esDoble;

    public Hamburguesa(int id, String nombre, double precioBase, String tipoCarne, boolean esDoble) {
        super(id, nombre, "Hamburguesas", precioBase);
        this.tipoCarne = tipoCarne;
        this.esDoble = esDoble;
    }


    public String getTipoCarne() { return tipoCarne; }
    public boolean isEsDoble() { return esDoble; }

    @Override
    public String obtenerDetallesPreparacion() {
        String tamaño = esDoble ? "Doble" : "Sencilla";
        return "Parrilla: Hamburguesa " + tamaño + " de " + tipoCarne;
    }
}