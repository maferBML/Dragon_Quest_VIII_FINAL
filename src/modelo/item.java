package modelo;

public class Item {

    private String nombre;       // Nombre del objeto
    private String descripcion;  // Qué hace el objeto
    private boolean esEspecial;  // true si es único del héroe
    private int valor;           // Cantidad del efecto (curación, MP, buff, etc.)

    public Item(String nombre, String descripcion, boolean esEspecial, int valor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esEspecial = esEspecial;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEspecial() {
        return esEspecial;
    }

    public int getValor() {
        return valor;
    }
}