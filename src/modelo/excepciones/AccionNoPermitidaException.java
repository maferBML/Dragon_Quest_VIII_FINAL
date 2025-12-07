package modelo.excepciones;

public class AccionNoPermitidaException extends Exception {

    public AccionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}