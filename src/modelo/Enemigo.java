package modelo;

import java.util.Random;

public class Enemigo extends Personaje {
    private String tipo;
    private boolean miniJefe;
    private Random random = new Random();

    public Enemigo(String nombre, int vidaHp, int magiaMp, int ataque, int defensa, int velocidad, String tipo) {
        super(nombre, vidaHp, magiaMp, ataque, defensa, velocidad);
        this.tipo = tipo;
    }

    public Enemigo(String nombre, int vidaHp, int magiaMp, int ataque, int defensa, int velocidad, String tipo, boolean miniJefe) {
        super(nombre, vidaHp, magiaMp, ataque, defensa, velocidad);
        this.tipo = tipo;
        this.miniJefe = miniJefe;
        if (miniJefe) {
            setVidaHp(getVidaHp() + 50);
            setAtaque(getAtaque() + 15);
            setDefensa(getDefensa() + 10);
        }
    }

    // ================== LÓGICA ORIGINAL (CONSOLA) ==================

    @Override
    public void atacar(Personaje enemigo) {
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            System.out.println(getNombre() + " está paralizado y no puede atacar. (lero lero)");
            return;
        }
        boolean critico = false;
        int danioBase = this.getAtaque() - enemigo.getDefensa();
        if (danioBase < 0) danioBase = 0;

        if (enemigo.isProtegido()) {
            danioBase = (int) (danioBase * 0.30);
            System.out.println(enemigo.getNombre() + " reduce el daño gracias a su postura defensiva.");
        }

        if (miniJefe && random.nextInt(100) < 40) {
            critico = true;
            danioBase *= 2;
            System.out.println(getNombre() + " te da un GOLPE CRÍTICO! CORRE!");
        }

        enemigo.setVidaHp(enemigo.getVidaHp() - danioBase);
        System.out.println(getNombre() + " ataca a " + enemigo.getNombre() + " causando " + danioBase + " puntos de daño.");

        if (miniJefe && critico && enemigo.estaVivo()) {
            if (random.nextInt(100) < 70) {
                int duracionSueño = random.nextInt(3) + 1; // 1-3 turnos
                enemigo.setEstado(new Estado("Sueño", duracionSueño));
                System.out.println(enemigo.getNombre() + " ha caído dormido por " + duracionSueño + " turnos!");
            }
        }

        if (enemigo.getVidaHp() <= 0) {
            enemigo.setVive(false);
            enemigo.setVidaHp(0);
            System.out.println(enemigo.getNombre() + " ha sido derrotado.");
        }
    }


    public void accionAutomatica(Personaje enemigo) {
        int decision = random.nextInt(100);
        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            System.out.println(getNombre() + " está paralizado y no puede actuar.");
            return;
        }

        if (tipo.equalsIgnoreCase("agresivo")) {
            atacar(enemigo);
        } else if (tipo.equalsIgnoreCase("defensivo") && decision < 30) {
            defender();
        } else {
            atacar(enemigo);
        }
    }

    public void defender() {
        System.out.println(this.getNombre() + " se prepara para defenderse del próximo ataque.");
        setDefensa(getDefensa() + 10);
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean esMiniJefe() { return miniJefe; }

    // ================== MÉTODO EXTRA PARA LA GUI ==================

    public String accionAutomaticaTexto(Heroe enemigo) {
        StringBuilder sb = new StringBuilder();

        if (getEstado() != null && getEstado().getNombre().equals("Paralizado")) {
            sb.append(getNombre()).append(" está paralizado y no puede actuar.\n");
            return sb.toString();
        }

        int decision = random.nextInt(100);

        if (tipo.equalsIgnoreCase("agresivo")) {
            sb.append(ataqueTextoInterno(enemigo));
        } else if (tipo.equalsIgnoreCase("defensivo") && decision < 30) {
            setDefensa(getDefensa() + 10);
            sb.append(getNombre()).append(" se prepara para defenderse del próximo ataque.\n");
        } else {
            sb.append(ataqueTextoInterno(enemigo));
        }

        return sb.toString();
    }

    private String ataqueTextoInterno(Personaje enemigo) {
        StringBuilder sb = new StringBuilder();

        if (enemigo == null || !enemigo.estaVivo()) {
            sb.append("No hay objetivo válido para atacar.\n");
            return sb.toString();
        }

        boolean critico = false;
        int danioBase = this.getAtaque() - enemigo.getDefensa();
        if (danioBase < 0) danioBase = 0;

        if (enemigo.isProtegido()) {
            danioBase = (int) (danioBase * 0.30);
            sb.append(enemigo.getNombre())
            .append(" reduce el daño gracias a su postura defensiva.\n");
        }


        if (miniJefe && random.nextInt(100) < 40) {
            critico = true;
            danioBase *= 2;
            sb.append(getNombre()).append(" te da un GOLPE CRÍTICO! CORRE!\n");
        }

        enemigo.setVidaHp(enemigo.getVidaHp() - danioBase);
        sb.append(getNombre()).append(" ataca a ")
          .append(enemigo.getNombre())
          .append(" causando ").append(danioBase).append(" puntos de daño.\n");

        if (miniJefe && critico && enemigo.estaVivo()) {
            if (random.nextInt(100) < 70) {
                int duracionSueño = random.nextInt(3) + 1; // 1-3 turnos
                enemigo.setEstado(new Estado("Sueño", duracionSueño));
                sb.append(enemigo.getNombre())
                  .append(" ha caído dormido por ")
                  .append(duracionSueño).append(" turnos!\n");
            }
        }

        if (enemigo.getVidaHp() <= 0) {
            enemigo.setVive(false);
            enemigo.setVidaHp(0);
            sb.append(enemigo.getNombre()).append(" ha sido derrotado.\n");
        }

        return sb.toString();
    }
}