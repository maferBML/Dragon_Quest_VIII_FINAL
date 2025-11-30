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
        if (nombre.equals("Envenenado")) {
            System.out.println(p.getNombre() + " sufre daño por veneno.");
            p.setVidaHp(p.getVidaHp() - 10);
            if (p.getVidaHp() <= 0) {
                p.setVidaHp(0);
                p.setVive(false);
                System.out.println(p.getNombre() + " ha muerto por veneno.");
            }
            reducirDuracion();
        }

        else if (nombre.equals("Sueño")) {
            reducirDuracion(); 
        }
    }
}