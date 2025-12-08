package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

import modelo.excepciones.DefenderException;
import modelo.excepciones.ObjetivoInvalidoException;
import modelo.excepciones.HabilidadSinMPException;
import modelo.excepciones.AccionNoPermitidaException;

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

        // Si el enemigo está defendiendo, recibe solo el 30% del daño
        if (enemigo.isProtegido()) {
            danio = (int) (danio * 0.30);
            System.out.println(enemigo.getNombre() + " reduce el daño gracias a su postura defensiva.");
        }

        enemigo.setVidaHp(enemigo.getVidaHp() - danio);
        if (enemigo.getVidaHp() <= 0) {
            enemigo.setVive(false);
            enemigo.setVidaHp(0);
            System.out.println(enemigo.getNombre() + " ha sido derrotado.");
        } else {
            System.out.println(this.getNombre() + " ataca a " + enemigo.getNombre()
                    + " causando " + danio + " puntos de daño.");
        }
    }

    public void defender() throws DefenderException {
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            throw new DefenderException(getNombre() + " está paralizado y no puede defenderse.");
        }

        if (isProtegido()) {
            throw new DefenderException(getNombre()
                    + " ya está en postura defensiva. No puedes defender dos veces seguidas.");
        }

        setProtegido(true);
        System.out.println(this.getNombre()
                + " adopta postura defensiva. Recibirá menos daño hasta su próximo turno.");
    }

    public void curar(int cantidad) {
        this.setVidaHp(this.getVidaHp() + cantidad);
        System.out.println(this.getNombre() + " se cura " + cantidad
                + " puntos de vida. Vida actual: " + this.getVidaHp());
    }

    // ====== versión de consola con Scanner (sin excepciones checked) ======
    public void usarHabilidad(ArrayList<Heroe> heroes, List<Enemigo> enemigos) {
        if (habilidades.isEmpty()) {
            System.out.println(getNombre() + " no tiene habilidades.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        System.out.println("\nHabilidades disponibles:");
        for (int i = 0; i < habilidades.size(); i++) {
            Habilidad h = habilidades.get(i);
            System.out.println((i + 1) + ". " + h.getNombre()
                    + " (MP: " + h.getCosteMp() + ")");
        }

        System.out.print("Elige una habilidad: ");
        int opcion = sc.nextInt();

        if (opcion < 1 || opcion > habilidades.size()) {
            System.out.println("\nTe inventaste esa opción.");
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
                System.out.println(enemigo.getNombre() + " recibe "
                        + h.getPoder() + " puntos de daño mágico.");
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
                        System.out.println((i + 1) + ". " + aliado.getNombre()
                                + " (HP: " + aliado.getVidaHp() + ")");
                    }
                }
                System.out.print("Elige número: ");
                int eleccion = sc.nextInt();

                if (eleccion < 1 || eleccion > heroes.size()
                        || !heroes.get(eleccion - 1).estaVivo()) {
                    System.out.println("\nY esa opción de dónde salió?"
                            + " Pierdes el turno por inventarte cosas.");
                    return;
                }

                Heroe aliadoCurado = heroes.get(eleccion - 1);
                aliadoCurado.setVidaHp(aliadoCurado.getVidaHp() + h.getPoder());
                System.out.println(aliadoCurado.getNombre() + " recupera "
                        + h.getPoder() + " puntos de vida.");
            }
            default -> System.out.println("\n¿Qué es esa habilidad? "
                    + "Para próximas actualizaciones te la ponemos.");
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
            System.out.println((i + 1) + ". " + e.getNombre()
                    + " (HP: " + e.getVidaHp() + ")");
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

    public String defenderTexto() throws DefenderException {
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            throw new DefenderException(getNombre()
                    + " está paralizado y no puede defenderse.\n");
        }

        if (isProtegido()) {
            throw new DefenderException(getNombre()
                    + " ya está en postura defensiva. "
                    + "No puedes defender dos veces seguidas.\n");
        }

        setProtegido(true);
        return getNombre()
                + " adopta postura defensiva. Recibirá menos daño hasta su próximo turno.\n";
    }

    /** Ataque básico que devuelve texto sin usar System.out. */
    public String atacarTexto(Enemigo enemigo)
            throws ObjetivoInvalidoException {

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
            throw new ObjetivoInvalidoException("Ese enemigo no es un objetivo válido.\n");
        }

        int danio = this.getAtaque() - enemigo.getDefensa();
        if (danio < 0) danio = 0;

        if (enemigo.isProtegido()) {
            danio = (int) (danio * 0.30);
            sb.append(enemigo.getNombre())
              .append(" reduce el daño gracias a su postura defensiva.\n");
        }

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
                                   Enemigo objetivoEnemigo)
            throws HabilidadSinMPException, ObjetivoInvalidoException,
                   AccionNoPermitidaException {

        StringBuilder sb = new StringBuilder();

        if (!estaVivo()) {
            sb.append(getNombre()).append(" ya no puede actuar.\n");
            return sb.toString();
        }

        if (getMagiaMp() < h.getCosteMp()) {
            throw new HabilidadSinMPException(
                    "No tienes suficiente MP para usar " + h.getNombre() + ".\n");
        }
    
        setMagiaMp(getMagiaMp() - h.getCosteMp());

        String tipo = h.getTipo().toLowerCase();

        switch (tipo) {
            case "daño" -> {
                if (objetivoEnemigo == null || !objetivoEnemigo.estaVivo()) {
                    throw new ObjetivoInvalidoException("No se puede atacar ese enemigo.\n");
                }

                objetivoEnemigo.setVidaHp(objetivoEnemigo.getVidaHp() - h.getPoder());
                sb.append(getNombre())
                  .append(" usa ").append(h.getNombre())
                  .append(" y causa ").append(h.getPoder())
                  .append(" puntos de daño.\n");

                if (objetivoEnemigo.getVidaHp() <= 0) {
                    objetivoEnemigo.setVive(false);
                    objetivoEnemigo.setVidaHp(0);
                    sb.append(objetivoEnemigo.getNombre()).append(" ha sido derrotado.\n");
                }
            }
            case "estado" -> {
                if (objetivoEnemigo == null || !objetivoEnemigo.estaVivo()) {
                    throw new ObjetivoInvalidoException(
                            "No se puede aplicar estado a ese enemigo.\n");
                }

                objetivoEnemigo.setEstado(new Estado(h.getEstado(), h.getDuracion()));
                sb.append(getNombre())
                  .append(" aplica ").append(h.getEstado())
                  .append(" a ").append(objetivoEnemigo.getNombre())
                  .append(".\n");
            }
            case "curación" -> {
                if (objetivoAliado == null || !objetivoAliado.estaVivo()) {
                    throw new AccionNoPermitidaException(
                            "No puedes curar a ese objetivo.\n");
                }

                objetivoAliado.setVidaHp(objetivoAliado.getVidaHp() + h.getPoder());
                sb.append(getNombre())
                  .append(" cura a ").append(objetivoAliado.getNombre())
                  .append(" por ").append(h.getPoder()).append(" HP.\n");
            }
            default -> throw new AccionNoPermitidaException(
                    "Tipo de habilidad desconocido.\n");
        }

        return sb.toString();
    }
}
