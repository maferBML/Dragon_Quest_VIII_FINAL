package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Random;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import modelo.excepciones.DefenderException;
import modelo.excepciones.ObjetivoInvalidoException;
import modelo.excepciones.HabilidadSinMPException;
import modelo.excepciones.AccionNoPermitidaException;

public class Heroe extends Personaje {

    private HashMap<String, Integer> inventario = new HashMap<>();

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

    public HashMap<String, Integer> getInventario() {
        return inventario;
    }

    public void agregarItem(String nombreItem) {
        if (inventario.size() < 5 || inventario.containsKey(nombreItem)) {
            inventario.put(nombreItem, inventario.getOrDefault(nombreItem, 0) + 1);
        } else {
            System.out.println("Inventario lleno (máximo 5 objetos).");
        }
    }

    public String usarItem(String nombreItem, Heroe objetivo, ArrayList<Heroe> todosLosHeroes) {

        if (!inventario.containsKey(nombreItem)) {
            return "No tienes ese objeto.";
        }

        // Reducir cantidad
        int cant = inventario.get(nombreItem);
        if (cant <= 1) inventario.remove(nombreItem);
        else inventario.put(nombreItem, cant - 1);

        Random r = new Random();

        switch (nombreItem.toLowerCase()) {

            // =================== ÍTEMS COMUNES ===================

            // 1. Hierba Sanadora → cura por 5 turnos, 5–12 HP por turno
            case "hierba sanadora":
                objetivo.setEstado(new Estado("CuracionRegenerativa", 5));
                return objetivo.getNombre() + " recibirá curación continua por 5 turnos.";

            // 2. Despertar a un compañero
            case "campanilla despertar":
                if (objetivo.getEstado() != null && objetivo.getEstado().getNombre().equalsIgnoreCase("Sueño")) {
                    objetivo.setEstado(null);
                    return objetivo.getNombre() + " ha despertado.";
                }
                return objetivo.getNombre() + " no estaba dormido.";

            // 3. Cura simple de 10 HP
            case "hierba pequeña":
                objetivo.setVidaHp(objetivo.getVidaHp() + 10);
                return objetivo.getNombre() + " recupera 10 HP.";

            // 4. Buff defensivo
            case "poción de defensa":
                objetivo.setDefensa(objetivo.getDefensa() + 3);
                return objetivo.getNombre() + " aumenta su defensa en +3 por un turno.";

            // =================== ÍTEMS ESPECIALES ===================
            // NO deben pedir objetivo, así que ignoramos 'objetivo'

            case "talismán del valor":
                this.setAtaque(this.getAtaque() + 3);
                return "El ataque de " + this.getNombre() + " aumenta permanentemente en +3.";

            case "hacha oxidada gigante":
                this.setAtaque(this.getAtaque() * 2);
                return this.getNombre() + " duplicará su próximo ataque.";

            case "amuleto de maná arcano":
                this.setMagiaMp(this.getMagiaMp() + 20);
                return this.getNombre() + " recupera 20 MP.";

            case "bendición divina":
                for (Heroe h : todosLosHeroes) {
                    if (h.estaVivo()) h.setVidaHp(h.getVidaHp() + 25);
                }
                return "Todos los aliados recuperan 25 HP.";

            default:
                return "Este objeto no tiene efecto programado.";
        }
    }

    // ==========================================================
    // ===============   SERIALIZACIÓN DE HÉROE   ===============
    // ==========================================================
    public String serializar() {
        // Estado en texto (solo el nombre para no depender de getters extra)
        String estadoTxt = "null";
        if (getEstado() != null) {
            estadoTxt = getEstado().getNombre();
        }

        // Inventario: item1:cantidad1,item2:cantidad2,...
        StringBuilder inv = new StringBuilder();
        for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
            if (inv.length() > 0) inv.append(",");
            inv.append(entry.getKey()).append(":").append(entry.getValue());
        }

        return "HEROE;"
                + getNombre() + ";"
                + getVidaHp() + ";"
                + getMagiaMp() + ";"
                + getAtaque() + ";"
                + getDefensa() + ";"
                + getVelocidad() + ";"
                + (estaVivo() ? "1" : "0") + ";"
                + estadoTxt + ";"
                + inv.toString();
    }

    public static Heroe deserializar(String linea) {
        try {
            String[] p = linea.split(";", -1); // -1 para no perder campos vacíos

            // p[0] = "HEROE"
            String nombre = p[1];
            int vida = Integer.parseInt(p[2]);
            int mp = Integer.parseInt(p[3]);
            int atq = Integer.parseInt(p[4]);
            int def = Integer.parseInt(p[5]);
            int vel = Integer.parseInt(p[6]);
            boolean vivo = p[7].equals("1");
            String estadoTxt = p[8];
            String inventarioTxt = p.length > 9 ? p[9] : "";

            Heroe h = new Heroe(nombre, vida, mp, atq, def, vel);
            h.setVive(vivo);

            if (!"null".equalsIgnoreCase(estadoTxt)) {
                // Duración por defecto 1 (si quieres, puedes ajustar)
                h.setEstado(new Estado(estadoTxt, 1));
            }

            // Reconstruir inventario
            if (inventarioTxt != null && !inventarioTxt.isBlank()) {
                String[] items = inventarioTxt.split(",");
                for (String it : items) {
                    String[] kv = it.split(":");
                    if (kv.length == 2) {
                        String nombreItem = kv[0];
                        int cant = Integer.parseInt(kv[1]);
                        for (int i = 0; i < cant; i++) {
                            h.agregarItem(nombreItem);
                        }
                    }
                }
            }

            // Volver a asignar las habilidades por defecto según el nombre
            asignarHabilidadesPorDefecto(h);

            return h;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Habilidades por defecto según el nombre, para partidas cargadas
    private static void asignarHabilidadesPorDefecto(Heroe h) {
        if (h.getNombre().equalsIgnoreCase("Jessica")) {
            h.agregarHabilidad(new Habilidad("Fuego", "daño", 25, 10));
            h.agregarHabilidad(new Habilidad("Curar", "curación", 30, 8));
            h.agregarHabilidad(new Habilidad("Veneno", "estado", 0, 6, 5, "Envenenado"));
        } else if (h.getNombre().equalsIgnoreCase("Angelo")) {
            h.agregarHabilidad(new Habilidad("Rayo Divino", "daño", 35, 12));
            h.agregarHabilidad(new Habilidad("Curación Menor", "curación", 20, 6));
        }
        // Héroe y Yangus no tenían habilidades especiales aquí.
    }
}
