package modelo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class HistorialBatallas {

    private static final String RUTA = "historial_batallas.txt";

    public static void registrar(String resultado,
                                 List<? extends Personaje> heroes,
                                 List<? extends Personaje> enemigos) {

        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA, true))) {

            pw.println("==============================================");
            pw.println("FECHA: " + LocalDateTime.now());
            pw.println("RESULTADO: " + resultado);
            pw.println();
            pw.println("HÉROES:");
            for (Personaje h : heroes) {
                pw.println("- " + h.getNombre()
                        + " | HP: " + h.getVidaHp()
                        + " | Estado: " + (h.estaVivo() ? "Vivo" : "Muerto"));
            }
            pw.println();
            pw.println("ENEMIGOS:");
            for (Personaje e : enemigos) {
                pw.println("- " + e.getNombre()
                        + " | HP: " + e.getVidaHp()
                        + " | Estado: " + (e.estaVivo() ? "Vivo" : "Muerto"));
            }
            pw.println("==============================================");
            pw.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
