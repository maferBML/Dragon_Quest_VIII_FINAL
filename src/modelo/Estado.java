package modelo;
public class Estado {
    private String nombre;
    private int duracion;

    public Estado(String nombre, int duracion) {
        this.nombre = nombre;
        this.duracion = duracion;
    }

    public String getNombre() { return nombre; }
    public int getDuracion() { return duracion; }
    public void reducirDuracion() { if (duracion > 0) duracion--; }
    public boolean terminado() { return duracion <= 0; }

    public void aplicarEfecto(Personaje p) {
        // ----- NUEVA LÓGICA: VENENO -----
        if (nombre.equalsIgnoreCase("Envenenado")) {

            // daño variable entre 5 y 12 por turno
            int danio = 5 + new java.util.Random().nextInt(8);

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

    }
}
