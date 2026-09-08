package vista;

import controlador.SistemaControlador;
import modelo.Ingrediente;
import modelo.Producto;
import modelo.Cliente;
import modelo.PersonaNatural;
import modelo.Empresa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

// Ventana principal que agrupa todos los modulos funcionales mediante pestañas.
public class VentanaPrincipal extends JFrame {
    
    private SistemaControlador controlador;

    // --- Modelos de Tabla ---
    private DefaultTableModel modeloIngredientes;
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloRecetaTemporal; 
    private DefaultTableModel modeloClientes; 
    private DefaultTableModel modeloHistorial;

    // --- Componentes Inventario (Ingredientes) ---
    private JTextField txtNombreIng, txtStockIng, txtPrecioExtraIng;

    // Controles del catalogo y estructuras temporales para construir recetas.
    private JTextField txtNombreProd, txtPrecioProd;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbIngredientesBD;
    private JSpinner spnCantidadIngrediente;
    private Map<Integer, Integer> recetaTemporal = new HashMap<>();
    private Map<Integer, String> nombresIngredientesTemporal = new HashMap<>(); // Para dibujar la tabla temporal

    // --- Componentes Clientes ---
    private JComboBox<String> cmbTipoClienteCrud;
    private JTextField txtDocClienteCrud, txtNombreClienteCrud, txtTelClienteCrud, txtCorreoClienteCrud;

    // --- Componentes Facturación (POS) ---
    private Cliente clienteActualFacturacion = null;
    private JLabel lblNombreCliente;
    private JComboBox<String> cmbPosClientes;
    private JComboBox<String> cmbPosProductos;
    private JComboBox<String> cmbPosExtras;
    private DefaultTableModel modeloOrdenActual;
    private JLabel lblTotalPos;
    private Map<Integer, Integer> ordenTemporal = new HashMap<>();

    public VentanaPrincipal() {
        // La vista solo coordina controles y delega las reglas al controlador.
        this.controlador = new SistemaControlador(); 
        
        setTitle("ERP Comidas Rápidas - 100% CRUD & POS");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cada pestaña representa un modulo independiente de la aplicacion.
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("🍅 1. Inventario (CRUD)", crearPanelIngredientes());
        pestañas.addTab("🍔 2. Catálogo y Recetas (CRUD)", crearPanelProductos());
        pestañas.addTab("👥 3. Gestión Clientes (CRUD)", crearPanelClientes());
        pestañas.addTab("🛒 4. Punto de Venta (POS)", crearPanelFacturacion());
        pestañas.addTab("📊 5. Historial de Ventas", crearPanelHistorialVentas());
        
        add(pestañas);
    }

    // =========================================================================
    //                        MÓDULO 1: INGREDIENTES (CRUD)
    // =========================================================================

    private JPanel crearPanelIngredientes() {
        // Construye el formulario CRUD y sincroniza sus acciones con el inventario.
        JPanel panel = new JPanel(new BorderLayout());
        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        txtNombreIng = new JTextField(12);
        txtStockIng = new JTextField(5);
        txtPrecioExtraIng = new JTextField(6);
        
        pnlForm.add(new JLabel("Nombre:")); pnlForm.add(txtNombreIng);
        pnlForm.add(new JLabel("Stock Inicial:")); pnlForm.add(txtStockIng);
        pnlForm.add(new JLabel("Precio Extra ($):")); pnlForm.add(txtPrecioExtraIng);
        
        JButton btnAgregarIng = new JButton("Agregar");
        JButton btnActualizarIng = new JButton("Actualizar");
        JButton btnEliminarIng = new JButton("Eliminar");
        pnlForm.add(btnAgregarIng); pnlForm.add(btnActualizarIng); pnlForm.add(btnEliminarIng);
        
        String[] columnas = {"ID", "Nombre", "Stock", "Precio Extra"};
        modeloIngredientes = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modeloIngredientes);
        actualizarTablaIngredientes();

        // Cada evento se delega a un metodo para mantener corto el constructor del panel.
        configurarSeleccionIngrediente(tabla);
        btnAgregarIng.addActionListener(e -> agregarIngrediente());
        btnActualizarIng.addActionListener(e -> actualizarIngrediente(tabla));
        btnEliminarIng.addActionListener(e -> eliminarIngrediente(tabla));

        panel.add(pnlForm, BorderLayout.NORTH);
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(crearBuscadorParaTabla(tabla, modeloIngredientes), BorderLayout.NORTH);
        pnlCentro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(pnlCentro, BorderLayout.CENTER);
        return panel;
    }

    private void configurarSeleccionIngrediente(JTable tabla) {
        // Al seleccionar una fila, sus datos pasan al formulario para editarla.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0 && !e.getValueIsAdjusting()) {
                txtNombreIng.setText(tabla.getValueAt(fila, 1).toString());
                txtStockIng.setText(tabla.getValueAt(fila, 2).toString());
                txtPrecioExtraIng.setText(tabla.getValueAt(fila, 3).toString().replace("$", ""));
            }
        });
    }

    private void agregarIngrediente() {
        try {
            // Se valida el formato en la vista y la regla de duplicados en el controlador.
            if (txtNombreIng.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del ingrediente no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            controlador.registrarIngrediente(
                txtNombreIng.getText(),
                Integer.parseInt(txtStockIng.getText()),
                Double.parseDouble(txtPrecioExtraIng.getText())
            );

            actualizarTablaIngredientes();
            actualizarComboIngredientes();
            limpiarCamposIngredientes();
            JOptionPane.showMessageDialog(this, "Ingrediente guardado exitosamente.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese números válidos para Stock y Precio.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Aqui se muestra, por ejemplo, el error de ingrediente duplicado.
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarIngrediente(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            try {
                int id = (int) tabla.getValueAt(fila, 0);
                controlador.actualizarIngrediente(id, txtNombreIng.getText(), Integer.parseInt(txtStockIng.getText()), Double.parseDouble(txtPrecioExtraIng.getText()));
                actualizarTablaIngredientes();
                actualizarComboIngredientes();
                limpiarCamposIngredientes();
                tabla.clearSelection();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en los datos.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un ingrediente para actualizar.");
        }
    }

    private void eliminarIngrediente(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            int id = (int) tabla.getValueAt(fila, 0);
            controlador.eliminarIngrediente(id);
            actualizarTablaIngredientes();
            actualizarComboIngredientes();
            limpiarCamposIngredientes();
        }
    }

    private void limpiarCamposIngredientes() {
        txtNombreIng.setText(""); txtStockIng.setText(""); txtPrecioExtraIng.setText("");
    }

    private void actualizarTablaIngredientes() {
        // Se limpia la tabla antes de cargar la consulta mas reciente.
        modeloIngredientes.setRowCount(0);
        for (Ingrediente i : controlador.obtenerIngredientes()) {
            modeloIngredientes.addRow(new Object[]{i.getId(), i.getNombre(), i.getStock(), "$" + i.getPrecioExtra()});
        }
    }

    // =========================================================================
    //                  MÓDULO 2: PRODUCTOS Y RECETAS (CRUD)
    // =========================================================================

private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlDatosBasicos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlDatosBasicos.setBorder(BorderFactory.createTitledBorder("Paso 1: Datos Básicos"));
        
        txtNombreProd = new JTextField(15);
        txtPrecioProd = new JTextField(8);
        cmbCategoria = new JComboBox<>(new String[]{"Hamburguesa", "Pizza", "Perro Caliente", "Bebida", "Otro"});
        
        pnlDatosBasicos.add(new JLabel("Nombre:")); pnlDatosBasicos.add(txtNombreProd);
        pnlDatosBasicos.add(new JLabel("Categoría:")); pnlDatosBasicos.add(cmbCategoria);
        pnlDatosBasicos.add(new JLabel("Precio Base ($):")); pnlDatosBasicos.add(txtPrecioProd);

        JPanel pnlConstructor = new JPanel();
        pnlConstructor.setLayout(new BoxLayout(pnlConstructor, BoxLayout.Y_AXIS));
        pnlConstructor.setBorder(BorderFactory.createTitledBorder("Paso 2: Armar Receta"));
        pnlConstructor.setPreferredSize(new Dimension(380, 0));

        JPanel pnlSelector = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel pnlFila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbIngredientesBD = new JComboBox<>();
        actualizarComboIngredientes(); 
        pnlFila1.add(new JLabel("Ingrediente:")); pnlFila1.add(cmbIngredientesBD);

        JPanel pnlFila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spnCantidadIngrediente = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        pnlFila2.add(new JLabel("Cantidad requerida:")); pnlFila2.add(spnCantidadIngrediente);

        JPanel pnlFila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddReceta = new JButton("Añadir a Receta");
        JButton btnLimpiarReceta = new JButton("Limpiar Receta");
        pnlFila3.add(btnAddReceta); pnlFila3.add(btnLimpiarReceta);

        pnlSelector.add(pnlFila1); pnlSelector.add(pnlFila2); pnlSelector.add(pnlFila3);

        String[] colReceta = {"ID", "Ingrediente", "Cant."};
        modeloRecetaTemporal = new DefaultTableModel(colReceta, 0);
        JTable tablaReceta = new JTable(modeloRecetaTemporal);

        pnlConstructor.add(pnlSelector);
        pnlConstructor.add(new JScrollPane(tablaReceta));

        // Botones de acción agrupados
        JPanel pnlAccionesFinales = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardarProducto = new JButton("Guardar Nuevo");
        JButton btnActualizarProducto = new JButton("Actualizar Seleccionado");
        btnGuardarProducto.setBackground(new Color(50, 150, 50));
        btnGuardarProducto.setForeground(Color.WHITE);
        pnlAccionesFinales.add(btnGuardarProducto);
        pnlAccionesFinales.add(btnActualizarProducto);
        
        pnlConstructor.add(pnlAccionesFinales);

        JPanel pnlCatalogo = new JPanel(new BorderLayout());
        pnlCatalogo.setBorder(BorderFactory.createTitledBorder("Catálogo Guardado"));
        
        String[] colProductos = {"ID", "Categoría", "Nombre", "Precio Base", "Composición (Receta)"};
        modeloProductos = new DefaultTableModel(colProductos, 0);
        JTable tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(50);
        actualizarTablaProductos();

        // 🔍 INYECCIÓN DEL BUSCADOR EN LA TABLA DE PRODUCTOS
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(crearBuscadorParaTabla(tablaProductos, modeloProductos), BorderLayout.NORTH);
        pnlCentro.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JPanel pnlAccionesProd = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEliminarProd = new JButton("Eliminar Producto");
        btnEliminarProd.setForeground(Color.RED);
        pnlAccionesProd.add(btnEliminarProd);

        pnlCatalogo.add(pnlAccionesProd, BorderLayout.NORTH);
        pnlCatalogo.add(pnlCentro, BorderLayout.CENTER); // Añadimos el contenedor con el buscador

        // --- EVENTO CLIC EN TABLA: Cargar Receta para Edición ---
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila >= 0 && !e.getValueIsAdjusting()) {
                int idProd = (int) tablaProductos.getValueAt(fila, 0);
                
                // Buscar el producto original en la lista del controlador
                Producto p = controlador.obtenerProductos().stream().filter(prod -> prod.getId() == idProd).findFirst().orElse(null);
                if (p != null) {
                    txtNombreProd.setText(p.getNombre());
                    cmbCategoria.setSelectedItem(p.getCategoria());
                    txtPrecioProd.setText(String.valueOf(p.getPrecioBase()));
                    
                    // Precargar la receta temporal
                    recetaTemporal.clear();
                    nombresIngredientesTemporal.clear();
                    for (Map.Entry<Ingrediente, Integer> entry : p.getRecetaBase().entrySet()) {
                        recetaTemporal.put(entry.getKey().getId(), entry.getValue());
                        nombresIngredientesTemporal.put(entry.getKey().getId(), entry.getKey().getNombre());
                    }
                    refrescarTablaRecetaTemporal();
                }
            }
        });

        // La receta temporal permite preparar los ingredientes antes de enviarlos al controlador.
        btnAddReceta.addActionListener(e -> {
            if (cmbIngredientesBD.getSelectedItem() == null) return;
            String seleccion = (String) cmbIngredientesBD.getSelectedItem();
            int idIngrediente = Integer.parseInt(seleccion.split(" - ")[0]);
            String nombreIng = seleccion.split(" - ")[1];
            int cantidad = (int) spnCantidadIngrediente.getValue();

            // Si se agrega el mismo ingrediente otra vez, se acumula su cantidad.
            recetaTemporal.put(idIngrediente, recetaTemporal.getOrDefault(idIngrediente, 0) + cantidad);
            nombresIngredientesTemporal.put(idIngrediente, nombreIng); 
            refrescarTablaRecetaTemporal();
        });

        btnLimpiarReceta.addActionListener(e -> {
            recetaTemporal.clear();
            nombresIngredientesTemporal.clear();
            refrescarTablaRecetaTemporal();
        });

        btnGuardarProducto.addActionListener(e -> {
            try {
                // La receta se exige antes de enviar el producto al controlador.
                if (recetaTemporal.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La receta debe tener ingredientes.");
                    return;
                }
                controlador.registrarProductoConReceta(txtNombreProd.getText(), (String) cmbCategoria.getSelectedItem(), Double.parseDouble(txtPrecioProd.getText()), recetaTemporal);
                txtNombreProd.setText(""); txtPrecioProd.setText("");
                recetaTemporal.clear(); nombresIngredientesTemporal.clear(); refrescarTablaRecetaTemporal();
                actualizarTablaProductos(); actualizarComboPosProductos(); 
                JOptionPane.showMessageDialog(this, "Producto guardado.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Verifique los datos."); }
        });

        // Evento Actualizar Producto
        btnActualizarProducto.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila >= 0) {
                try {
                    if (recetaTemporal.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "La receta no puede quedar vacía.");
                        return;
                    }
                    int idProd = (int) tablaProductos.getValueAt(fila, 0);
                    controlador.actualizarProducto(idProd, txtNombreProd.getText(), (String) cmbCategoria.getSelectedItem(), Double.parseDouble(txtPrecioProd.getText()), recetaTemporal);
                    
                    txtNombreProd.setText(""); txtPrecioProd.setText("");
                    recetaTemporal.clear(); nombresIngredientesTemporal.clear(); refrescarTablaRecetaTemporal();
                    actualizarTablaProductos(); actualizarComboPosProductos();
                    tablaProductos.clearSelection();
                    JOptionPane.showMessageDialog(this, "Producto y Receta actualizados.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Verifique los datos."); }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para actualizar.");
            }
        });

        btnEliminarProd.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila >= 0) {
                int idProd = (int) tablaProductos.getValueAt(fila, 0);
                controlador.eliminarProducto(idProd);
                actualizarTablaProductos(); actualizarComboPosProductos();
                recetaTemporal.clear(); nombresIngredientesTemporal.clear(); refrescarTablaRecetaTemporal();
                txtNombreProd.setText(""); txtPrecioProd.setText("");
            }
        });

        panel.add(pnlDatosBasicos, BorderLayout.NORTH);
        panel.add(pnlConstructor, BorderLayout.WEST);
        panel.add(pnlCatalogo, BorderLayout.CENTER);
        
        return panel;
    }


    private void refrescarTablaRecetaTemporal() {
        modeloRecetaTemporal.setRowCount(0);
        for (Map.Entry<Integer, Integer> entry : recetaTemporal.entrySet()) {
            modeloRecetaTemporal.addRow(new Object[]{entry.getKey(), nombresIngredientesTemporal.get(entry.getKey()), entry.getValue()});
        }
    }

    private void actualizarComboIngredientes() {
        if (cmbIngredientesBD != null) {
            cmbIngredientesBD.removeAllItems();
            for (Ingrediente i : controlador.obtenerIngredientes()) {
                cmbIngredientesBD.addItem(i.getId() + " - " + i.getNombre());
            }
        }
    }

    private void actualizarTablaProductos() {
        modeloProductos.setRowCount(0);
        for (Producto p : controlador.obtenerProductos()) {
            String detallesReceta = p.getRecetaBase().entrySet().stream()
                .map(entry -> entry.getKey().getNombre() + " (x" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
            String textoHtml = "<html><body style='width: 250px; padding: 2px; color: gray;'>" + detallesReceta + "</body></html>";
            modeloProductos.addRow(new Object[]{ p.getId(), p.getCategoria(), p.getNombre(), "$" + p.getPrecioBase(), textoHtml });
        }
    }

    // 
    //MÓDULO GESTIÓN DE CLIENTES 
    // 

    private JPanel crearPanelClientes() {
        // Construye el CRUD de personas naturales y empresas usando el mismo formulario.
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbTipoClienteCrud = new JComboBox<>(new String[]{"Persona Natural", "Empresa"});
        txtDocClienteCrud = new JTextField(10);
        txtNombreClienteCrud = new JTextField(15);
        txtTelClienteCrud = new JTextField(10);
        txtCorreoClienteCrud = new JTextField(12);

        pnlForm.add(new JLabel("Tipo:")); pnlForm.add(cmbTipoClienteCrud);
        pnlForm.add(new JLabel("CC/NIT:")); pnlForm.add(txtDocClienteCrud);
        pnlForm.add(new JLabel("Nombre/Razón:")); pnlForm.add(txtNombreClienteCrud);
        pnlForm.add(new JLabel("Tel:")); pnlForm.add(txtTelClienteCrud);
        pnlForm.add(new JLabel("Correo:")); pnlForm.add(txtCorreoClienteCrud);

        JButton btnAgregarCli = new JButton("Registrar");
        JButton btnActualizarCli = new JButton("Actualizar");
        JButton btnEliminarCli = new JButton("Eliminar");
        pnlForm.add(btnAgregarCli); 
        pnlForm.add(btnActualizarCli); 
        pnlForm.add(btnEliminarCli);

        String[] colClientes = {"ID", "Tipo", "CC/NIT", "Nombre", "Teléfono", "Correo"};
        modeloClientes = new DefaultTableModel(colClientes, 0);
        JTable tablaClientes = new JTable(modeloClientes);
        actualizarTablaClientes();

        // Evento para cargar datos al hacer clic en una fila
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0 && !e.getValueIsAdjusting()) {
                String tipo = tablaClientes.getValueAt(fila, 1).toString();
                cmbTipoClienteCrud.setSelectedIndex(tipo.equals("Empresa") ? 1 : 0);
                txtDocClienteCrud.setText(tablaClientes.getValueAt(fila, 2).toString());
                txtNombreClienteCrud.setText(tablaClientes.getValueAt(fila, 3).toString());
                txtTelClienteCrud.setText(tablaClientes.getValueAt(fila, 4).toString());
                txtCorreoClienteCrud.setText(tablaClientes.getValueAt(fila, 5).toString());
            }
        });

        btnAgregarCli.addActionListener(e -> {
            boolean esEmpresa = cmbTipoClienteCrud.getSelectedIndex() == 1;
            Cliente c = controlador.registrarClienteExpress(esEmpresa, txtDocClienteCrud.getText(), txtNombreClienteCrud.getText(), txtTelClienteCrud.getText(), txtCorreoClienteCrud.getText());
            if (c != null) {
                limpiarCamposClientes();
                actualizarTablaClientes();
                JOptionPane.showMessageDialog(this, "Cliente Registrado");
            }
        });

        btnActualizarCli.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tablaClientes.getValueAt(fila, 0);
                boolean esEmpresa = cmbTipoClienteCrud.getSelectedIndex() == 1;
                
                if (controlador.actualizarCliente(id, esEmpresa, txtDocClienteCrud.getText(), txtNombreClienteCrud.getText(), txtTelClienteCrud.getText(), txtCorreoClienteCrud.getText())) {
                    limpiarCamposClientes();
                    actualizarTablaClientes();
                    tablaClientes.clearSelection();
                    JOptionPane.showMessageDialog(this, "Cliente Actualizado");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.");
            }
        });

        btnEliminarCli.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tablaClientes.getValueAt(fila, 0);
                if (controlador.eliminarCliente(id)) {
                    limpiarCamposClientes();
                    actualizarTablaClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se puede eliminar porque tiene ventas asociadas.");
                }
            }
        });

        panel.add(pnlForm, BorderLayout.NORTH);
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(crearBuscadorParaTabla(tablaClientes, modeloClientes), BorderLayout.NORTH);
        pnlCentro.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        panel.add(pnlCentro, BorderLayout.CENTER);
        return panel;
    }

    private void limpiarCamposClientes() {
        txtDocClienteCrud.setText(""); txtNombreClienteCrud.setText(""); 
        txtTelClienteCrud.setText(""); txtCorreoClienteCrud.setText("");
    }

    private void actualizarTablaClientes() {
        modeloClientes.setRowCount(0);
        for (Cliente c : controlador.obtenerClientes()) {
            String tipo = (c instanceof Empresa) ? "Empresa" : "Persona";
            String identificacion = (c instanceof Empresa) ? ((Empresa)c).getNit() : ((PersonaNatural)c).getCedula();
            String nombre = (c instanceof Empresa) ? ((Empresa)c).getRazonSocial() : ((PersonaNatural)c).getNombreCompleto();
            
            modeloClientes.addRow(new Object[]{c.getId(), tipo, identificacion, nombre, c.getTelefono(), c.getCorreo()});
        }
    }

    // 
    //MÓDULO PUNTO DE VENTA
    //
    // 

    private JPanel crearPanelFacturacion() {
        // El POS prepara una orden, permite modificarla y finalmente la procesa.
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlCabecera = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlCabecera.setBorder(BorderFactory.createTitledBorder("Paso 1: Identificar Cliente y Producto"));
        
        JTextField txtBuscarDoc = new JTextField(10);
        JButton btnBuscarCliente = new JButton("Buscar Cliente");
        lblNombreCliente = new JLabel(" | Cliente: (Consumidor Final)   ");
        lblNombreCliente.setForeground(Color.BLUE);

        btnBuscarCliente.addActionListener(e -> {
            // Primero se intenta buscar el cliente; si no existe, se ofrece registrarlo.
            String doc = txtBuscarDoc.getText().trim();
            if (doc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese una Cédula o NIT.");
                return;
            }

            Cliente encontrado = controlador.buscarClientePos(doc);
            if (encontrado != null) {
                clienteActualFacturacion = encontrado;
                String nombreMostrado = (encontrado instanceof PersonaNatural) ? 
                    ((PersonaNatural)encontrado).getNombreCompleto() : ((Empresa)encontrado).getRazonSocial();
                lblNombreCliente.setText(" | Cliente: " + nombreMostrado + "   ");
            } else {
                int opcion = JOptionPane.showConfirmDialog(this, "Cliente no registrado. ¿Desea registrarlo ahora?", "Nuevo Cliente", JOptionPane.YES_NO_OPTION);
                if (opcion == JOptionPane.YES_OPTION) mostrarDialogoRegistroCliente(doc); 
            }
        });

        cmbPosProductos = new JComboBox<>();
        actualizarComboPosProductos();
        JButton btnIniciarOrden = new JButton("Cargar Receta Base");

        pnlCabecera.add(new JLabel("CC/NIT:")); pnlCabecera.add(txtBuscarDoc);
        pnlCabecera.add(btnBuscarCliente);
        pnlCabecera.add(lblNombreCliente);
        pnlCabecera.add(new JLabel("Producto:")); pnlCabecera.add(cmbPosProductos);
        pnlCabecera.add(btnIniciarOrden);

        JPanel pnlPersonalizar = new JPanel(new BorderLayout());
        pnlPersonalizar.setBorder(BorderFactory.createTitledBorder("Personalizar Orden (Extras / Quitar)"));

        JPanel pnlAccionesExtra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbPosExtras = new JComboBox<>();
        actualizarComboPosExtras();
        
        JButton btnAddExtra = new JButton("+ Agregar Extra");
        JButton btnRemoveIngrediente = new JButton("- Quitar 1 Unidad (Seleccionado)");
        
        pnlAccionesExtra.add(new JLabel("Ingrediente:")); pnlAccionesExtra.add(cmbPosExtras);
        pnlAccionesExtra.add(btnAddExtra);
        pnlAccionesExtra.add(btnRemoveIngrediente);

        String[] colOrden = {"ID", "Ingrediente", "Cantidad Final", "Estado"};
        modeloOrdenActual = new DefaultTableModel(colOrden, 0);
        JTable tablaOrden = new JTable(modeloOrdenActual);

        pnlPersonalizar.add(pnlAccionesExtra, BorderLayout.NORTH);
        pnlPersonalizar.add(new JScrollPane(tablaOrden), BorderLayout.CENTER);

        JPanel pnlCobro = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalPos = new JLabel("Total Estimado: $0.00");
        lblTotalPos.setFont(new Font("Arial", Font.BOLD, 18));
        
        JButton btnFacturar = new JButton("💰 PROCESAR VENTA");
        btnFacturar.setBackground(new Color(0, 102, 204));
        btnFacturar.setForeground(Color.WHITE);
        
        pnlCobro.add(lblTotalPos);
        pnlCobro.add(btnFacturar);

        btnIniciarOrden.addActionListener(e -> {
            if (cmbPosProductos.getSelectedItem() == null) return;
            String seleccion = (String) cmbPosProductos.getSelectedItem();
            int idProd = Integer.parseInt(seleccion.split(" - ")[0]);

            Producto p = controlador.obtenerProductos().stream().filter(prod -> prod.getId() == idProd).findFirst().orElse(null);
            if (p != null) {
                ordenTemporal.clear();
                modeloOrdenActual.setRowCount(0);
                for (Map.Entry<Ingrediente, Integer> entry : p.getRecetaBase().entrySet()) {
                    ordenTemporal.put(entry.getKey().getId(), entry.getValue());
                    modeloOrdenActual.addRow(new Object[]{entry.getKey().getId(), entry.getKey().getNombre(), entry.getValue(), "Base"});
                }
                lblTotalPos.setText("Total Estimado: $" + p.getPrecioBase());
            }
        });

        btnAddExtra.addActionListener(e -> {
            if (cmbPosExtras.getSelectedItem() == null) return;
            String seleccion = (String) cmbPosExtras.getSelectedItem();
            int idIng = Integer.parseInt(seleccion.split(" - ")[0]);
            ordenTemporal.put(idIng, ordenTemporal.getOrDefault(idIng, 0) + 1);
            refrescarTablaOrden();
        });

        btnRemoveIngrediente.addActionListener(e -> {
            int fila = tablaOrden.getSelectedRow();
            if (fila >= 0) {
                int idIng = (int) modeloOrdenActual.getValueAt(fila, 0);
                int cantidadActual = ordenTemporal.get(idIng);
                if (cantidadActual > 1) {
                    ordenTemporal.put(idIng, cantidadActual - 1);
                } else {
                    ordenTemporal.remove(idIng);
                }
                refrescarTablaOrden();
            }
        });

        btnFacturar.addActionListener(e -> {
            if (ordenTemporal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cargue un producto primero.");
                return;
            }
            try {
                int idCliente = (clienteActualFacturacion != null) ? clienteActualFacturacion.getId() : 1; 
                int idProducto = Integer.parseInt(((String)cmbPosProductos.getSelectedItem()).split(" - ")[0]);
                
                // El controlador valida stock, calcula extras, descuenta inventario y registra la venta.
                double totalFinal = controlador.procesarVenta(idCliente, idProducto, ordenTemporal);
                
                JOptionPane.showMessageDialog(this, "Venta Exitosa!\nTotal pagado: $" + totalFinal + "\nEl inventario ha sido descontado.");
                
                ordenTemporal.clear();
                modeloOrdenActual.setRowCount(0);
                lblTotalPos.setText("Total Estimado: $0.00");
                clienteActualFacturacion = null;
                lblNombreCliente.setText(" | Cliente: (Consumidor Final)   ");
                txtBuscarDoc.setText("");
                actualizarTablaIngredientes(); 
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en la venta: " + ex.getMessage(), "Error de Stock", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(pnlCabecera, BorderLayout.NORTH);
        panel.add(pnlPersonalizar, BorderLayout.CENTER);
        panel.add(pnlCobro, BorderLayout.SOUTH);
        
        return panel;
    }

    private void actualizarComboPosProductos() {
        if (cmbPosProductos != null) {
            cmbPosProductos.removeAllItems();
            for (Producto p : controlador.obtenerProductos()) {
                cmbPosProductos.addItem(p.getId() + " - " + p.getNombre() + " ($" + p.getPrecioBase() + ")");
            }
        }
    }

    private void actualizarComboPosExtras() {
        // Cualquier ingrediente disponible puede agregarse como extra en la orden.
        if (cmbPosExtras != null) {
            cmbPosExtras.removeAllItems();
            for (Ingrediente i : controlador.obtenerIngredientes()) {
                cmbPosExtras.addItem(i.getId() + " - " + i.getNombre());
            }
        }
    }

    private void refrescarTablaOrden() {
        modeloOrdenActual.setRowCount(0);
        for (Map.Entry<Integer, Integer> entry : ordenTemporal.entrySet()) {
            Ingrediente ingBD = controlador.obtenerIngredientes().stream().filter(i -> i.getId() == entry.getKey()).findFirst().orElse(null);
            if (ingBD != null) {
                modeloOrdenActual.addRow(new Object[]{entry.getKey(), ingBD.getNombre(), entry.getValue(), "Modificado"});
            }
        }
    }

    private void mostrarDialogoRegistroCliente(String documentoIngresado) {
        // Registro rapido desde el POS cuando la identificacion no fue encontrada.
        JPanel pnlRegistro = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JComboBox<String> cmbTipoCli = new JComboBox<>(new String[]{"Persona Natural", "Empresa"});
        JTextField txtDoc = new JTextField(documentoIngresado);
        txtDoc.setEditable(false); 
        JTextField txtNombre = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtCorreo = new JTextField();
        
        pnlRegistro.add(new JLabel("Tipo:")); pnlRegistro.add(cmbTipoCli);
        pnlRegistro.add(new JLabel("Documento:")); pnlRegistro.add(txtDoc);
        pnlRegistro.add(new JLabel("Nombre / Razón Social:")); pnlRegistro.add(txtNombre);
        pnlRegistro.add(new JLabel("Teléfono:")); pnlRegistro.add(txtTelefono);
        pnlRegistro.add(new JLabel("Correo:")); pnlRegistro.add(txtCorreo);

        int result = JOptionPane.showConfirmDialog(this, pnlRegistro, "Registrar Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
        if (result == JOptionPane.OK_OPTION) {
            boolean esEmpresa = cmbTipoCli.getSelectedIndex() == 1;
            Cliente nuevo = controlador.registrarClienteExpress(esEmpresa, txtDoc.getText(), txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText());
            
            if (nuevo != null) {
                clienteActualFacturacion = nuevo;
                lblNombreCliente.setText(" | Cliente: " + txtNombre.getText() + "   ");
                JOptionPane.showMessageDialog(this, "Cliente guardado y seleccionado exitosamente.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar en base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private JPanel crearBuscadorParaTabla(JTable tabla, DefaultTableModel modelo) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtBuscar = new JTextField(25);
        panel.add(new JLabel("🔍 Buscar:"));
        panel.add(txtBuscar);

        // Activamos el motor de filtrado de Java Swing
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Evento que reacciona cada vez que tecleas una letra
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscar.getText();
                if (texto.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // (?i) hace que ignore mayúsculas y minúsculas
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto)); 
                }
            }
        });
        return panel;
    }

    private JPanel crearPanelHistorialVentas() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefrescar = new JButton("🔄 Refrescar Historial de Ventas");
        pnlSuperior.add(btnRefrescar);

        // AÑADIDO: La columna "Detalle de Compra"
        String[] columnas = {"ID Factura", "Fecha de Venta", "Cliente / Razón Social", "Detalle de Compra", "Total Pagado"};
        modeloHistorial = new DefaultTableModel(columnas, 0);
        JTable tablaHistorial = new JTable(modeloHistorial);
        
        // AÑADIDO: Altura para que el producto y la receta quepan
        tablaHistorial.setRowHeight(50);
        
        // AÑADIDO: Ajuste de anchos para dar protagonismo al detalle
        tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaHistorial.getColumnModel().getColumn(3).setPreferredWidth(300); 

        btnRefrescar.addActionListener(e -> {
            // Se vuelve a consultar la base para reflejar las ventas mas recientes.
            modeloHistorial.setRowCount(0);
            for (String[] fila : controlador.obtenerHistorialVentas()) {
                modeloHistorial.addRow(fila);
            }
        });

        // 🔍 Puedes añadir el buscador comodín aquí también si lo deseas
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(crearBuscadorParaTabla(tablaHistorial, modeloHistorial), BorderLayout.NORTH);
        pnlCentro.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);

        panel.add(pnlSuperior, BorderLayout.NORTH);
        panel.add(pnlCentro, BorderLayout.CENTER);
        
        return panel;
    }
}