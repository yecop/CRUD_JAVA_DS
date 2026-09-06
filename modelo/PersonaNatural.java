package modelo;

public class PersonaNatural extends Cliente {
    private String cedula;
    private String nombreCompleto;

    public PersonaNatural(int id, String telefono, String correo, String cedula, String nombreCompleto) {
        super(id, telefono, correo);
        this.cedula = cedula; 
        this.nombreCompleto = nombreCompleto;
    }
    public String getCedula() { return cedula; }
    public String getNombreCompleto() { return nombreCompleto; }

    @Override
    public String obtenerDatosFacturacion() {
        return "CC: " + cedula + " | Nombre: " + nombreCompleto;
    }
}