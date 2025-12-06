package modelo;
public abstract class Personaje {
    private String nombre;
    private int vidaHp;
    private int magiaMp;
    private int ataque;
    private int defensa;
    private int velocidad;
    private boolean vive;
    private Estado estado;

    public Personaje(String nombre, int vidaHp, int magiaMp, int ataque, int defensa, int velocidad) {
        this.nombre = nombre;
        this.vidaHp = vidaHp;
        this.magiaMp = magiaMp;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.vive = true;
        this.estado = null;
    }

    public abstract void atacar(Personaje enemigo);

    public void recibirDanio(int danio) {
        int danioRecibido = danio - this.defensa;
        if (danioRecibido < 0) {
            danioRecibido = 0;
        }
        this.vidaHp -= danioRecibido;
        if (this.vidaHp <= 0) {
            this.vidaHp = 0;
            this.vive = false;
            System.out.println(this.nombre + " ha sido derrotado.");
        } else {
            System.out.println(this.nombre + " ha recibido " + danioRecibido + " puntos de daño. Vida restante: " + this.vidaHp);
        }
    }

    public boolean estaVivo() {
        return this.vive;
    }

    public void mostrarEstado() {
        System.out.println("Nombre: " + this.nombre);
        System.out.println("Vida (HP): " + this.vidaHp);
        System.out.println("Magia (MP): " + this.magiaMp);
        System.out.println("Ataque: " + this.ataque);
        System.out.println("Defensa: " + this.defensa);
        System.out.println("Velocidad: " + this.velocidad);
        System.out.println("Estado: " + (this.vive ? "Vivo" : "Derrotado") +
            (estado != null ? " [" + estado.getNombre() + " " + estado.getDuracion() + "]" : ""));
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public int getVidaHp() {
        return vidaHp;
    }

    public int getMagiaMp() {
        return magiaMp;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public boolean isVive() {
        return vive;
    }

    public Estado getEstado() {
        return estado;
    }

    // 🔹 Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setVidaHp(int vidaHp) {
        this.vidaHp = vidaHp;
    }

    public void setMagiaMp(int magiaMp) {
        this.magiaMp = magiaMp;
    }

    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public void setVive(boolean vive) {
        this.vive = vive;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}