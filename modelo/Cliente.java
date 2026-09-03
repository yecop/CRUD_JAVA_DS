package modelo;
import java.io.Serializable;

public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nombre;
    private String telefono;

    public Cliente(int id, String nombre, String telefono) {
        this.id = id; this.nombre = nombre; this.telefono = telefono;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
}