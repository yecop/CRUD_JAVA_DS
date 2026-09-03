package controlador;

import modelo.Producto;
import modelo.Cliente;
import persistencia.BaseDeDatos;
import persistencia.GestorArchivos;

import java.util.List;

public class SistemaControlador {
    private BaseDeDatos bd;

    public SistemaControlador() {
        this.bd = GestorArchivos.cargar();
    }

    // --- Lógica de Productos ---
    public int generarIdProducto() {
        return bd.getProductos().stream().mapToInt(Producto::getId).max().orElse(0) + 1;
    }

    public void registrarProducto(String tipo, String nombre, double precio, String ingredientes, String extra1, Object extra2) {
        Producto nuevo = ProductoFactory.crearProducto(tipo, generarIdProducto(), nombre, precio, extra1, extra2);
        agregarIngredientesAProducto(nuevo, ingredientes);
        bd.getProductos().add(nuevo);
        GestorArchivos.guardar(bd);
    }

    // NUEVO MÉTODO: Actualizar Producto
    public void actualizarProducto(int indexLista, int idOriginal, String tipo, String nombre, double precio, String ingredientes, String extra1, Object extra2) {
        // Creamos un nuevo objeto pero mantenemos su ID original
        Producto modificado = ProductoFactory.crearProducto(tipo, idOriginal, nombre, precio, extra1, extra2);
        agregarIngredientesAProducto(modificado, ingredientes);
        
        // Reemplazamos el objeto en la posición exacta de la lista
        bd.getProductos().set(indexLista, modificado);
        GestorArchivos.guardar(bd);
    }

    private void agregarIngredientesAProducto(Producto p, String ingredientes) {
        if (ingredientes != null && !ingredientes.trim().isEmpty()) {
            for (String ing : ingredientes.split(",")) {
                p.agregarIngrediente(ing.trim());
            }
        }
    }

    public List<Producto> obtenerProductos() { return bd.getProductos(); }

    public void eliminarProducto(int index) {
        bd.getProductos().remove(index);
        GestorArchivos.guardar(bd);
    }

    // --- Lógica de Clientes ---
    public int generarIdCliente() {
        return bd.getClientes().stream().mapToInt(Cliente::getId).max().orElse(0) + 1;
    }

    public void registrarCliente(String nombre, String telefono) {
        bd.getClientes().add(new Cliente(generarIdCliente(), nombre, telefono));
        GestorArchivos.guardar(bd);
    }

    // NUEVO MÉTODO: Actualizar Cliente
    public void actualizarCliente(int indexLista, int idOriginal, String nombre, String telefono) {
        bd.getClientes().set(indexLista, new Cliente(idOriginal, nombre, telefono));
        GestorArchivos.guardar(bd);
    }

    public List<Cliente> obtenerClientes() { return bd.getClientes(); }

    public void eliminarCliente(int index) {
        bd.getClientes().remove(index);
        GestorArchivos.guardar(bd);
    }
}