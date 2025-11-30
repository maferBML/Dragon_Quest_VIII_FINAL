package vista;

import modelo.Musica;
import controlador.ControlJuego;

import javax.swing.*;
import java.awt.*;

public class VentanaDerrota extends JFrame {

    private Image fondo;
    private Musica musica = new Musica();

    public VentanaDerrota() {

        setTitle("Derrota...");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fondo = new ImageIcon(getClass().getResource("/foticos/derrota.jpeg")).getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(null);
        add(panel);

        // ===== TÍTULO =====
        JLabel titulo = new JLabel("DERROTADO...", JLabel.LEFT);
        titulo.setFont(new Font("Serif", Font.BOLD, 52));
        titulo.setForeground(Color.BLACK);
        titulo.setBounds(40, 40, 800, 60);
        panel.add(titulo);

        // ===== MENSAJE TRISTE =====
        JLabel mensaje = new JLabel(
                "<html><b>Aunque caíste en batalla,<br>"
                        + "la esperanza no ha muerto.<br>"
                        + "Tu aventura apenas empieza.</b></html>"
        );
        mensaje.setFont(new Font("Serif", Font.BOLD, 26));
        mensaje.setForeground(Color.BLACK);
        mensaje.setBounds(40, 130, 700, 120);
        panel.add(mensaje);

        // ===== BOTÓN VOLVER =====
        JLabel btnMenu = new JLabel("↩  Volver al menú");
        btnMenu.setFont(new Font("Serif", Font.BOLD, 34));
        btnMenu.setForeground(Color.BLACK);
        btnMenu.setBounds(40, 450, 400, 60);
        panel.add(btnMenu);

        animacionHoverDeslizar(btnMenu);

        btnMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                musica.parar();
                VentanaInicio inicio = new VentanaInicio(new ControlJuego());
                inicio.setVisible(true);
                dispose();
            }
        });


        setVisible(true);

        musica.reproducirLoop("/sonidos/derrota.wav");
    }

    // ===== ANIMACIÓN =====
    private void animacionHoverDeslizar(JLabel lbl) {
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                new Thread(() -> {
                    for (int i = 0; i < 8; i++) {
                        lbl.setLocation(lbl.getX() + 2, lbl.getY());
                        try { Thread.sleep(8); } catch (Exception ignored) {}
                    }
                }).start();
            }

            @Override public void mouseExited(java.awt.event.MouseEvent e) {
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