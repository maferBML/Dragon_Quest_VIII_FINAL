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

    public ControlJuego() {
        // no hace nada por ahora
    }

    // ================== CREAR PARTIDA (MISMA LÓGICA QUE TENÍAS) ==================

    private void crearPartidaBase() {
        // --- Crear héroes ---
        Heroe heroe1 = new Heroe("Héroe", 100, 30, 25, 10, 15);
        Heroe heroe2 = new Heroe("Yangus", 120, 20, 27, 12, 12);
        Heroe heroe3 = new Heroe("Jessica", 90, 50, 20, 8, 18);
        Heroe heroe4 = new Heroe("Angelo", 85, 40, 24, 9, 16);

        // Habilidades de Jessica
        heroe3.agregarHabilidad(new Habilidad("Fuego", "daño", 25, 10));
        heroe3.agregarHabilidad(new Habilidad("Curar", "curación", 30, 8));
        heroe3.agregarHabilidad(new Habilidad("Veneno", "estado", 0, 6, 5, "Envenenado"));

        // Habilidades de Angelo
        heroe4.agregarHabilidad(new Habilidad("Rayo Divino", "daño", 35, 12));
        heroe4.agregarHabilidad(new Habilidad("Curación Menor", "curación", 20, 6));

        heroes = new ArrayList<>();
        heroes.add(heroe1);
        heroes.add(heroe2);
        heroes.add(heroe3);
        heroes.add(heroe4);

        // --- Crear enemigos base ---
        Enemigo[] enemigosArr = {
            new Enemigo("Goblin", 70, 0, 20, 8, 10, "agresivo"),
            new Enemigo("Slime", 60, 0, 15, 5, 8, "agresivo"),
            new Enemigo("Dragón", 110, 20, 30, 15, 14, "defensivo"),
            new Enemigo("Esqueleto", 80, 0, 18, 9, 13, "agresivo")
        };

        // --- Elegir mini-jefe aleatorio (MISMA IDEA QUE ANTES, SIN PRINTS) ---
        Random random = new Random();
        int indiceMiniJefe = random.nextInt(enemigosArr.length);

        Enemigo elegido = enemigosArr[indiceMiniJefe];
        enemigosArr[indiceMiniJefe] = new Enemigo(
            elegido.getNombre(),
            elegido.getVidaHp(),
            elegido.getMagiaMp(),
            elegido.getAtaque(),
            elegido.getDefensa(),
            elegido.getVelocidad(),
            elegido.getTipo(),
            true   // es miniJefe
        );

        enemigos = new ArrayList<>(Arrays.asList(enemigosArr));
    }

    // ================== MODO CONSOLA ==================

    public void iniciarConsola() {
        crearPartidaBase();

        // Mensaje de mini jefe SOLO por consola
        for (Enemigo e : enemigos) {
            if (e.esMiniJefe()) {
                System.out.println("\n¡Un " + e.getNombre().toUpperCase() +
                        " ha aparecido como JEFE (Cagaste)!");
                System.out.println("HP aumentado: " + e.getVidaHp());
                System.out.println("Ataque aumentado: " + e.getAtaque());
                System.out.println("Defensa aumentada: " + e.getDefensa());
                System.out.println("==============================================\n");
                break;
            }
        }

        Combate combate = new Combate(heroes, enemigos);
        combate.iniciar();
    }

    // ================== MODO GUI ==================

    public void iniciarGUI() {
        crearPartidaBase();
        SwingUtilities.invokeLater(() -> new VentanaInicio(this));
    }

    // ================== GETTERS PARA LA VISTA ==================

    public ArrayList<Heroe> getHeroes() {
        return heroes;
    }

    public ArrayList<Enemigo> getEnemigos() {
        return enemigos;
    }

    public void reiniciarPartida() {
        crearPartidaBase();
}

}