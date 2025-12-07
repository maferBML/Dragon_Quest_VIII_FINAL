package modelo;
public class Habilidad {
    private String nombre;
    private String tipo;
    private int poder;
    private int costeMp;
    private int duracion;
    private String estado;

    public Habilidad(String nombre, String tipo, int poder, int costeMp) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.poder = poder;
        this.costeMp = costeMp;
        this.duracion = 0;
        this.estado = "Normal";
    }

    public Habilidad(String nombre, String tipo, int poder, int costeMp, int duracion, String estado) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.poder = poder;
        this.costeMp = costeMp;
        this.duracion = duracion;
        this.estado = estado;
    }


    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }
    
    public int getPoder() {
        return poder;
    }

    public int getCosteMp() {
        return costeMp;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getEstado() {
        return estado;
    }

    
}

