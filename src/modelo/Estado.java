package modelo;

public class Estado {
    private String nombre;
    private int duracion;

    public Estado(String nombre, int duracion) {
        this.nombre = nombre;
        this.duracion = duracion;
    }

    // ============================
    // NUEVO: Constructor seguro para partidas cargadas
    // ============================
    public Estado(String nombre) {
        this.nombre = nombre;
        this.duracion = 1;  // duración mínima para que no crashee
    }

    public String getNombre() { return nombre; }
    public int getDuracion() { return duracion; }
    public void reducirDuracion() { if (duracion > 0) duracion--; }
    public boolean terminado() { return duracion <= 0; }

    public void aplicarEfecto(Personaje p) {

        // =====================================================
        //   ESTADO: VENENO  (lo tuyo, intacto)
        // =====================================================
        if (nombre.equalsIgnoreCase("Envenenado")) {

            int danio = 5 + new java.util.Random().nextInt(8); // 5 a 12
            p.setVidaHp(p.getVidaHp() - danio);

            System.out.println(p.getNombre() +
                    " sufre " + danio + " de daño por VENENO.");

            if (p.getVidaHp() <= 0) {
                p.setVidaHp(0);
                p.setVive(false);
                System.out.println(p.getNombre() + " murió por el veneno.");
            }

            reducirDuracion();
            return;
        }

        // =====================================================
        //   ESTADO: CURACIÓN REGENERATIVA  (tu nuevo ítem)
        // =====================================================
        if (nombre.equalsIgnoreCase("CuracionRegenerativa")) {

            int cura = 5 + new java.util.Random().nextInt(8); // 5 a 12
            p.setVidaHp(p.getVidaHp() + cura);

            System.out.println(p.getNombre() +
                    " recupera " + cura + " puntos por CURACIÓN REGENERATIVA.");

            reducirDuracion();

            if (terminado()) {
                System.out.println(p.getNombre() +
                        " ya no tiene curación regenerativa activa.");
                p.setEstado(null);
            }

            return;
        }

        // =====================================================
        //   FUTUROS ESTADOS
        // =====================================================
    }
}
