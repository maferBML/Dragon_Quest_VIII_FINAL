package modelo;
import java.util.*;

public class Combate {
    private List<Heroe> heroes;
    private List<Enemigo> enemigos;
    private Random random = new Random();

    public Combate(List<Heroe> heroes, List<Enemigo> enemigos) {
        this.heroes = heroes;
        this.enemigos = enemigos;
    }

    public void iniciar() {
        System.out.println("=== ¡Comienza el combate, FIGHT! ===\n");
        int turno = 1;
        Scanner sc = new Scanner(System.in);

        while (hayVivos(heroes) && hayVivos(enemigos)) {
            System.out.println("---- Turno " + turno + " ----");
            mostrarEstado();

            System.out.print("\n¿Vas a continuar? (s/n): ");
            String seguir = sc.next().toLowerCase();
            if (seguir.equals("n")) {
                System.out.println("\n¡COBARDE! ¡lOS ENEMIGOS GANARON!");
            return;
        }

            List<Personaje> participantes = new ArrayList<>();
            participantes.addAll(heroes);
            participantes.addAll(enemigos);

            // Se ordenan por velocidad (mayor primero)
            participantes.sort((a, b) -> b.getVelocidad() - a.getVelocidad());

            for (Personaje p : participantes) {
                if (!p.estaVivo()) continue;

                // Aplicar efectos de estado
                if (p.getEstado() != null) {
                    if (p.getEstado().getNombre().equals("Sueño")) {
                        System.out.println(p.getNombre() + " está dormido (" + p.getEstado().getDuracion() + " turnos restantes).");
                        boolean despierta = random.nextInt(100) < 33;
                        if (despierta) {
                            System.out.println(p.getNombre() + " se desperto! :O");
                            p.setEstado(null);
                        } else {
                            System.out.println(p.getNombre() + " sigue dormido y pierde el turno.");
                            p.getEstado().reducirDuracion();
                            if (p.getEstado().terminado()) {
                                System.out.println(p.getNombre() + " ya no está " + p.getEstado().getNombre() + ".");
                                p.setEstado(null);
                            }
                            continue;
                        }
                    } else {
                        p.getEstado().aplicarEfecto(p);
                        if (p.getEstado() != null && p.getEstado().terminado()) {
                            System.out.println(p.getNombre() + " ya no está " + p.getEstado().getNombre() + ".");
                            p.setEstado(null);
                        }
                    }
                }

                if (!p.estaVivo()) continue;

                // Turno de los héroes
                if (p instanceof Heroe) {
                    Heroe heroe = (Heroe) p;

                    System.out.println("\n==============================");
                    System.out.println("     Turno de " + heroe.getNombre());
                    System.out.println("==============================");
                    boolean accionRealizada = false;

while (!accionRealizada) {
    System.out.println("1. Atacar");
    System.out.println("2. Defender");
    System.out.println("3. Usar Habilidad");
    System.out.print("Elige una acción (1,2,3): ");
    int opcion = sc.nextInt();

    switch (opcion) {
        case 1 -> {
            Enemigo objetivo = elegirEnemigo();
            if (objetivo != null) heroe.atacar(objetivo);
            accionRealizada = true;
        }
        case 2 -> {
            heroe.defender();
            accionRealizada = true;
        }
        case 3 -> {
            int mpAntes = heroe.getMagiaMp();
            heroe.usarHabilidad((ArrayList<Heroe>) heroes, enemigos);

            if (heroe.getMagiaMp() != mpAntes) accionRealizada = true;
            else System.out.println("\nElige otra acción: ");
        }
        default -> System.out.println("\nQue fue eso?\n");
    }
}


                } 
                // Turno de los enemigos
                else if (p instanceof Enemigo) {
                    Heroe objetivo = elegirHeroe();
                    if (objetivo != null) ((Enemigo) p).accionAutomatica(objetivo);
                }

                if (!hayVivos(heroes) || !hayVivos(enemigos)) break;
            }
            turno++;
        }

        if (hayVivos(heroes))
            System.out.println("¡GANASTE MASTER!");
        else
            System.out.println("¡Sos un malo!");
    }

    private boolean hayVivos(List<? extends Personaje> lista) {
        for (Personaje p : lista) if (p.estaVivo()) return true;
        return false;
    }

    private void mostrarEstado() {
        System.out.println("\n Héroes:");
        for (Heroe h : heroes) {
            System.out.println("  " + h.getNombre() + " - HP: " + h.getVidaHp() + " MP: " + h.getMagiaMp() + estadoString(h.getEstado()));
        }

        System.out.println("\n Enemigos:");
        for (int i = 0; i < enemigos.size(); i++) {
            Enemigo e = enemigos.get(i);
            if (e.estaVivo())
                System.out.println("  [" + (i + 1) + "] " + e.getNombre() + " - HP: " + e.getVidaHp() + estadoString(e.getEstado()));
        }
    }

    private String estadoString(Estado est) {
        if (est == null) return "";
        return " [" + est.getNombre() + " (" + est.getDuracion() + ")]";
    }

    // 🔹 Elegir enemigo específico
    private Enemigo elegirEnemigo() {
        Scanner sc = new Scanner(System.in);
        List<Enemigo> vivos = new ArrayList<>();
        for (Enemigo e : enemigos) if (e.estaVivo()) vivos.add(e);

        if (vivos.isEmpty()) return null;

        System.out.println("\nElige un enemigo para atacar:");
        for (int i = 0; i < vivos.size(); i++) {
            Enemigo e = vivos.get(i);
            System.out.println((i + 1) + ". " + e.getNombre() + " (HP: " + e.getVidaHp() + ")");
        }

        System.out.print("Número del enemigo: ");
        int eleccion = sc.nextInt();

        if (eleccion < 1 || eleccion > vivos.size()) {
            System.out.println("\nOpción inválida, se elige un enemigo al azar.");
            return vivos.get(random.nextInt(vivos.size()));
        }
        return vivos.get(eleccion - 1);
    }

    // 🔹 Elegir héroe al azar
    private Heroe elegirHeroe() {
        List<Heroe> vivos = new ArrayList<>();
        for (Heroe h : heroes) if (h.estaVivo()) vivos.add(h);
        return vivos.isEmpty() ? null : vivos.get(random.nextInt(vivos.size()));
    }

    // 🔹 Versión para GUI (captura texto en lugar de imprimir en consola)
public String iniciarDesdeGUI() {
    StringBuilder resultado = new StringBuilder();
    resultado.append("💥 ¡Comienza la batalla épica! 💥\n\n");
    int turno = 1;

    while (hayVivos(heroes) && hayVivos(enemigos) && turno <= 1) { 
        // Solo muestra un turno al iniciar
        resultado.append("---- Turno ").append(turno).append(" ----\n");
        for (Heroe h : heroes) {
            resultado.append(h.getNombre())
                     .append(" - HP: ").append(h.getVidaHp())
                     .append(" | MP: ").append(h.getMagiaMp()).append("\n");
        }
        resultado.append("\n");

        for (Enemigo e : enemigos) {
            resultado.append(e.getNombre())
                     .append(" - HP: ").append(e.getVidaHp()).append("\n");
        }

        resultado.append("\nSelecciona una acción para comenzar...");
        break; // Solo muestra el estado inicial
    }

    return resultado.toString();
}

}