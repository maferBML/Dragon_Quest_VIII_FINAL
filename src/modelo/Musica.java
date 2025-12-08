package modelo;

import javax.sound.sampled.*;
import java.net.URL;

public class Musica {

    private Clip clip;

    public void reproducirLoop(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);

            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // música infinita
            clip.start();
        } catch (Exception e) {
            System.out.println("Error al cargar sonido: " + e.getMessage());
        }
    }

    public void parar() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}

