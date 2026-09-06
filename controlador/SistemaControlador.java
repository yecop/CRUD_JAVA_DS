package controlador;

import modelo.Ingrediente;
import modelo.Producto;
import modelo.Cliente;
import modelo.PersonaNatural;
import modelo.Empresa;

import persistencia.IIngredienteDAO;
import persistencia.IProductoDAO;
import persistencia.IClienteDAO;
import persistencia.IngredienteDAOImpl;
import persistencia.ProductoDAOImpl;
import persistencia.ClienteDAOImpl;
import persistencia.IVentaDAO;

import java.util.List;
import java.util.Map;

public class SistemaControlador {
    
    private IIngredienteDAO ingredienteDAO;
    private IProductoDAO productoDAO;
    private IClienteDAO clienteDAO; 
    private IVentaDAO ventaDAO;

    public SistemaControlador() {
        this.ingredienteDAO = new IngredienteDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl(); 
        this.ventaDAO = new persistencia.VentaDAOImpl();
    }

    // =========================================================================
    //                        GESTIÓN DE INGREDIENTES
    // =========================================================================

    public boolean registrarIngrediente(String nombre, int stock, double precioExtra) throws Exception {
        String nombreLimpio = nombre.trim();
        
        // 1. Regla de Negocio: Validar que no exista un ingrediente con el mismo nombre (Ignorando mayúsculas/minúsculas)
        boolean existe = ingredienteDAO.listar().stream()
            .anyMatch(ing -> ing.getNombre().trim().equalsIgnoreCase(nombreLimpio));
            
        if (existe) {
            throw new Exception("El ingrediente '" + nombreLimpio + "' ya existe en el inventario.\nUse el botón 'Actualizar' si desea modificar su stock.");
        }

        // 2. Si pasa la validación, se guarda en base de datos
        Ingrediente nuevo = new Ingrediente(0, nombreLimpio, stock, precioExtra);
        return ingredienteDAO.insertar(nuevo);
    }

    public List<Ingrediente> obtenerIngredientes() {
        return ingredienteDAO.listar();
    }

    public boolean actualizarIngrediente(int id, String nombre, int stock, double precioExtra) {
        Ingrediente modificado = new Ingrediente(id, nombre, stock, precioExtra);
        return ingredienteDAO.actualizar(modificado);
    }

    public boolean eliminarIngrediente(int id) {
        return ingredienteDAO.eliminar(id);
    }

    // =========================================================================
    //                  GESTIÓN DE PRODUCTOS (CATÁLOGO Y RECETAS)
    // =========================================================================

    public boolean registrarProductoConReceta(String nombre, String categoria, double precioBase, Map<Integer, Integer> recetaIdsCantidades) {
        Producto nuevoProducto = new Producto(0, nombre, categoria, precioBase);

        if (recetaIdsCantidades != null && !recetaIdsCantidades.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : recetaIdsCantidades.entrySet()) {
                Ingrediente ing = ingredienteDAO.obtenerPorId(entry.getKey());
                if (ing != null) {
                    nuevoProducto.agregarIngredienteAReceta(ing, entry.getValue());
                }
            }
        }
        return productoDAO.insertar(nuevoProducto);
    }

    public List<Producto> obtenerProductos() {
        return productoDAO.listar();
    }

    public boolean actualizarProducto(int id, String nombre, String categoria, double precioBase, Map<Integer, Integer> recetaIdsCantidades) {
        Producto modificado = new Producto(id, nombre, categoria, precioBase);
        
        // Reconstruimos la receta con los ingredientes actualizados
        if (recetaIdsCantidades != null && !recetaIdsCantidades.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : recetaIdsCantidades.entrySet()) {
                Ingrediente ing = ingredienteDAO.obtenerPorId(entry.getKey());
                if (ing != null) {
                    modificado.agregarIngredienteAReceta(ing, entry.getValue());
                }
            }
        }
        // El DAO se encarga de borrar la receta vieja e insertar esta nueva (Transacción SQL)
        return productoDAO.actualizar(modificado);
    }

    public boolean eliminarProducto(int id) {
        return productoDAO.eliminar(id);
    }

    // =========================================================================
    //                        GESTIÓN DE CLIENTES Y POS
    // =========================================================================

    public Cliente buscarClientePos(String identificacion) {
        return clienteDAO.buscarPorIdentificacion(identificacion);
    }

    public Cliente registrarClienteExpress(boolean esEmpresa, String identificacion, String nombre, String telefono, String correo) {
        Cliente nuevo;
        if (esEmpresa) {
            nuevo = new Empresa(0, telefono, correo, identificacion, nombre);
        } else {
            nuevo = new PersonaNatural(0, telefono, correo, identificacion, nombre);
        }
        
        if (clienteDAO.insertar(nuevo)) {
            return nuevo;
        }
        return null;
    }

    public double procesarVenta(int idCliente, int idProducto, Map<Integer, Integer> recetaFinal) throws Exception {
        Producto productoBase = productoDAO.listar().stream()
            .filter(p -> p.getId() == idProducto)
            .findFirst()
            .orElseThrow(() -> new Exception("Producto no encontrado"));

        double totalVenta = productoBase.getPrecioBase();
        
        for (Map.Entry<Integer, Integer> entry : recetaFinal.entrySet()) {
            int idIngrediente = entry.getKey();
            int cantidadSolicitada = entry.getValue();
            
            Ingrediente ingBD = ingredienteDAO.obtenerPorId(idIngrediente);
            if (ingBD == null) throw new Exception("Ingrediente inválido en la orden.");
            
            if (ingBD.getStock() < cantidadSolicitada) {
                throw new Exception("Stock insuficiente de: " + ingBD.getNombre() + ". Disponible: " + ingBD.getStock());
            }

            int cantidadBase = productoBase.getRecetaBase().entrySet().stream()
                .filter(e -> e.getKey().getId() == idIngrediente)
                .map(Map.Entry::getValue)
                .findFirst().orElse(0);

            if (cantidadSolicitada > cantidadBase) {
                int extras = cantidadSolicitada - cantidadBase;
                totalVenta += (extras * ingBD.getPrecioExtra());
            }
        }

        for (Map.Entry<Integer, Integer> entry : recetaFinal.entrySet()) {
            Ingrediente ingBD = ingredienteDAO.obtenerPorId(entry.getKey());
            ingBD.setStock(ingBD.getStock() - entry.getValue());
            ingredienteDAO.actualizar(ingBD); 
        }
        ventaDAO.registrarVenta(idCliente, idProducto, totalVenta, recetaFinal);
        
        return totalVenta;
    }
    public List<Cliente> obtenerClientes() {
        return clienteDAO.listar();
    }

    public boolean actualizarCliente(int id, boolean esEmpresa, String identificacion, String nombre, String telefono, String correo) {
        Cliente modificado;
        if (esEmpresa) {
            modificado = new Empresa(id, telefono, correo, identificacion, nombre);
        } else {
            modificado = new PersonaNatural(id, telefono, correo, identificacion, nombre);
        }
        return clienteDAO.actualizar(modificado);
    }

    public boolean eliminarCliente(int id) {
        return clienteDAO.eliminar(id);
    }

    public List<String[]> obtenerHistorialVentas() {
        return ventaDAO.obtenerHistorialVentas();
    }
}