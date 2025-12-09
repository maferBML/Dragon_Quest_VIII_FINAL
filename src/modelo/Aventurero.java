package modelo;

public class Aventurero {
    private String nombre;
    private int nivel;

    public Aventurero(String nombre, int nivel) {
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivel() {
        return nivel;
    }

    @Override
    public String toString() {
        return nombre + " (Nivel " + nivel + ")";
    }
}

