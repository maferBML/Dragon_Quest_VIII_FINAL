package modelo;

import javax.sound.sampled.*;
import java.net.URL;

public class Musica {

    private Clip clip;

    public void reproducirLoop(String ruta) {
        try {
            // Si ya había audio sonando, lo apagamos completamente
            if (clip != null) {
                if (clip.isRunning()) clip.stop();
                clip.flush();
                clip.close();
                clip = null; // MUY IMPORTANTE
            }

            URL url = getClass().getResource(ruta);
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);

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
