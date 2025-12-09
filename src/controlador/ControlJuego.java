package controlador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.SwingUtilities;

import modelo.Combate;
import modelo.Enemigo;
import modelo.Habilidad;
import modelo.Heroe;
import vista.VentanaInicio;

public class ControlJuego {

    private ArrayList<Heroe> heroes;
    private ArrayList<Enemigo> enemigos;

    // 🔥 Variables necesarias para guardar/cargar
    private int turnoActual = 0;
    private int indiceHeroeActual = 0;

    public ControlJuego() {
    }

    // Constructor usado al cargar partida
    public ControlJuego(ArrayList<Heroe> heroes, ArrayList<Enemigo> enemigos) {
        this.heroes = heroes;
        this.enemigos = enemigos;
    }

    public int getTurnoActual() { return turnoActual; }
    public void setTurnoActual(int turno) { this.turnoActual = turno; }

    public int getIndiceHeroeActual() { return indiceHeroeActual; }
    public void setIndiceHeroeActual(int indice) { this.indiceHeroeActual = indice; }

    public void avanzarTurno() { turnoActual++; }

    // ===================== CREAR PARTIDA BASE ==========================
    private void crearPartidaBase() {

        Heroe heroe1 = new Heroe("Héroe", 100, 30, 25, 10, 15);
        Heroe heroe2 = new Heroe("Yangus", 120, 20, 27, 12, 12);
        Heroe heroe3 = new Heroe("Jessica", 90, 50, 20, 8, 18);
        Heroe heroe4 = new Heroe("Angelo", 85, 40, 24, 9, 16);

        // Habilidades
        heroe3.agregarHabilidad(new Habilidad("Fuego", "daño", 25, 10));
        heroe3.agregarHabilidad(new Habilidad("Curar", "curación", 30, 8));
        heroe3.agregarHabilidad(new Habilidad("Veneno", "estado", 0, 6, 5, "Envenenado"));

        heroe4.agregarHabilidad(new Habilidad("Rayo Divino", "daño", 35, 12));
        heroe4.agregarHabilidad(new Habilidad("Curación Menor", "curación", 20, 6));

        // Ítems
        for (Heroe h : Arrays.asList(heroe1, heroe2, heroe3, heroe4)) {
            h.agregarItem("Hierba Sanadora");
            h.agregarItem("Campanilla Despertar");
            h.agregarItem("Hierba Pequeña");
            h.agregarItem("Poción de Defensa");
        }

        heroe1.agregarItem("Talismán del valor");
        heroe2.agregarItem("Hacha oxidada gigante");
        heroe3.agregarItem("Amuleto de maná arcano");
        heroe4.agregarItem("Bendición divina");

        heroes = new ArrayList<>();
        heroes.add(heroe1);
        heroes.add(heroe2);
        heroes.add(heroe3);
        heroes.add(heroe4);

        // Enemigos base
        Enemigo[] enemigosArr = {
                new Enemigo("Goblin", 70, 0, 20, 8, 10, "agresivo"),
                new Enemigo("Slime", 60, 0, 15, 5, 8, "agresivo"),
                new Enemigo("Dragón", 110, 20, 30, 15, 14, "defensivo"),
                new Enemigo("Esqueleto", 80, 0, 18, 9, 13, "agresivo")
        };

        Random r = new Random();
        int jefeIndex = r.nextInt(enemigosArr.length);
        Enemigo base = enemigosArr[jefeIndex];

        enemigosArr[jefeIndex] = new Enemigo(
                base.getNombre(),
                base.getVidaHp(),
                base.getMagiaMp(),
                base.getAtaque(),
                base.getDefensa(),
                base.getVelocidad(),
                base.getTipo(),
                true
        );

        enemigos = new ArrayList<>(Arrays.asList(enemigosArr));

        turnoActual = 0;
        indiceHeroeActual = 0;
    }

    public void iniciarConsola() {
        crearPartidaBase();
        Combate combate = new Combate(heroes, enemigos);
        combate.iniciar();
    }

    // ===================== GUI ==========================

    public void iniciarGUI() {
        crearPartidaBase();
        SwingUtilities.invokeLater(() -> new VentanaInicio(this));
    }

    public ArrayList<Heroe> getHeroes() { return heroes; }
    public ArrayList<Enemigo> getEnemigos() { return enemigos; }

    public void reiniciarPartida() {
        heroes = new ArrayList<>();
        enemigos = new ArrayList<>();
        crearPartidaBase();
    }
}
