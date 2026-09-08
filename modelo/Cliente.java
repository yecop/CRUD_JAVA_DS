package modelo;
import java.io.Serializable;

// Clase base para los clientes que pueden realizar compras en el sistema.
// Las subclases definen los datos propios de una persona o una empresa.
public abstract class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String telefono;
    protected String correo; 

    public Cliente(int id, String telefono, String correo) {
        this.id = id; this.telefono = telefono; this.correo = correo;
    }

    // Cada tipo de cliente presenta sus datos con una identificacion diferente.
    public abstract String obtenerDatosFacturacion();

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // MySQL asigna el id
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}