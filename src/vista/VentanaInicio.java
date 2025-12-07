package vista;

import modelo.Musica;
import controlador.ControlJuego;

import javax.swing.*;
import java.awt.*;

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

        // ================= TÍTULO CLÁSICO =================
        JLabel titulo = new JLabel("DRAGON QUEST VIII", JLabel.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 60));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(0, 80, getWidth(), 50);
        panel.add(titulo);

        // SUBTÍTULO (opcional)
        JLabel subtitulo = new JLabel("El Reino de Trodain", JLabel.CENTER);
        subtitulo.setFont(new Font("Serif", Font.BOLD, 28));
        subtitulo.setForeground(Color.WHITE);
        subtitulo.setBounds(0, 140, getWidth(), 40);
        panel.add(subtitulo);

        // ================= BOTONES RPG =================
        JLabel btnStart = crearBotonMenu("▶️  Empezar aventura");
        btnStart.setBounds(50, 250, 400, 60);
        panel.add(btnStart);

        JLabel btnSalir = crearBotonMenu("✖  Salir");
        btnSalir.setBounds(50, 330, 400, 60);
        panel.add(btnSalir);

        // Acción iniciar
        btnStart.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                musica.parar();
                control.reiniciarPartida();                      // 💥 PARAR MÚSICA DEL MENÚ
                new VentanaBatalla(control);          // abrir batalla
                dispose();
            }
        });

        // Acción salir
        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.exit(0);
            }
        });

        // Animación hover (solo deslizar)
        animacionHoverDeslizar(btnStart);
        animacionHoverDeslizar(btnSalir);

        setVisible(true);

        // 💥 ESTA ES LA ÚNICA PARTE DONDE DEBE IR LA MÚSICA DEL MENÚ
        musica.reproducirLoop("/sonidos/intro.wav");
    }

    // ====================== BOTÓN ESTILO RPG ======================
    private JLabel crearBotonMenu(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Serif", Font.BOLD, 32));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        return lbl;
    }

    // ====================== HOVER DESLIZANTE ======================
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