package modelo;

public class Empresa extends Cliente {
    private String nit;
    private String razonSocial;

    public Empresa(int id, String telefono, String correo, String nit, String razonSocial) {
        super(id, telefono, correo);
        this.nit = nit; 
        this.razonSocial = razonSocial;
    }
    
    public String getNit() { return nit; }
    public String getRazonSocial() { return razonSocial; }

    @Override
    public String obtenerDatosFacturacion() {
        return "NIT: " + nit + " | Razón Social: " + razonSocial;
    }
}