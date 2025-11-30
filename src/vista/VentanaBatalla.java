package vista;

import modelo.Musica;
import controlador.ControlJuego;
import modelo.Enemigo;
import modelo.Habilidad;
import modelo.Heroe;
import modelo.Personaje;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VentanaBatalla extends JFrame {

    private enum ModoAccion { NINGUNO, ATACAR, HABILIDAD_ENEMIGO }

    private ControlJuego control;

    private Image fondo;
    private JTextArea cuadroTexto;
    private JPanel panelHeroes, panelEnemigos;
    private JPanel panelInferior;
    private JPanel panelMenuAcciones;   // menú tipo Pokémon (derecha)

    private ArrayList<Heroe> heroes;
    private ArrayList<Enemigo> enemigos;

    // Héroes: texto + imagen + iconos
    private ArrayList<JLabel> labelsHeroes = new ArrayList<>();
    private ArrayList<JLabel> labelsImagenHeroes = new ArrayList<>();
    private ArrayList<ImageIcon> iconosNormalesHeroes = new ArrayList<>();
    private ArrayList<ImageIcon> iconosActivosHeroes = new ArrayList<>();

    // Enemigos: panel + label de texto (nombre/HP)
    private ArrayList<JPanel> panelesEnemigos = new ArrayList<>();
    private ArrayList<JLabel> labelsEnemigos = new ArrayList<>();

    private int indiceHeroeActual = 0;
    private Random random = new Random();
    private ModoAccion modoActual = ModoAccion.NINGUNO;

    private JButton btnAtacar;
    private JButton btnDefender;
    private JButton btnHabilidad;
    private JButton btnSalir;

    private Habilidad habilidadSeleccionada = null;

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

        // 🎵 AQUÍ VA — SOLO UNA VEZ
        musicaBatalla.reproducirLoop("/sonidos/batalla.wav");

        construirInterfaz();

        Heroe actual = obtenerHeroeActual();
        if (actual != null) {
            cuadroTexto.append("\nTurno inicial de: " + actual.getNombre() + "\n");
            resaltarHeroe(actual);
        }

        setVisible(true);
    }

    private Musica musicaBatalla = new Musica();

    // ===================== INTERFAZ =====================

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

        // ===== HÉROES (arriba) =====
        panelHeroes = new JPanel(new GridLayout(1, heroes.size(), 15, 15));
        panelHeroes.setOpaque(false);
        panelHeroes.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 50));
        for (Heroe h : heroes) {
            agregarHeroe(h);
        }

        // ===== ENEMIGOS (centro) =====
        panelEnemigos = new JPanel(new GridLayout(1, enemigos.size(), 15, 15));
        panelEnemigos.setOpaque(false);
        panelEnemigos.setBorder(BorderFactory.createEmptyBorder(50, 50, 30, 50));
        for (Enemigo e : enemigos) {
            agregarEnemigo(e);
        }

        // ===== TEXTO BATALLA (abajo izquierda) =====
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

        // ===== MENÚ ACCIONES (abajo derecha, estilo Pokémon) =====
        panelMenuAcciones = new JPanel();
        panelMenuAcciones.setBackground(new Color(10, 10, 30));
        panelMenuAcciones.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panelMenuAcciones.setLayout(new GridLayout(4, 1, 5, 5));
        panelMenuAcciones.setPreferredSize(new Dimension(230, 0));  // ancho del menú

        btnAtacar = crearBoton("Atacar");
        btnDefender = crearBoton("Defender");
        btnHabilidad = crearBoton("Habilidad");
        btnSalir = crearBoton("Salir");

        // === listeners ===
        btnAtacar.addActionListener(e -> {
            Heroe h = obtenerHeroeActual();
            if (h == null) {
                cuadroTexto.append("\nNo quedan héroes vivos.\n");
                return;
            }
            modoActual = ModoAccion.ATACAR;
            habilidadSeleccionada = null;
            cuadroTexto.append("\n" + h.getNombre() + " se prepara para atacar. Elige un enemigo.\n");
            mostrarMenuAcciones(false);
        });

        btnDefender.addActionListener(e -> {
            Heroe h = obtenerHeroeActual();
            if (h == null) {
                cuadroTexto.append("\nNo quedan héroes vivos.\n");
                return;
            }
            cuadroTexto.append("\n👉 Turno de: " + h.getNombre() + "\n");
            cuadroTexto.append(h.defenderTexto());
            mostrarMenuAcciones(false);
            finTurnoJugador();
            turnoEnemigo();
        });

        btnHabilidad.addActionListener(e -> manejarHabilidad());

        btnSalir.addActionListener(e -> System.exit(0));

        panelMenuAcciones.add(btnAtacar);
        panelMenuAcciones.add(btnDefender);
        panelMenuAcciones.add(btnHabilidad);
        panelMenuAcciones.add(btnSalir);

        // ===== PANEL INFERIOR: texto (CENTER) + menú (EAST) =====
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(10, 10, 30));
        panelInferior.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        panelInferior.add(panelTexto, BorderLayout.CENTER);
        panelInferior.add(panelMenuAcciones, BorderLayout.EAST);

        // ===== ARMAR TODO =====
        panelFondo.add(panelHeroes, BorderLayout.NORTH);
        panelFondo.add(panelEnemigos, BorderLayout.CENTER);
        panelFondo.add(panelInferior, BorderLayout.SOUTH);

        add(panelFondo);
    }

    // ===================== AUXILIARES UI =====================

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
        else if (nombre.equalsIgnoreCase("Dragón") || nombre.equalsIgnoreCase("Dragon")) archivo = "/foticos/dragon.png";
        else if (nombre.equalsIgnoreCase("Esqueleto")) archivo = "/foticos/esqueleto.png";
        return archivo == null ? null : new ImageIcon(getClass().getResource(archivo));
    }

    // ===== HEROES =====
    private void agregarHeroe(Heroe h) {
        JPanel panelHeroe = new JPanel(new GridBagLayout());
        panelHeroe.setOpaque(true);
        panelHeroe.setBackground(new Color(20, 20, 50));
        panelHeroe.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panelHeroe.setPreferredSize(new Dimension(280, 90));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Imagen
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

        // Texto
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

    // ===== ENEMIGOS: imagen + marco clicable =====
    private void agregarEnemigo(Enemigo e) {

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);

        JLayeredPane capa = new JLayeredPane();

        // ===== TAMAÑOS POR ENEMIGO (ajustados para que no se corten) =====
        int w = 140, h = 120, offsetY = 45;

        switch (e.getNombre().toLowerCase()) {
            case "slime":
                w = 80;  h = 80;  offsetY = 70;  // pequeño y abajo
                break;
            case "goblin":
                w = 120; h = 120; offsetY = 40;
                break;
            case "dragón":
            case "dragon":
                w = 130; h = 130; offsetY = 35;  // más grande pero cabe completo
                break;
            case "esqueleto":
                w = 120; h = 120; offsetY = 40;
                break;
        }

        int panelW = w + 60;
        int panelH = 170; // altura fija para que no se recorte en el GridLayout

        capa.setPreferredSize(new Dimension(panelW, panelH));

        // ===== IMAGEN =====
        ImageIcon icon = iconoEnemigoPorNombre(e.getNombre());
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }

        JLabel lblImg = new JLabel(icon);
        int imgX = (panelW - w) / 2;
        lblImg.setBounds(imgX, offsetY, w, h);
        capa.add(lblImg, JLayeredPane.DEFAULT_LAYER);

        // ===== TEXTO ARRIBA =====
        JLabel textoArriba = new JLabel(
            "<html><center>HP: " + e.getVidaHp() + "</center></html>",
            JLabel.CENTER
    );

        textoArriba.setForeground(e.esMiniJefe() ? Color.ORANGE : Color.RED);
        textoArriba.setFont(new Font("Serif", Font.BOLD, 16));

        // AQUÍ SUBES / BAJAS EL TEXTO:
        //        setBounds(x, y, ancho, alto)
        // Si quieres el texto más arriba, baja el número de 'y'.
        // Si lo quieres más abajo, súbelo.
        textoArriba.setBounds(imgX - 10,160, w + 20, 40);

        capa.add(textoArriba, JLayeredPane.PALETTE_LAYER);

        // ===== MARCO =====
        JPanel marco = new JPanel();
        marco.setOpaque(false);
        marco.setBorder(BorderFactory.createLineBorder(
                e.esMiniJefe() ? Color.ORANGE : Color.RED, 3
        ));
        marco.setBounds(
                imgX - 10,          // un poquito más ancho que la imagen
                offsetY - 10,       // empieza justo encima de la imagen
                w + 25,
                h + 15              // termina apenas bajo los pies
        );
        capa.add(marco, JLayeredPane.PALETTE_LAYER);

        labelsEnemigos.add(textoArriba);
        panelesEnemigos.add(contenedor);

        // CLICK
        capa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                enemigoClicado(e, contenedor);
            }
        });

        contenedor.add(capa, BorderLayout.CENTER);
        panelEnemigos.add(contenedor);
    }

    // ===================== TEXTO INICIAL JEFE =====================

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

    // ===================== LÓGICA DE TURNO =====================

    private Heroe obtenerHeroeActual() {
        int intentos = 0;
        while (intentos < heroes.size()) {
            Heroe h = heroes.get(indiceHeroeActual);
            if (h.estaVivo()) return h;
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
                lbl.setText("<html><center><b>" + h.getNombre() + "</b><br>HP: "
                        + h.getVidaHp() + "<br>MP: " + h.getMagiaMp() + "</center></html>");
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
                lbl.setText(
                    "<html><center>HP: " + e.getVidaHp() + "</center></html>"
                );

            }
        }
        panelEnemigos.revalidate();
        panelEnemigos.repaint();
    }

    private void deshabilitarTodo() {
        for (JPanel p : panelesEnemigos) {
            p.setEnabled(false);
        }
        btnAtacar.setEnabled(false);
        btnDefender.setEnabled(false);
        btnHabilidad.setEnabled(false);
        btnSalir.setEnabled(false);
    }

    // click en un enemigo (imagen/marco)
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
        cuadroTexto.append(atacante.atacarTexto(objetivo));
        actualizarEnemigos();

        if (!objetivo.estaVivo()) {
            cuadroTexto.append("💥 " + objetivo.getNombre() + " ha sido derrotado.\n");
            panelEnemigo.setVisible(false);
        }

        if (!hayVivos(enemigos)) {
            cuadroTexto.append("\n🏆 ¡HAS GANADO LA BATALLA!\n");
            deshabilitarTodo();
            actualizarHeroes();
            cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
            musicaBatalla.parar();
            new VentanaVictoria();
            dispose();

            return;
        }

        finTurnoJugador();
        turnoEnemigo();
    }

    // === HABILIDADES ===
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
            nombres[i] = (i + 1) + ". " + hab.getNombre() + " (MP: " + hab.getCosteMp() + ")";
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
                nombresAliados[i] = ha.getNombre() + " (HP: " + ha.getVidaHp() + ")";
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
            cuadroTexto.append(h.usarHabilidadGUI(hSel, heroes, objetivoHeroe, null));

            actualizarHeroes();
            finTurnoJugador();
            turnoEnemigo();

        } else {
            habilidadSeleccionada = hSel;
            modoActual = ModoAccion.HABILIDAD_ENEMIGO;
            cuadroTexto.append("\n" + h.getNombre() + " prepara " + hSel.getNombre()
                    + ". Elige un enemigo como objetivo.\n");
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
        cuadroTexto.append(atacante.usarHabilidadGUI(hab, heroes, null, objetivo));
        actualizarEnemigos();

        if (!objetivo.estaVivo()) {
            cuadroTexto.append("💥 " + objetivo.getNombre() + " ha sido derrotado.\n");
            panelEnemigo.setVisible(false);
        }

        if (!hayVivos(enemigos)) {
            cuadroTexto.append("\n🏆 ¡HAS GANADO LA BATALLA!\n");
            deshabilitarTodo();
            actualizarHeroes();
            cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
            musicaBatalla.parar();
            new VentanaVictoria();
            dispose();
            return;
        }

        actualizarHeroes();
        finTurnoJugador();
        turnoEnemigo();
    }

    private void turnoEnemigo() {
        if (!hayVivos(enemigos) || !hayVivos(heroes)) return;

        Enemigo enemigoAtaca = elegirEnemigoVivoAleatorio();
        Heroe heroeObjetivo = elegirHeroeVivoAleatorio();
        if (enemigoAtaca == null || heroeObjetivo == null) return;

        cuadroTexto.append("\n⚠️ Turno del enemigo: " + enemigoAtaca.getNombre() + "\n");
        cuadroTexto.append(enemigoAtaca.accionAutomaticaTexto(heroeObjetivo));

        if (!heroeObjetivo.estaVivo()) {
            cuadroTexto.append("💀 " + heroeObjetivo.getNombre() + " ha sido derrotado.\n");
        }

        if (!hayVivos(heroes)) {
            cuadroTexto.append("\n💀 ¡TU EQUIPO HA SIDO DERROTADO!\n");
            deshabilitarTodo();
            musicaBatalla.parar();
            new VentanaDerrota();
            dispose();
            return;

        }

        actualizarHeroes();
        actualizarEnemigos();

        if (hayVivos(heroes) && hayVivos(enemigos)) {
            mostrarMenuAcciones(true);
            Heroe siguiente = obtenerHeroeActual();
            if (siguiente != null) resaltarHeroe(siguiente);
        }

        cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
    }

    private void finTurnoJugador() {
        actualizarHeroes();
        indiceHeroeActual = (indiceHeroeActual + 1) % heroes.size();
        Heroe siguiente = obtenerHeroeActual();
        if (siguiente != null) resaltarHeroe(siguiente);
        cuadroTexto.setCaretPosition(cuadroTexto.getText().length());
    }
}