package vista;

import modelo.Musica;
import controlador.ControlJuego;
import modelo.Enemigo;
import modelo.Estado;
import modelo.Habilidad;
import modelo.Heroe;
import modelo.Personaje;

import modelo.excepciones.DefenderException;
import modelo.excepciones.HabilidadSinMPException;
import modelo.excepciones.ObjetivoInvalidoException;
import modelo.excepciones.AccionNoPermitidaException;

import modelo.HistorialBatallas;
import utils.GuardarPartida;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Deque;
import java.util.HashMap;
import java.util.ArrayDeque;

public class VentanaBatalla extends JFrame {

    private boolean batallaFinalizada = false;

    private enum ModoAccion { NINGUNO, ATACAR, HABILIDAD_ENEMIGO }

    private ControlJuego control;

    private JButton btnObjetos;

    private Image fondo;
    private JTextArea cuadroTexto;
    private JPanel panelHeroes, panelEnemigos;
    private JPanel panelInferior;
    private JPanel panelMenuAcciones;

    private ArrayList<Heroe> heroes;
    private ArrayList<Enemigo> enemigos;

    private ArrayList<JLabel> labelsHeroes = new ArrayList<>();
    private ArrayList<JLabel> labelsImagenHeroes = new ArrayList<>();
    private ArrayList<ImageIcon> iconosNormalesHeroes = new ArrayList<>();
    private ArrayList<ImageIcon> iconosActivosHeroes = new ArrayList<>();

    private ArrayList<JPanel> panelesEnemigos = new ArrayList<>();
    private ArrayList<JLabel> labelsEnemigos = new ArrayList<>();

    private int indiceHeroeActual = 0;
    private Random random = new Random();
    private ModoAccion modoActual = ModoAccion.NINGUNO;

    private JButton btnAtacar;
    private JButton btnDefender;
    private JButton btnHabilidad;
    private JButton btnSalir;
    private JButton btnDeshacer;
    private JButton btnRehacer;

    private Habilidad habilidadSeleccionada = null;

    private Musica musicaBatalla = new Musica();

    // turno de batalla (rondas), se va incrementando en turnoEnemigo()
    private int turnoActual = 1;

    // ================== CLASES AUXILIARES PARA UNDO/REDO ==================

    private static class DatosPersonaje {
        int vida, mp, ataque, defensa, velocidad;
        boolean vive, protegido;
        String estadoNombre;
        int estadoDuracion;
    }

    private static class EstadoBatalla {
        DatosPersonaje[] heroes;
        DatosPersonaje[] enemigos;
        int indiceHeroeActual;
    }

    private Deque<EstadoBatalla> pilaUndo = new ArrayDeque<>();
    private Deque<EstadoBatalla> pilaRedo = new ArrayDeque<>();

    // ================== CONSTRUCTORES ==================

    // Constructor normal (nueva partida)
    public VentanaBatalla(ControlJuego control) {
        this.control = control;
        this.heroes = control.getHeroes();
        this.enemigos = control.getEnemigos();

        setTitle("⚔️ Batalla en el Reino de Trodain");
        setSize(900, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        fondo = new ImageIcon(getClass().getResource("/foticos/bosque.jpg")).getImage();

        // Música únicamente de batalla
        musicaBatalla.reproducirLoop("/sonidos/batalla.wav");

        construirInterfaz();

        Heroe actual = obtenerHeroeActual();
        if (actual != null) {
            cuadroTexto.append("\nTurno inicial de: " + actual.getNombre() + "\n");
            resaltarHeroe(actual);
        }

        guardarEstadoActual();
        setVisible(true);
    }

    // Constructor para partidas CARGADAS
    public VentanaBatalla(ControlJuego control, int turnoInicial) {
        // Construye toda la ventana normalmente
        this(control);

        // Sobrescribimos el turno con el que se guardó
        this.turnoActual = turnoInicial;

        // Aproximamos qué héroe sigue, usando el número de turno guardado
        if (heroes != null && !heroes.isEmpty()) {
            if (turnoInicial <= 0) {
                this.indiceHeroeActual = 0;
            } else {
                this.indiceHeroeActual = turnoInicial % heroes.size();
            }

            Heroe actual = obtenerHeroeActual();
            if (actual != null) {
                resaltarHeroe(actual);
                cuadroTexto.append("\n[Partida cargada] Turno de: " + actual.getNombre() + "\n");
            }
        }
    }

    // ================== INTERFAZ ==================

    private void construirInterfaz() {

        JPanel panelFondo = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fondo != null) {
                    g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        panelFondo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // HÉROES
        panelHeroes = new JPanel(new GridLayout(1, heroes.size(), 15, 15));
        panelHeroes.setOpaque(false);
        panelHeroes.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 50));
        for (Heroe h : heroes) {
            agregarHeroe(h);
        }

        // ENEMIGOS
        panelEnemigos = new JPanel(new GridLayout(1, enemigos.size(), 15, 15));
        panelEnemigos.setOpaque(false);
        panelEnemigos.setBorder(BorderFactory.createEmptyBorder(50, 50, 30, 50));
        for (Enemigo e : enemigos) {
            agregarEnemigo(e);
        }

        // CUADRO DE TEXTO
        cuadroTexto = new JTextArea(8, 20);
        cuadroTexto.setEditable(false);
        cuadroTexto.setWrapStyleWord(true);
        cuadroTexto.setLineWrap(true);
        cuadroTexto.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cuadroTexto.setBackground(new Color(20, 20, 50));
        cuadroTexto.setForeground(Color.WHITE);
        cuadroTexto.setText(mensajeJefeInicial());
        cuadroTexto.append("\n💥 ¡Comienza la batalla! 💥\n");

        JScrollPane scrollTexto = new JScrollPane(cuadroTexto);
        scrollTexto.setBorder(null);

        JPanel panelTexto = new JPanel(new BorderLayout());
        panelTexto.setBackground(new Color(10, 10, 30));
        panelTexto.add(scrollTexto, BorderLayout.CENTER);

        // MENÚ DE ACCIONES
        panelMenuAcciones = new JPanel();
        panelMenuAcciones.setBackground(new Color(10, 10, 30));
        panelMenuAcciones.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panelMenuAcciones.setLayout(new GridLayout(0, 1, 5, 5));
        panelMenuAcciones.setPreferredSize(new Dimension(230, 0));

        btnAtacar    = crearBoton("Atacar");
        btnDefender  = crearBoton("Defender");
        btnHabilidad = crearBoton("Habilidad");
        btnDeshacer  = crearBoton("Deshacer");
        btnRehacer   = crearBoton("Rehacer");
        btnSalir     = crearBoton("Salir");
        btnObjetos   = crearBoton("Objetos");

        panelMenuAcciones.add(btnObjetos);

        // ====== LISTENERS BOTONES ======

        btnObjetos.addActionListener(e -> abrirInventario());

        btnAtacar.addActionListener(e -> {
            Heroe h = obtenerHeroeActual();
            if (h == null) {
                cuadroTexto.append("\nNo quedan héroes vivos.\n");
                return;
            }
            if (!procesarEstado(h)) {
                finTurnoJugador();
                turnoEnemigo();
                return;
            }
            modoActual = ModoAccion.ATACAR;
            habilidadSeleccionada = null;
            cuadroTexto.append("\n" + h.getNombre()
                    + " se prepara para atacar. Elige un enemigo.\n");
            mostrarMenuAcciones(false);
        });

        btnDefender.addActionListener(e -> {
            Heroe h = obtenerHeroeActual();
            if (!procesarEstado(h)) {
                finTurnoJugador();
                turnoEnemigo();
                return;
            }
            if (h == null) {
                cuadroTexto.append("\nNo quedan héroes vivos.\n");
                return;
            }
            cuadroTexto.append("\n👉 Turno de: " + h.getNombre() + "\n");
            try {
                cuadroTexto.append(h.defenderTexto());
                mostrarMenuAcciones(false);
                finTurnoJugador();
                turnoEnemigo();
            } catch (DefenderException ex) {
                cuadroTexto.append(ex.getMessage());
            }
        });

        btnHabilidad.addActionListener(e -> {
            Heroe h = obtenerHeroeActual();
            if (!procesarEstado(h)) {
                finTurnoJugador();
                turnoEnemigo();
                return;
            }
            manejarHabilidad();
        });

        btnDeshacer.addActionListener(e -> deshacerRonda());
        btnRehacer.addActionListener(e -> rehacerRonda());

        // Botón SALIR: pregunta si guardar
        btnSalir.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(
                    this,
                    "¿Quieres guardar antes de salir?",
                    "Guardar y salir",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (op == JOptionPane.CANCEL_OPTION) return;

            if (op == JOptionPane.YES_OPTION) {
                guardarPartida();
            }
            musicaBatalla.parar();
            System.exit(0);
        });

        panelMenuAcciones.add(btnAtacar);
        panelMenuAcciones.add(btnDefender);
        panelMenuAcciones.add(btnHabilidad);
        panelMenuAcciones.add(btnDeshacer);
        panelMenuAcciones.add(btnRehacer);
        panelMenuAcciones.add(btnSalir);

        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(10, 10, 30));
        panelInferior.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        panelInferior.add(panelTexto, BorderLayout.CENTER);
        panelInferior.add(panelMenuAcciones, BorderLayout.EAST);

        panelFondo.add(panelHeroes, BorderLayout.NORTH);
        panelFondo.add(panelEnemigos, BorderLayout.CENTER);
        panelFondo.add(panelInferior, BorderLayout.SOUTH);

        add(panelFondo);
    }

    private void mostrarMenuAcciones(boolean mostrar) {
        panelMenuAcciones.setVisible(mostrar);
        panelMenuAcciones.revalidate();
        panelMenuAcciones.repaint();
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(40, 40, 90));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return btn;
    }

    private ImageIcon cargarIcono(String ruta, int w, int h) {
        URL url = getClass().getResource(ruta);
        if (url == null) {
            System.out.println("No se encontró recurso: " + ruta);
            return null;
        }
        ImageIcon base = new ImageIcon(url);
        Image img = base.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private ImageIcon iconoHeroePorNombre(String nombre, boolean activo) {
        String archivo = null;

        if (nombre.equalsIgnoreCase("Héroe") || nombre.equalsIgnoreCase("Heroe")) {
            archivo = activo ? "/foticos/hero_activo.png" : "/foticos/hero_normal.png";
            return cargarIcono(archivo, 70, 70);
        }
        if (nombre.equalsIgnoreCase("Yangus")) {
            archivo = activo ? "/foticos/yangus_activo.png" : "/foticos/yangus_normal.png";
            return cargarIcono(archivo, 70, 70);
        }
        if (nombre.equalsIgnoreCase("Jessica")) {
            archivo = activo ? "/foticos/jessica_activo.png" : "/foticos/jessica_normal.png";
            return cargarIcono(archivo, 70, 70);
        }
        if (nombre.equalsIgnoreCase("Angelo")) {
            archivo = activo ? "/foticos/angelo_activo.png" : "/foticos/angelo_normal.png";
            return cargarIcono(archivo, 70, 70);
        }
        return null;
    }

    private ImageIcon iconoEnemigoPorNombre(String nombre) {
        String archivo = null;
        if (nombre.equalsIgnoreCase("Goblin")) archivo = "/foticos/goblin.png";
        else if (nombre.equalsIgnoreCase("Slime")) archivo = "/foticos/slime.png";
        else if (nombre.equalsIgnoreCase("Dragón") || nombre.equalsIgnoreCase("Dragon"))
            archivo = "/foticos/dragon.png";
        else if (nombre.equalsIgnoreCase("Esqueleto")) archivo = "/foticos/esqueleto.png";
        return archivo == null ? null : new ImageIcon(getClass().getResource(archivo));
    }

    private void agregarHeroe(Heroe h) {
        JPanel panelHeroe = new JPanel(new GridBagLayout());
        panelHeroe.setOpaque(true);
        panelHeroe.setBackground(new Color(20, 20, 50));
        panelHeroe.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panelHeroe.setPreferredSize(new Dimension(280, 90));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        ImageIcon iconNormal = iconoHeroePorNombre(h.getNombre(), false);
        ImageIcon iconActivo = iconoHeroePorNombre(h.getNombre(), true);

        JLabel lblImg = new JLabel();
        lblImg.setOpaque(false);
        lblImg.setHorizontalAlignment(JLabel.LEFT);
        if (iconNormal != null) {
            lblImg.setIcon(iconNormal);
        }

        labelsImagenHeroes.add(lblImg);
        iconosNormalesHeroes.add(iconNormal);
        iconosActivosHeroes.add(iconActivo);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panelHeroe.add(lblImg, gbc);

        JLabel lblStats = new JLabel(
                "<html><center><b>" + h.getNombre() + "</b><br>HP: "
                        + h.getVidaHp() + "<br>MP: " + h.getMagiaMp() + "</center></html>"
        );
        lblStats.setForeground(Color.WHITE);
        lblStats.setFont(new Font("Serif", Font.BOLD, 14));
        labelsHeroes.add(lblStats);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panelHeroe.add(lblStats, gbc);

        panelHeroes.add(panelHeroe);
    }

    private void agregarEnemigo(Enemigo e) {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);

        JLayeredPane capa = new JLayeredPane();

        int w = 140, h = 120, offsetY = 45;

        switch (e.getNombre().toLowerCase()) {
            case "slime":
                w = 80; h = 80; offsetY = 70;
                break;
            case "goblin":
                w = 120; h = 120; offsetY = 40;
                break;
            case "dragón":
            case "dragon":
                w = 130; h = 130; offsetY = 35;
                break;
            case "esqueleto":
                w = 120; h = 120; offsetY = 40;
                break;
        }

        int panelW = w + 60;
        int panelH = 170;

        capa.setPreferredSize(new Dimension(panelW, panelH));

        ImageIcon icon = iconoEnemigoPorNombre(e.getNombre());
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }

        JLabel lblImg = new JLabel(icon);
        int imgX = (panelW - w) / 2;
        lblImg.setBounds(imgX, offsetY, w, h);
        capa.add(lblImg, JLayeredPane.DEFAULT_LAYER);

        JLabel textoArriba = new JLabel(
                "<html><center>HP: " + e.getVidaHp() + "</center></html>",
                JLabel.CENTER
        );

        textoArriba.setForeground(e.esMiniJefe() ? Color.ORANGE : Color.RED);
        textoArriba.setFont(new Font("Serif", Font.BOLD, 16));
        textoArriba.setBounds(imgX - 10, 160, w + 20, 40);

        capa.add(textoArriba, JLayeredPane.PALETTE_LAYER);

        JPanel marco = new JPanel();
        marco.setOpaque(false);
        marco.setBorder(BorderFactory.createLineBorder(
                e.esMiniJefe() ? Color.ORANGE : Color.RED, 3
        ));
        marco.setBounds(
                imgX - 10,
                offsetY - 10,
                w + 25,
                h + 15
        );
        capa.add(marco, JLayeredPane.PALETTE_LAYER);

        labelsEnemigos.add(textoArriba);
        panelesEnemigos.add(contenedor);

        capa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                enemigoClicado(e, contenedor);
            }
        });

        contenedor.add(capa, BorderLayout.CENTER);
        panelEnemigos.add(contenedor);
    }

    private String mensajeJefeInicial() {
        StringBuilder sb = new StringBuilder();
        for (Enemigo e : enemigos) {
            if (e.esMiniJefe()) {
                sb.append("¡Un ").append(e.getNombre().toUpperCase())
                        .append(" ha aparecido como JEFE (Cagaste)!\n")
                        .append("HP aumentado: ").append(e.getVidaHp()).append("\n")
                        .append("Ataque aumentado: ").append(e.getAtaque()).append("\n")
                        .append("Defensa aumentada: ").append(e.getDefensa()).append("\n");
                break;
            }
        }
        sb.append("==============================================\n");
        return sb.toString();
    }

    // ================== LÓGICA DE TURNO ==================

    private Heroe obtenerHeroeActual() {
        int intentos = 0;
        while (intentos < heroes.size()) {
            Heroe h = heroes.get(indiceHeroeActual);
            if (h.estaVivo()) {
                if (h.isProtegido()) {
                    h.setProtegido(false);
                }
                return h;
            }
            indiceHeroeActual = (indiceHeroeActual + 1) % heroes.size();
            intentos++;
        }
        return null;
    }

    private void resaltarHeroe(Heroe actual) {
        for (int i = 0; i < heroes.size(); i++) {
            Heroe h = heroes.get(i);
            JLabel lblStats = labelsHeroes.get(i);
            JLabel lblImg = labelsImagenHeroes.get(i);

            boolean esActual = (h == actual);
            lblStats.setForeground(esActual ? Color.YELLOW : Color.WHITE);

            ImageIcon icon = esActual ? iconosActivosHeroes.get(i) : iconosNormalesHeroes.get(i);
            if (icon != null) lblImg.setIcon(icon);
        }
        panelHeroes.revalidate();
        panelHeroes.repaint();
    }

    private Enemigo elegirEnemigoVivoAleatorio() {
        ArrayList<Enemigo> vivos = new ArrayList<>();
        for (Enemigo e : enemigos) if (e.estaVivo()) vivos.add(e);
        if (vivos.isEmpty()) return null;
        return vivos.get(random.nextInt(vivos.size()));
    }

    private Heroe elegirHeroeVivoAleatorio() {
        ArrayList<Heroe> vivos = new ArrayList<>();
        for (Heroe h : heroes) if (h.estaVivo()) vivos.add(h);
        if (vivos.isEmpty()) return null;
        return vivos.get(random.nextInt(vivos.size()));
    }

    private boolean hayVivos(List<? extends Personaje> lista) {
        for (Personaje p : lista) if (p.estaVivo()) return true;
        return false;
    }

    private void actualizarHeroes() {
        for (int i = 0; i < heroes.size(); i++) {
            Heroe h = heroes.get(i);
            JLabel lbl = labelsHeroes.get(i);
            JLabel lblImg = labelsImagenHeroes.get(i);
            if (!h.estaVivo()) {
                lbl.setVisible(false);
                lblImg.setVisible(false);
            } else {
                lbl.setVisible(true);
                lblImg.setVisible(true);
                lbl.setText("<html><center><b>" + h.getNombre() +
                        "</b><br>HP: " + h.getVidaHp() +
                        "<br>MP: " + h.getMagiaMp() + "</center></html>");
            }
        }
        panelHeroes.revalidate();
        panelHeroes.repaint();
    }

    private void actualizarEnemigos() {
        for (int i = 0; i < enemigos.size(); i++) {
            Enemigo e = enemigos.get(i);
            JLabel lbl = labelsEnemigos.get(i);
            JPanel panelE = panelesEnemigos.get(i);

            if (!e.estaVivo()) {
                lbl.setVisible(false);
                panelE.setVisible(false);
                panelE.setEnabled(false);
            } else {
                lbl.setVisible(true);
                panelE.setVisible(true);
                panelE.setEnabled(true);
                lbl.setText("<html><center>HP: " + e.getVidaHp() + "</center></html>");
            }
        }
        panelEnemigos.revalidate();
        panelEnemigos.repaint();
    }

    // ================== ESTADOS ESPECIALES (VENENO, CURA) ==================

    private boolean procesarVeneno(Personaje p) {
        if (p == null) return true;
        if (p.getEstado() == null) return true;

        if (!p.getEstado().getNombre().equalsIgnoreCase("Envenenado")) return true;

        int danio = 5 + random.nextInt(8);
        p.setVidaHp(p.getVidaHp() - danio);

        cuadroTexto.append("☠️ " + p.getNombre() +
                " sufre " + danio + " de daño por veneno.\n");

        p.getEstado().reducirDuracion();

        if (p.getVidaHp() <= 0) {
            p.setVidaHp(0);
            p.setVive(false);
            cuadroTexto.append("💀 " + p.getNombre() +
                    " ha muerto a causa del veneno.\n");
        }

        if (p.getEstado() != null && p.getEstado().terminado()) {
            cuadroTexto.append("✨ El veneno en " + p.getNombre() +
                    " se ha disipado.\n");
            p.setEstado(null);
        }

        return p.estaVivo();
    }

    private boolean procesarCuracion(Personaje p) {
        if (p == null) return true;
        if (p.getEstado() == null) return true;

        if (!p.getEstado().getNombre().equalsIgnoreCase("CuracionRegenerativa"))
            return true;

        int cura = 5 + random.nextInt(8);
        p.setVidaHp(p.getVidaHp() + cura);

        cuadroTexto.append("✨ " + p.getNombre() +
                " recupera " + cura + " puntos de vida.\n");

        p.getEstado().reducirDuracion();

        if (p.getEstado().terminado()) {
            cuadroTexto.append("💫 La curación regenerativa terminó para " +
                    p.getNombre() + ".\n");
            p.setEstado(null);
        }

        return true;
    }

    private void deshabilitarTodo() {
        for (JPanel p : panelesEnemigos) p.setEnabled(false);

        btnAtacar.setEnabled(false);
        btnDefender.setEnabled(false);
        btnHabilidad.setEnabled(false);
        btnSalir.setEnabled(false);
        btnObjetos.setEnabled(false);
        btnDeshacer.setEnabled(false);
        btnRehacer.setEnabled(false);
    }

    // ================== CLICK EN ENEMIGO ==================

    private void enemigoClicado(Enemigo enemigo, JPanel panelEnemigo) {
        if (!panelEnemigo.isEnabled()) return;

        Heroe atacante = obtenerHeroeActual();
        if (atacante == null) return;

        if (modoActual == ModoAccion.NINGUNO) return;
        if (!enemigo.estaVivo()) return;

        if (modoActual == ModoAccion.ATACAR) {
            ejecutarAtaqueBasico(atacante, enemigo, panelEnemigo);
            modoActual = ModoAccion.NINGUNO;
            mostrarMenuAcciones(true);
            return;
        }

        if (modoActual == ModoAccion.HABILIDAD_ENEMIGO && habilidadSeleccionada != null) {
            ejecutarHabilidadEnemigo(atacante, habilidadSeleccionada, enemigo, panelEnemigo);
            modoActual = ModoAccion.NINGUNO;
            mostrarMenuAcciones(true);
        }
    }

    private void ejecutarAtaqueBasico(Heroe atacante, Enemigo objetivo, JPanel panelEnemigo) {
        if (objetivo == null || !objetivo.estaVivo()) {
            cuadroTexto.append("Ese enemigo ya está derrotado.\n");
            return;
        }

        cuadroTexto.append("\n👉 Turno de: " + atacante.getNombre() + "\n");
        try {
            cuadroTexto.append(atacante.atacarTexto(objetivo));
        } catch (ObjetivoInvalidoException e) {
            cuadroTexto.append(e.getMessage());
            mostrarMenuAcciones(true);
            return;
        }

        actualizarEnemigos();

        if (!objetivo.estaVivo()) {
            cuadroTexto.append("💥 " + objetivo.getNombre() + " ha sido derrotado.\n");
            panelEnemigo.setVisible(false);
        }

        if (!hayVivos(enemigos)) {
            cuadroTexto.append("\n🏆 ¡HAS GANADO LA BATALLA!\n");
            musicaBatalla.parar();
            finalizarBatalla("Victoria del jugador");
            return;
        }

        finTurnoJugador();
        turnoEnemigo();
    }

    // ================== HABILIDADES ==================

    private void manejarHabilidad() {
        Heroe h = obtenerHeroeActual();
        if (h == null) {
            cuadroTexto.append("\nNo quedan héroes vivos.\n");
            return;
        }

        ArrayList<Habilidad> habilidades = h.getHabilidades();
        if (habilidades.isEmpty()) {
            cuadroTexto.append("\n" + h.getNombre() + " no tiene habilidades.\n");
            return;
        }

        String[] nombres = new String[habilidades.size()];
        for (int i = 0; i < habilidades.size(); i++) {
            Habilidad hab = habilidades.get(i);
            nombres[i] = (i + 1) + ". " + hab.getNombre() +
                    " (MP: " + hab.getCosteMp() + ")";
        }

        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Elige una habilidad:",
                "Habilidades de " + h.getNombre(),
                JOptionPane.PLAIN_MESSAGE,
                null,
                nombres,
                nombres[0]
        );

        if (seleccion == null) return;

        int indice = 0;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(seleccion)) {
                indice = i;
                break;
            }
        }

        Habilidad hSel = habilidades.get(indice);
        String tipo = hSel.getTipo().toLowerCase();

        if (tipo.equals("curación")) {
            ArrayList<Heroe> vivos = new ArrayList<>();
            for (Heroe her : heroes) if (her.estaVivo()) vivos.add(her);

            if (vivos.isEmpty()) {
                cuadroTexto.append("\nNo hay aliados vivos para curar.\n");
                return;
            }

            String[] nombresAliados = new String[vivos.size()];
            for (int i = 0; i < vivos.size(); i++) {
                Heroe ha = vivos.get(i);
                nombresAliados[i] = ha.getNombre() +
                        " (HP: " + ha.getVidaHp() + ")";
            }

            String selAliado = (String) JOptionPane.showInputDialog(
                    this,
                    "¿A quién quieres curar?",
                    "Elegir aliado",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    nombresAliados,
                    nombresAliados[0]
            );

            if (selAliado == null) return;

            Heroe objetivoHeroe = vivos.get(0);
            for (int i = 0; i < nombresAliados.length; i++) {
                if (nombresAliados[i].equals(selAliado)) {
                    objetivoHeroe = vivos.get(i);
                    break;
                }
            }

            cuadroTexto.append("\n👉 Turno de: " + h.getNombre() + "\n");
            mostrarMenuAcciones(false);
            try {
                cuadroTexto.append(h.usarHabilidadGUI(hSel, heroes, objetivoHeroe, null));
            } catch (HabilidadSinMPException |
                     ObjetivoInvalidoException |
                     AccionNoPermitidaException ex) {
                cuadroTexto.append(ex.getMessage());
                mostrarMenuAcciones(true);
                return;
            }

            actualizarHeroes();
            finTurnoJugador();
            turnoEnemigo();

        } else {
            habilidadSeleccionada = hSel;
            modoActual = ModoAccion.HABILIDAD_ENEMIGO;
            cuadroTexto.append("\n" + h.getNombre() +
                    " prepara " + hSel.getNombre() +
                    ". Elige un enemigo como objetivo.\n");
            mostrarMenuAcciones(false);
        }
    }

    private void ejecutarHabilidadEnemigo(Heroe atacante,
                                          Habilidad hab,
                                          Enemigo objetivo,
                                          JPanel panelEnemigo) {

        if (objetivo == null || !objetivo.estaVivo()) {
            cuadroTexto.append("Ese enemigo ya está derrotado.\n");
            return;
        }

        cuadroTexto.append("\n👉 Turno de: " + atacante.getNombre() + "\n");
        try {
            cuadroTexto.append(atacante.usarHabilidadGUI(hab, heroes, null, objetivo));
        } catch (HabilidadSinMPException |
                 ObjetivoInvalidoException |
                 AccionNoPermitidaException ex) {
            cuadroTexto.append(ex.getMessage());
            mostrarMenuAcciones(true);
            return;
        }

        actualizarEnemigos();

        if (!objetivo.estaVivo()) {
            cuadroTexto.append("💥 " + objetivo.getNombre() +
                    " ha sido derrotado.\n");
            panelEnemigo.setVisible(false);
        }

        if (!hayVivos(enemigos)) {
            cuadroTexto.append("\n🏆 ¡HAS GANADO LA BATALLA!\n");
            musicaBatalla.parar();
            finalizarBatalla("Victoria del jugador (habilidad)");
            return;
        }

        actualizarHeroes();
        finTurnoJugador();
        turnoEnemigo();
    }

    // ================== TURNO DE ENEMIGO ==================

    private void turnoEnemigo() {

        if (!hayVivos(heroes)) {
            cuadroTexto.append("\n💀 ¡TU EQUIPO HA SIDO DERROTADO!\n");
            musicaBatalla.parar();
            finalizarBatalla("Derrota del jugador");
            return;
        }

        Enemigo enemigoAtaca = elegirEnemigoVivoAleatorio();
        if (enemigoAtaca == null) return;

        if (!procesarVeneno(enemigoAtaca)) {
            actualizarEnemigos();
            if (!hayVivos(enemigos)) {
                cuadroTexto.append("\n🏆 ¡HAS GANADO LA BATALLA!\n");
                musicaBatalla.parar();
                finalizarBatalla("Victoria del jugador (veneno)");
            } else {
                mostrarMenuAcciones(true);
                Heroe siguiente = obtenerHeroeActual();
                if (siguiente != null) resaltarHeroe(siguiente);
            }
            return;
        }

        Heroe heroeObjetivo = elegirHeroeVivoAleatorio();
        if (heroeObjetivo == null) return;

        cuadroTexto.append("\n⚠️ Turno del enemigo: " +
                enemigoAtaca.getNombre() + "\n");
        cuadroTexto.append(enemigoAtaca.accionAutomaticaTexto(heroeObjetivo));

        if (!heroeObjetivo.estaVivo()) {
            cuadroTexto.append("💀 " + heroeObjetivo.getNombre() +
                    " ha sido derrotado.\n");
        }

        if (!hayVivos(heroes)) {
            cuadroTexto.append("\n💀 ¡TU EQUIPO HA SIDO DERROTADO!\n");
            musicaBatalla.parar();
            finalizarBatalla("Derrota del jugador");
            return;
        }

        actualizarHeroes();
        actualizarEnemigos();

        if (hayVivos(heroes) && hayVivos(enemigos)) {
            turnoActual++;
            guardarEstadoActual();

            mostrarMenuAcciones(true);
            Heroe siguiente = obtenerHeroeActual();
            if (siguiente != null) resaltarHeroe(siguiente);
        }

        cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
    }

    private boolean procesarEstado(Heroe h) {
        if (h == null) return true;

        if (!procesarVeneno(h)) {
            actualizarHeroes();
            if (!hayVivos(heroes)) {
                cuadroTexto.append("\n💀 ¡TU EQUIPO HA SIDO DERROTADO!\n");
                deshabilitarTodo();
                musicaBatalla.parar();
            }
            return false;
        }

        procesarCuracion(h);

        if (h.getEstado() == null) return true;

        String nombre = h.getEstado().getNombre();

        if (nombre.equalsIgnoreCase("Sueño")) {
            int turnosRestantes = h.getEstado().getDuracion();
            cuadroTexto.append("\n😴 " + h.getNombre() +
                    " está dormido (" + turnosRestantes +
                    " turnos restantes).\n");

            h.getEstado().reducirDuracion();

            if (h.getEstado().terminado()) {
                cuadroTexto.append("✨ " + h.getNombre() +
                        " se despertará en su próximo turno.\n");
                h.setEstado(null);
            }

            return false;
        }

        return true;
    }

    private void finTurnoJugador() {
        actualizarHeroes();

        indiceHeroeActual = (indiceHeroeActual + 1) % heroes.size();

        Heroe siguiente = obtenerHeroeActual();
        if (siguiente != null) resaltarHeroe(siguiente);

        cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
    }

    // ================== UNDO / REDO ==================

    private EstadoBatalla tomarEstado() {
        EstadoBatalla est = new EstadoBatalla();

        est.heroes = new DatosPersonaje[heroes.size()];
        for (int i = 0; i < heroes.size(); i++) {
            Heroe h = heroes.get(i);
            DatosPersonaje d = new DatosPersonaje();
            d.vida = h.getVidaHp();
            d.mp = h.getMagiaMp();
            d.ataque = h.getAtaque();
            d.defensa = h.getDefensa();
            d.velocidad = h.getVelocidad();
            d.vive = h.estaVivo();
            d.protegido = h.isProtegido();

            if (h.getEstado() != null) {
                d.estadoNombre = h.getEstado().getNombre();
                d.estadoDuracion = h.getEstado().getDuracion();
            } else {
                d.estadoNombre = null;
                d.estadoDuracion = 0;
            }
            est.heroes[i] = d;
        }

        est.enemigos = new DatosPersonaje[enemigos.size()];
        for (int i = 0; i < enemigos.size(); i++) {
            Enemigo e = enemigos.get(i);
            DatosPersonaje d = new DatosPersonaje();
            d.vida = e.getVidaHp();
            d.mp = e.getMagiaMp();
            d.ataque = e.getAtaque();
            d.defensa = e.getDefensa();
            d.velocidad = e.getVelocidad();
            d.vive = e.estaVivo();
            d.protegido = e.isProtegido();

            if (e.getEstado() != null) {
                d.estadoNombre = e.getEstado().getNombre();
                d.estadoDuracion = e.getEstado().getDuracion();
            } else {
                d.estadoNombre = null;
                d.estadoDuracion = 0;
            }
            est.enemigos[i] = d;
        }

        est.indiceHeroeActual = this.indiceHeroeActual;

        return est;
    }

    private void aplicarEstado(EstadoBatalla est) {

        for (int i = 0; i < heroes.size() && i < est.heroes.length; i++) {
            Heroe h = heroes.get(i);
            DatosPersonaje d = est.heroes[i];

            h.setVidaHp(d.vida);
            h.setMagiaMp(d.mp);
            h.setAtaque(d.ataque);
            h.setDefensa(d.defensa);
            h.setVelocidad(d.velocidad);
            h.setVive(d.vive);
            h.setProtegido(d.protegido);

            if (d.estadoNombre != null) {
                h.setEstado(new Estado(d.estadoNombre, d.estadoDuracion));
            } else {
                h.setEstado(null);
            }
        }

        for (int i = 0; i < enemigos.size() && i < est.enemigos.length; i++) {
            Enemigo e = enemigos.get(i);
            DatosPersonaje d = est.enemigos[i];

            e.setVidaHp(d.vida);
            e.setMagiaMp(d.mp);
            e.setAtaque(d.ataque);
            e.setDefensa(d.defensa);
            e.setVelocidad(d.velocidad);
            e.setVive(d.vive);
            e.setProtegido(d.protegido);

            if (d.estadoNombre != null) {
                e.setEstado(new Estado(d.estadoNombre, d.estadoDuracion));
            } else {
                e.setEstado(null);
            }
        }

        this.indiceHeroeActual = est.indiceHeroeActual;

        actualizarHeroes();
        actualizarEnemigos();

        Heroe actual = obtenerHeroeActual();
        if (actual != null) resaltarHeroe(actual);

        cuadroTexto.append("\n⏮ Se ha restaurado una ronda.\n");
        cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
    }

    private void guardarEstadoActual() {
        EstadoBatalla est = tomarEstado();
        pilaUndo.addFirst(est);

        if (pilaUndo.size() > 2) pilaUndo.removeLast();

        pilaRedo.clear();
    }

    private void deshacerRonda() {
        if (pilaUndo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay más rondas para deshacer.",
                    "Deshacer",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        EstadoBatalla estadoActual = tomarEstado();
        pilaRedo.addFirst(estadoActual);

        EstadoBatalla est = pilaUndo.removeFirst();
        aplicarEstado(est);
    }

    private void rehacerRonda() {
        if (pilaRedo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay acciones para rehacer.",
                    "Rehacer",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        EstadoBatalla estadoActual = tomarEstado();
        pilaUndo.addFirst(estadoActual);

        EstadoBatalla est = pilaRedo.removeFirst();
        aplicarEstado(est);
    }

    // ================== GUARDAR PARTIDA ==================

    private void guardarPartida() {
        String nombre = JOptionPane.showInputDialog(
                this,
                "Escribe un nombre para la partida:",
                "Guardar partida",
                JOptionPane.PLAIN_MESSAGE
        );

        if (nombre == null) {
            cuadroTexto.append("\nGuardado cancelado.\n");
            return;
        }

        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes escribir un nombre para la partida.",
                    "Nombre inválido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            GuardarPartida.guardar(control, turnoActual, nombre);
            cuadroTexto.append("\n📜 Partida guardada como: " + nombre + "\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar la partida:\n" + ex.getMessage(),
                    "Error al guardar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================== FINALIZAR BATALLA ==================

    private void finalizarBatalla(String resultado) {
        if (batallaFinalizada) return;
        batallaFinalizada = true;

        try {
            HistorialBatallas.registrar(resultado, control.getHeroes(), control.getEnemigos());

            if (resultado.toLowerCase().contains("derrota")) {
                new VentanaDerrota();
            } else {
                new VentanaVictoria();
            }

            control.reiniciarPartida();
            musicaBatalla.parar();
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al finalizar batalla:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== INVENTARIO ==================

    private void abrirInventario() {
        Heroe h = obtenerHeroeActual();
        if (h == null) {
            cuadroTexto.append("\nNo quedan héroes vivos.\n");
            return;
        }

        HashMap<String, Integer> inv = h.getInventario();

        if (inv.isEmpty()) {
            cuadroTexto.append("\nNo tienes objetos.\n");
            return;
        }

        Object[] opciones = inv.keySet().toArray();

        String elegido = (String) JOptionPane.showInputDialog(
                this,
                "Elige un objeto:",
                "Inventario",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (elegido == null) return;

        ArrayList<Heroe> vivos = new ArrayList<>();
        for (Heroe ally : heroes) if (ally.estaVivo()) vivos.add(ally);

        Heroe objetivo = vivos.get(0);

        boolean esEspecial = elegido.equalsIgnoreCase("talismán del valor")
                || elegido.equalsIgnoreCase("hacha oxidada gigante")
                || elegido.equalsIgnoreCase("amuleto de maná arcano")
                || elegido.equalsIgnoreCase("bendición divina");

        if (!esEspecial) {
            String[] nombres = vivos.stream()
                    .map(a -> a.getNombre() +
                            " (HP: " + a.getVidaHp() + ")")
                    .toArray(String[]::new);

            String sel = (String) JOptionPane.showInputDialog(
                    this,
                    "¿A quién lo aplicas?",
                    "Objetivo",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    nombres,
                    nombres[0]
            );

            if (sel == null) return;

            for (int i = 0; i < nombres.length; i++) {
                if (nombres[i].equals(sel)) {
                    objetivo = vivos.get(i);
                }
            }
        }

        String resultado = h.usarItem(elegido, objetivo, heroes);
        cuadroTexto.append("\n" + resultado + "\n");

        actualizarHeroes();
        finTurnoJugador();
        turnoEnemigo();
    }

}
