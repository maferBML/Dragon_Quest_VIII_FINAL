package utils;

import controlador.ControlJuego;
import modelo.Enemigo;
import modelo.Heroe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

public class GuardarPartida {

    public static final String RUTA_CARPETA = "partidas";

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static class PartidaCargada {
        private final ControlJuego controlador;
        private final int turno;

        public PartidaCargada(ControlJuego controlador, int turno) {
            this.controlador = controlador;
            this.turno = turno;
        }

        public ControlJuego getControlador() { return controlador; }
        public int getTurno() { return turno; }
    }

    public static void guardar(ControlJuego ctrl, int turno, String nombreArchivo) throws Exception {

        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists()) carpeta.mkdirs();

        if (!nombreArchivo.toLowerCase().endsWith(".json"))
            nombreArchivo += ".json";

        File archivo = new File(carpeta, nombreArchivo);

        DatosPartida datos = new DatosPartida(
                new ArrayList<>(ctrl.getHeroes()),
                new ArrayList<>(ctrl.getEnemigos()),
                turno,
                ctrl.getIndiceHeroeActual() // ❤️ héroe en turno
        );

        try (Writer writer = new FileWriter(archivo)) {
            gson.toJson(datos, writer);
        }
    }

    public static PartidaCargada cargar(String nombreArchivo) {
        try {
            if (!nombreArchivo.toLowerCase().endsWith(".json"))
                nombreArchivo += ".json";

            File archivo = new File(RUTA_CARPETA, nombreArchivo);
            if (!archivo.exists()) return null;

            try (Reader reader = new FileReader(archivo)) {
                DatosPartida datos = gson.fromJson(reader, DatosPartida.class);

                ControlJuego ctrl = new ControlJuego(
                        datos.getHeroes(),
                        datos.getEnemigos()
                );

                ctrl.setIndiceHeroeActual(datos.getIndiceHeroeActual());
                ctrl.setTurnoActual(datos.getTurno());

                return new PartidaCargada(ctrl, datos.getTurno());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
