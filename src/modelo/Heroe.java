package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class Heroe extends Personaje {

    private ArrayList<Habilidad> habilidades = new ArrayList<>();

    public Heroe(String nombre, int vidaHp, int magiaMp, int ataque, int defensa, int velocidad) {
        super(nombre, vidaHp, magiaMp, ataque, defensa, velocidad);
    }

    public void agregarHabilidad(Habilidad h) {
        habilidades.add(h);
    }

    // ================== LÓGICA ORIGINAL (CONSOLA) ==================

    @Override
    public void atacar(Personaje enemigo) {
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            System.out.println(getNombre() + " está paralizado y no puede atacar.");
            return;
        }

        int danio = this.getAtaque() - enemigo.getDefensa();
        if (danio < 0) danio = 0;

        enemigo.setVidaHp(enemigo.getVidaHp() - danio);
        if (enemigo.getVidaHp() <= 0) {
            enemigo.setVive(false);
            enemigo.setVidaHp(0);
            System.out.println(enemigo.getNombre() + " ha sido derrotado.");
        } else {
            System.out.println(this.getNombre() + " ataca a " + enemigo.getNombre() + " causando " + danio + " puntos de daño.");
        }
    }

    public void defender() {
        System.out.println(this.getNombre() + " se anda preparando para defenderse del próximo ataque. Defensa aumentada temporalmente.");
        setDefensa(getDefensa() + 5);
    }

    public void curar(int cantidad) {
        this.setVidaHp(this.getVidaHp() + cantidad);
        System.out.println(this.getNombre() + " se cura " + cantidad + " puntos de vida. Vida actual: " + this.getVidaHp());
    }

    // versión de consola con Scanner
    public void usarHabilidad(ArrayList<Heroe> heroes, List<Enemigo> enemigos) {
        if (habilidades.isEmpty()) {
            System.out.println(getNombre() + " no tiene habilidades.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        System.out.println("\nHabilidades disponibles:");
        for (int i = 0; i < habilidades.size(); i++) {
            Habilidad h = habilidades.get(i);
            System.out.println((i + 1) + ". " + h.getNombre() + " (MP: " + h.getCosteMp() + ")");
        }

        System.out.print("Elige una habilidad: ");
        int opcion = sc.nextInt();

        if (opcion < 1 || opcion > habilidades.size()) {
            System.out.println("\nTe inventaste esa opcion.");
            return;
        }

        Habilidad h = habilidades.get(opcion - 1);

        if (getMagiaMp() < h.getCosteMp()) {
            System.out.println("\nNo tienes suficiente MP para usar " + h.getNombre() + ".");
            return;
        }

        setMagiaMp(getMagiaMp() - h.getCosteMp());
        System.out.println(getNombre() + " usa " + h.getNombre() + "!");

        switch (h.getTipo().toLowerCase()) {
            case "daño" -> {
                Enemigo enemigo = elegirEnemigo(enemigos);
                if (enemigo == null) return;
                enemigo.setVidaHp(enemigo.getVidaHp() - h.getPoder());
                System.out.println(enemigo.getNombre() + " recibe " + h.getPoder() + " puntos de daño mágico.");
                if (enemigo.getVidaHp() <= 0) enemigo.setVive(false);
            }

            case "estado" -> {
                Enemigo enemigo = elegirEnemigo(enemigos);
                if (enemigo == null) return;
                enemigo.setEstado(new Estado(h.getEstado(), h.getDuracion()));
                System.out.println(enemigo.getNombre() + " ahora está " + h.getEstado() + ".");
            }

            case "curación" -> {
                System.out.println("\n¿A qué compañero quieres curar?");
                for (int i = 0; i < heroes.size(); i++) {
                    Heroe aliado = heroes.get(i);
                    if (aliado.estaVivo()) {
                        System.out.println((i + 1) + ". " + aliado.getNombre() + " (HP: " + aliado.getVidaHp() + ")");
                    }
                }
                System.out.print("Elige número: ");
                int eleccion = sc.nextInt();

                if (eleccion < 1 || eleccion > heroes.size() || !heroes.get(eleccion - 1).estaVivo()) {
                    System.out.println("\nY esa opción de dónde salió? Pierdes el turno por inventarte cosas.");
                    return;
                }

                Heroe aliadoCurado = heroes.get(eleccion - 1);
                aliadoCurado.setVidaHp(aliadoCurado.getVidaHp() + h.getPoder());
                System.out.println(aliadoCurado.getNombre() + " recupera " + h.getPoder() + " puntos de vida.");
            }

            default -> System.out.println("\n¿Qué es esa habilidad? Para próximas actualizaciones te la ponemos.");
        }
    }

    private Enemigo elegirEnemigo(List<Enemigo> enemigos) {
        Scanner sc = new Scanner(System.in);
        List<Enemigo> vivos = new ArrayList<>();
        for (Enemigo e : enemigos) if (e.estaVivo()) vivos.add(e);

        if (vivos.isEmpty()) return null;

        System.out.println("\nElige un enemigo:");
        for (int i = 0; i < vivos.size(); i++) {
            Enemigo e = vivos.get(i);
            System.out.println((i + 1) + ". " + e.getNombre() + " (HP: " + e.getVidaHp() + ")");
        }

        System.out.print("Número del enemigo: ");
        int eleccion = sc.nextInt();

        if (eleccion < 1 || eleccion > vivos.size()) {
            System.out.println("Opción inválida, se elige uno al azar.");
            return vivos.get(new Random().nextInt(vivos.size()));
        }

        return vivos.get(eleccion - 1);
    }

    // ================== MÉTODOS EXTRA PARA LA GUI ==================

    public ArrayList<Habilidad> getHabilidades() {
        return habilidades;
    }

    /** Igual que defender(), pero devuelve el texto para la GUI. */
    public String defenderTexto() {
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            return getNombre() + " está paralizado y no puede defenderse.\n";
        }
        setDefensa(getDefensa() + 5);
        return getNombre() + " se prepara para defenderse del próximo ataque. Defensa aumentada temporalmente.\n";
    }

    /** Ataque básico que devuelve texto sin usar System.out. */
    public String atacarTexto(Enemigo enemigo) {
        StringBuilder sb = new StringBuilder();

        if (!estaVivo()) {
            sb.append(getNombre()).append(" ya no puede atacar.\n");
            return sb.toString();
        }

        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            sb.append(getNombre()).append(" está paralizado y no puede atacar.\n");
            return sb.toString();
        }

        if (enemigo == null || !enemigo.estaVivo()) {
            sb.append("Ese enemigo ya está derrotado.\n");
            return sb.toString();
        }

        int danio = this.getAtaque() - enemigo.getDefensa();
        if (danio < 0) danio = 0;

        enemigo.setVidaHp(enemigo.getVidaHp() - danio);

        sb.append(getNombre()).append(" ataca a ")
          .append(enemigo.getNombre())
          .append(" causando ").append(danio).append(" puntos de daño.\n");

        if (enemigo.getVidaHp() <= 0) {
            enemigo.setVive(false);
            enemigo.setVidaHp(0);
            sb.append(enemigo.getNombre()).append(" ha sido derrotado.\n");
        }

        return sb.toString();
    }

    /**
     * Versión sin Scanner para la GUI.
     * - h: habilidad elegida.
     * - objetivoAliado: se usa si es curación.
     * - objetivoEnemigo: se usa si es daño/estado.
     */
    public String usarHabilidadGUI(Habilidad h,
                                   ArrayList<Heroe> heroes,
                                   Heroe objetivoAliado,
                                   Enemigo objetivoEnemigo) {

        StringBuilder sb = new StringBuilder();

        if (!estaVivo()) {
            sb.append(getNombre()).append(" ya no puede actuar.\n");
            return sb.toString();
        }

        if (getMagiaMp() < h.getCosteMp()) {
            sb.append("No tienes suficiente MP para usar ").append(h.getNombre()).append(".\n");
            return sb.toString();
        }

        setMagiaMp(getMagiaMp() - h.getCosteMp());
        sb.append(getNombre()).append(" usa ").append(h.getNombre()).append("!\n");

        switch (h.getTipo().toLowerCase()) {
            case "daño" -> {
                if (objetivoEnemigo == null || !objetivoEnemigo.estaVivo()) {
                    sb.append("Ese enemigo ya está derrotado.\n");
                    break;
                }
                objetivoEnemigo.setVidaHp(objetivoEnemigo.getVidaHp() - h.getPoder());
                sb.append(objetivoEnemigo.getNombre())
                  .append(" recibe ").append(h.getPoder())
                  .append(" puntos de daño mágico.\n");
                if (objetivoEnemigo.getVidaHp() <= 0) {
                    objetivoEnemigo.setVive(false);
                    objetivoEnemigo.setVidaHp(0);
                    sb.append(objetivoEnemigo.getNombre()).append(" ha sido derrotado.\n");
                }
            }

            case "estado" -> {
                if (objetivoEnemigo == null || !objetivoEnemigo.estaVivo()) {
                    sb.append("Ese enemigo ya está derrotado.\n");
                    break;
                }
                objetivoEnemigo.setEstado(new Estado(h.getEstado(), h.getDuracion()));
                sb.append(objetivoEnemigo.getNombre())
                  .append(" ahora está ").append(h.getEstado()).append(".\n");
            }

            case "curación" -> {
                if (objetivoAliado == null || !objetivoAliado.estaVivo()) {
                    sb.append("No hay aliado válido para curar.\n");
                    break;
                }
                objetivoAliado.setVidaHp(objetivoAliado.getVidaHp() + h.getPoder());
                sb.append(objetivoAliado.getNombre())
                  .append(" recupera ").append(h.getPoder())
                  .append(" puntos de vida.\n");
            }

            default -> sb.append("La habilidad no está bien configurada.\n");
        }

        return sb.toString();
    }
}