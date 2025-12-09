package vista;

import modelo.Musica;
import controlador.ControlJuego;
import vista.VentanaCargarPartida;
import vista.VentanaGremio;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class VentanaInicio extends JFrame {

    private Image imagenFondo;
    private ControlJuego control;
    private Musica musica = new Musica();

    public VentanaInicio(ControlJuego control) {
        this.control = control;

        setTitle("Reino de Trodain - Dragon Quest RPG");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        imagenFondo = new ImageIcon(getClass().getResource("/foticos/bosque.jpg")).getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagenFondo != null) {
                    g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        panel.setLayout(null);
        add(panel);

        JLabel titulo = new JLabel("DRAGON QUEST VIII", JLabel.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 60));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(0, 80, getWidth(), 50);
        panel.add(titulo);

        JLabel subtitulo = new JLabel("El Reino de Trodain", JLabel.CENTER);
        subtitulo.setFont(new Font("Serif", Font.BOLD, 28));
        subtitulo.setForeground(Color.WHITE);
        subtitulo.setBounds(0, 140, getWidth(), 40);
        panel.add(subtitulo);

        // ================== BOTONES ==================

        JLabel btnStart = crearBotonMenu("▶  Empezar aventura");
        btnStart.setBounds(50, 250, 400, 60);
        panel.add(btnStart);

        JLabel btnCargar = crearBotonMenu("📂  Cargar Partida");
        btnCargar.setBounds(50, 300, 400, 60);
        panel.add(btnCargar);

        JLabel btnHistorial = crearBotonMenu("📜  Historial de batallas");
        btnHistorial.setBounds(50, 350, 400, 60);
        panel.add(btnHistorial);

        JLabel btnGremio = crearBotonMenu("⚔️  Gremio de Aventureros");
        btnGremio.setBounds(50, 400, 400, 60);
        panel.add(btnGremio);

        JLabel btnSalir = crearBotonMenu("✖  Salir");
        btnSalir.setBounds(50, 450, 400, 60);
        panel.add(btnSalir);

        // ========== ACCIONES ==========
        btnStart.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                musica.parar();
                control.reiniciarPartida();
                new VentanaBatalla(control);
                dispose();
            }
        });

        btnCargar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new VentanaCargarPartida(VentanaInicio.this).setVisible(true);
            }
        });

        btnHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    String texto = Files.readString(Path.of("historial_batallas.txt"));
                    JTextArea area = new JTextArea(25, 45);
                    area.setEditable(false);
                    area.setFont(new Font("Monospaced", Font.PLAIN, 14));
                    area.setText(texto);

                    JScrollPane scroll = new JScrollPane(area);

                    JOptionPane.showMessageDialog(
                            VentanaInicio.this,
                            scroll,
                            "📜 Historial de Batallas",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            VentanaInicio.this,
                            "Aún no hay historial disponible.",
                            "Historial",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        btnGremio.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new VentanaGremio().setVisible(true);
            }
        });

        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.exit(0);
            }
        });

        animacionHoverDeslizar(btnStart);
        animacionHoverDeslizar(btnCargar);
        animacionHoverDeslizar(btnHistorial);
        animacionHoverDeslizar(btnGremio);
        animacionHoverDeslizar(btnSalir);

        setVisible(true);

        musica.reproducirLoop("/sonidos/intro.wav");
    }

    private JLabel crearBotonMenu(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Serif", Font.BOLD, 32));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private void animacionHoverDeslizar(JLabel lbl) {
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                new Thread(() -> {
                    for (int i = 0; i < 8; i++) {
                        lbl.setLocation(lbl.getX() + 2, lbl.getY());
                        try { Thread.sleep(8); } catch (Exception ignored) {}
                    }
                }).start();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                new Thread(() -> {
                    for (int i = 0; i < 8; i++) {
                        lbl.setLocation(lbl.getX() - 2, lbl.getY());
                        try { Thread.sleep(8); } catch (Exception ignored) {}
                    }
                }).start();
            }
        });
    }
}
