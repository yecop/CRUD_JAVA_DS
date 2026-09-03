package persistencia;
import java.io.*;

public class GestorArchivos {
    private static final String ARCHIVO = "datos_comidas.dat";

    public static void guardar(BaseDeDatos bd) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            oos.writeObject(bd);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static BaseDeDatos cargar() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO))) {
            return (BaseDeDatos) ois.readObject();
        } catch (Exception e) { return new BaseDeDatos(); }
    }
}