package modelo;

import javax.sound.sampled.*;
import java.net.URL;

public class Musica {

    private Clip clip;

    public void reproducirLoop(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);

            // Si ya hay un clip sonando, lo cerramos antes de abrir uno nuevo
            if (clip != null && clip.isOpen()) {
                clip.stop();
                clip.close();
            }

            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (Exception e) {
            System.out.println("Error al cargar sonido: " + e.getMessage());
        }
    }

    public void parar() {
        try {
            if (clip != null) {
                clip.stop();
                clip.flush();  // 🔥 Limpia audio en cola
                clip.close();  // 🔥 Libera recursos
            }
        } catch (Exception e) {
            System.out.println("Error al detener sonido: " + e.getMessage());
        }
    }
}
