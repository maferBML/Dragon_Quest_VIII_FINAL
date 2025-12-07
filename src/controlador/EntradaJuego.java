package controlador;

import java.util.Scanner;

public class EntradaJuego {

    private ControlJuego control;

    public EntradaJuego() {
        this.control = new ControlJuego();
    }

    public void iniciar() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== DRAGON QUEST VIII T3 ===");
        System.out.println("1. Modo consola");
        System.out.println("2. Modo GUI");
        System.out.print("Elige una opción: ");
        int opcion = sc.nextInt();

        if (opcion == 1) {
            control.iniciarConsola();
        } else {
            control.iniciarGUI();
        }
    }
}