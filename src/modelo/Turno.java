package modelo;

/**
 * Contiene la información necesaria para ejecutar una acción en el Combate.
 */
public class Turno {
    public enum Accion {
        ATAQUE_BASICO, HABILIDAD
    }

    private final Personaje atacante;
    private final Personaje objetivo;
    private final Accion tipoAccion;
    private final Habilidad habilidadUsada; // Null si es ataque básico

    // Constructor para ataque básico
    public Turno(Personaje atacante, Personaje objetivo) {
        this.atacante = atacante;
        this.objetivo = objetivo;
        this.tipoAccion = Accion.ATAQUE_BASICO;
        this.habilidadUsada = null;
    }

    // Constructor para habilidad
    public Turno(Personaje atacante, Personaje objetivo, Habilidad habilidadUsada) {
        this.atacante = atacante;
        this.objetivo = objetivo;
        this.tipoAccion = Accion.HABILIDAD;
        this.habilidadUsada = habilidadUsada;
    }

    public Personaje getAtacante() { return atacante; }
    public Personaje getObjetivo() { return objetivo; }
    public Accion getTipoAccion() { return tipoAccion; }
    public Habilidad getHabilidadUsada() { return habilidadUsada; }
}
