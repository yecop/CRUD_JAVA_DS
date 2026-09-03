package vista;

import controlador.SistemaControlador;
import modelo.Cliente;
import modelo.Producto;
import modelo.Hamburguesa;
import modelo.Pizza;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    
    private SistemaControlador controlador; 
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloClientes;

    private JTextField txtNombreProd, txtPrecioProd, txtIngredientes;
    private JComboBox<String> cmbTipo, cmbCarne, cmbTamano, cmbMasa;
    private JCheckBox chkDoble;
    private JTextField txtNombreCli, txtTelefonoCli;

    public VentanaPrincipal() {
        this.controlador = new SistemaControlador(); 
        setTitle("Sistema de Gestión - CRUD Completo (SOLID)");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("🍕 Gestión de Productos", crearPanelProductos());
        pestañas.addTab("👥 Gestión de Clientes", crearPanelClientes());
        
        add(pestañas);
    }

    // ================= PRODUCTOS =================

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel pnlFormulario = new JPanel();
        pnlFormulario.setLayout(new BoxLayout(pnlFormulario, BoxLayout.Y_AXIS));
        pnlFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbTipo = new JComboBox<>(new String[]{"Hamburguesa", "Pizza"});
        pnlTipo.add(new JLabel("Tipo:")); pnlTipo.add(cmbTipo);

        JPanel pnlComun = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtNombreProd = new JTextField(12);
        txtPrecioProd = new JTextField(6);
        txtIngredientes = new JTextField(20); 
        pnlComun.add(new JLabel("Nombre:")); pnlComun.add(txtNombreProd);
        pnlComun.add(new JLabel("Precio ($):")); pnlComun.add(txtPrecioProd);
        pnlComun.add(new JLabel("Ingredientes extra (separados por coma):")); pnlComun.add(txtIngredientes);

        JPanel pnlDinamico = new JPanel(new CardLayout());
        
        JPanel pnlHamburguesa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbCarne = new JComboBox<>(new String[]{"Res", "Pollo", "Vegana"});
        chkDoble = new JCheckBox("Carne Doble");
        pnlHamburguesa.add(new JLabel("Tipo Carne:")); pnlHamburguesa.add(cmbCarne);
        pnlHamburguesa.add(chkDoble);
        
        JPanel pnlPizza = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbTamano = new JComboBox<>(new String[]{"Personal", "Mediana", "Familiar"});
        cmbMasa = new JComboBox<>(new String[]{"Delgada", "Tradicional", "Borde Queso"});
        pnlPizza.add(new JLabel("Tamaño:")); pnlPizza.add(cmbTamano);
        pnlPizza.add(new JLabel("Masa:")); pnlPizza.add(cmbMasa);

        pnlDinamico.add(pnlHamburguesa, "Hamburguesa");
        pnlDinamico.add(pnlPizza, "Pizza");

        cmbTipo.addActionListener(e -> {
            CardLayout cl = (CardLayout) (pnlDinamico.getLayout());
            cl.show(pnlDinamico, (String) cmbTipo.getSelectedItem());
        });

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar Seleccionado");
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        pnlBotones.add(btnAgregar); pnlBotones.add(btnActualizar); pnlBotones.add(btnEliminar);

        pnlFormulario.add(pnlTipo); pnlFormulario.add(pnlComun); pnlFormulario.add(pnlDinamico); pnlFormulario.add(pnlBotones);

        String[] columnas = {"ID", "Categoría", "Nombre", "Precio", "Detalles & Ingredientes"};
        modeloProductos = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modeloProductos);
        actualizarTablaProductos();

        // EVENTO: Cargar datos al hacer clic en la tabla
        tabla.getSelectionModel().addListSelectionListener(event -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0 && !event.getValueIsAdjusting()) {
                Producto p = controlador.obtenerProductos().get(fila);
                txtNombreProd.setText(p.getNombre());
                txtPrecioProd.setText(String.valueOf(p.getPrecioBase()));
                txtIngredientes.setText(String.join(", ", p.getIngredientes()));
                
                if (p instanceof Hamburguesa) {
                    cmbTipo.setSelectedItem("Hamburguesa");
                    Hamburguesa h = (Hamburguesa) p;
                    cmbCarne.setSelectedItem(h.getTipoCarne());
                    chkDoble.setSelected(h.isEsDoble());
                } else if (p instanceof Pizza) {
                    cmbTipo.setSelectedItem("Pizza");
                    Pizza pz = (Pizza) p;
                    cmbTamano.setSelectedItem(pz.getTamaño());
                    cmbMasa.setSelectedItem(pz.getTipoMasa());
                }
            }
        });

        btnAgregar.addActionListener(e -> {
            try {
                enviarDatosProducto(true, tabla.getSelectedRow());
                limpiarCamposProducto();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error en los datos"); }
        });

        btnActualizar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                try {
                    enviarDatosProducto(false, fila);
                    limpiarCamposProducto();
                    tabla.clearSelection();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error en los datos"); }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para actualizar.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                controlador.eliminarProducto(fila);
                actualizarTablaProductos();
                limpiarCamposProducto();
            }
        });

        panel.add(pnlFormulario, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private void enviarDatosProducto(boolean esNuevo, int fila) {
        String tipo = (String) cmbTipo.getSelectedItem();
        String nombre = txtNombreProd.getText();
        double precio = Double.parseDouble(txtPrecioProd.getText());
        String ingredientes = txtIngredientes.getText();
        String extra1 = ""; Object extra2 = null;

        if (tipo.equals("Hamburguesa")) {
            extra1 = (String) cmbCarne.getSelectedItem();
            extra2 = chkDoble.isSelected();
        } else if (tipo.equals("Pizza")) {
            extra1 = (String) cmbTamano.getSelectedItem();
            extra2 = (String) cmbMasa.getSelectedItem();
        }

        if (esNuevo) {
            controlador.registrarProducto(tipo, nombre, precio, ingredientes, extra1, extra2);
        } else {
            int idOriginal = controlador.obtenerProductos().get(fila).getId();
            controlador.actualizarProducto(fila, idOriginal, tipo, nombre, precio, ingredientes, extra1, extra2);
        }
        actualizarTablaProductos();
    }

    private void limpiarCamposProducto() {
        txtNombreProd.setText(""); txtPrecioProd.setText(""); txtIngredientes.setText("");
    }

    private void actualizarTablaProductos() {
        modeloProductos.setRowCount(0);
        for (Producto p : controlador.obtenerProductos()) {
            String detalles = p.obtenerDetallesPreparacion();
            if (p.getIngredientes() != null && !p.getIngredientes().isEmpty()) {
                detalles += " | Extra: " + String.join(", ", p.getIngredientes());
            }
            modeloProductos.addRow(new Object[]{ p.getId(), p.getCategoria(), p.getNombre(), "$" + p.getPrecioBase(), detalles });
        }
    }

    // ================= CLIENTES =================

    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel pnlFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtNombreCli = new JTextField(15);
        txtTelefonoCli = new JTextField(10);
        
        pnlFormulario.add(new JLabel("Nombre Completo:")); pnlFormulario.add(txtNombreCli);
        pnlFormulario.add(new JLabel("Teléfono:")); pnlFormulario.add(txtTelefonoCli);
        
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar Seleccionado");
        JButton btnEliminar = new JButton("Eliminar");
        pnlFormulario.add(btnAgregar); pnlFormulario.add(btnActualizar); pnlFormulario.add(btnEliminar);
        
        String[] columnas = {"ID", "Nombre", "Teléfono"};
        modeloClientes = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modeloClientes);
        actualizarTablaClientes();
        
        // EVENTO: Cargar datos al hacer clic en la tabla de clientes
        tabla.getSelectionModel().addListSelectionListener(event -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0 && !event.getValueIsAdjusting()) {
                Cliente c = controlador.obtenerClientes().get(fila);
                txtNombreCli.setText(c.getNombre());
                txtTelefonoCli.setText(c.getTelefono());
            }
        });

        btnAgregar.addActionListener(e -> {
            if (!txtNombreCli.getText().isEmpty() && !txtTelefonoCli.getText().isEmpty()) {
                controlador.registrarCliente(txtNombreCli.getText(), txtTelefonoCli.getText());
                actualizarTablaClientes();
                txtNombreCli.setText(""); txtTelefonoCli.setText("");
            }
        });

        btnActualizar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int idOriginal = controlador.obtenerClientes().get(fila).getId();
                controlador.actualizarCliente(fila, idOriginal, txtNombreCli.getText(), txtTelefonoCli.getText());
                actualizarTablaClientes();
                txtNombreCli.setText(""); txtTelefonoCli.setText("");
                tabla.clearSelection();
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                controlador.eliminarCliente(fila);
                actualizarTablaClientes();
                txtNombreCli.setText(""); txtTelefonoCli.setText("");
            }
        });

        panel.add(pnlFormulario, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private void actualizarTablaClientes() {
        modeloClientes.setRowCount(0);
        for (Cliente c : controlador.obtenerClientes()) {
            modeloClientes.addRow(new Object[]{c.getId(), c.getNombre(), c.getTelefono()});
        }
    }
}