package modelo;
import java.io.Serializable;

public abstract class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String telefono;
    protected String correo; 

    public Cliente(int id, String telefono, String correo) {
        this.id = id; this.telefono = telefono; this.correo = correo;
    }

    public abstract String obtenerDatosFacturacion();

    // Getters y Setters necesarios
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // <-- Vital para que MySQL asigne el ID
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}