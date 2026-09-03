package controlador;
import modelo.*;

public class ProductoFactory {
    // Si agregas un nuevo producto, SOLO modificas esta fábrica. La vista queda intacta.
    public static Producto crearProducto(String tipo, int id, String nombre, double precio, String extra1, Object extra2) {
        switch (tipo) {
            case "Hamburguesa":
                return new Hamburguesa(id, nombre, precio, extra1, (Boolean) extra2);
            case "Pizza":
                return new Pizza(id, nombre, precio, extra1, (String) extra2);
            default:
                throw new IllegalArgumentException("Tipo de producto no soportado");
        }
    }
}