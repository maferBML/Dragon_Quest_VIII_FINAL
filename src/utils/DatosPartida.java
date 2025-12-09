package utils;

import modelo.Enemigo;
import modelo.Heroe;

import java.util.ArrayList;

public class DatosPartida {

    private ArrayList<Heroe> heroes;
    private ArrayList<Enemigo> enemigos;
    private int turno;
    private int indiceHeroeActual;

    public DatosPartida(ArrayList<Heroe> heroes,
                        ArrayList<Enemigo> enemigos,
                        int turno,
                        int indiceHeroeActual) {
        this.heroes = heroes;
        this.enemigos = enemigos;
        this.turno = turno;
        this.indiceHeroeActual = indiceHeroeActual;
    }

    public ArrayList<Heroe> getHeroes() {
        return heroes;
    }

    public ArrayList<Enemigo> getEnemigos() {
        return enemigos;
    }

    public int getTurno() {
        return turno;
    }

    public int getIndiceHeroeActual() {
        return indiceHeroeActual;
    }
}
