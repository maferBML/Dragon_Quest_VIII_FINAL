package vista;

import modelo.Aventurero;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class VentanaGremio extends JFrame {

    private Queue<Aventurero> colaAventureros = new LinkedList<>();
    private JTextArea areaTexto;
    private Image imagenFondo;

    public VentanaGremio() {

        setTitle("Gremio de Aventureros");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== FONDO PRINCIPAL =====
        imagenFondo = new ImageIcon(getClass().getResource("/foticos/Gremio.png")).getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(null);
        add(panel);

        // ===== TÍTULO MEDIEVAL =====
        JLabel titulo = new JLabel("Gremio de Aventureros", JLabel.CENTER);
        titulo.setFont(new Font("Old English Text MT", Font.BOLD, 44));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(0, 20, getWidth(), 60);
        panel.add(titulo);

        // =============================================================
        // ======================= MARCO ESTÉTICO ======================
        // =============================================================

        JPanel marco = new JPanel();
        marco.setBounds(380, 100, 380, 350);
        marco.setLayout(new BorderLayout());
        marco.setBackground(new Color(230, 215, 175)); // tono pergamino suave
        marco.setBorder(BorderFactory.createLineBorder(new Color(100, 70, 30), 8)); // marco madera
        panel.add(marco);

        // ===== ÁREA DE TEXTO =====
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Serif", Font.PLAIN, 18));
        areaTexto.setForeground(Color.BLACK);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setOpaque(false);

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        marco.add(scroll, BorderLayout.CENTER);

        // =============================================================
        // ============================ BOTONES =========================
        // =============================================================

        JButton btnRegistrar = crearBoton("Registrar aventurero");
        JButton btnAtender = crearBoton("Atender siguiente");
        JButton btnVerSiguiente = crearBoton("Ver siguiente");
        JButton btnVerCola = crearBoton("Ver cola completa");
        JButton btnVaciar = crearBoton("Vaciar cola");
        JButton btnSalir = crearBoton("Cerrar");

        btnRegistrar.setBounds(40, 120, 300, 50);
        btnAtender.setBounds(40, 180, 300, 50);
        btnVerSiguiente.setBounds(40, 240, 300, 50);
        btnVerCola.setBounds(40, 300, 300, 50);
        btnVaciar.setBounds(40, 360, 300, 50);
        btnSalir.setBounds(40, 420, 300, 50);

        panel.add(btnRegistrar);
        panel.add(btnAtender);
        panel.add(btnVerSiguiente);
        panel.add(btnVerCola);
        panel.add(btnVaciar);
        panel.add(btnSalir);

        // =============================================================
        // ========================== EVENTOS ===========================
        // =============================================================

        btnRegistrar.addActionListener(e -> mostrarPergaminoRegistro());
        btnAtender.addActionListener(e -> atenderSiguiente());
        btnVerSiguiente.addActionListener(e -> mostrarSiguiente());
        btnVerCola.addActionListener(e -> mostrarCola());
        btnVaciar.addActionListener(e -> vaciarCola());
        btnSalir.addActionListener(e -> dispose());

        setVisible(true);
    }

    // =============================================================
    // =============== VENTANA EMERGENTE CON PERGAMINO ==============
    // =============================================================

    private void mostrarPergaminoRegistro() {

        Image pergamino = new ImageIcon(getClass().getResource("/foticos/Pergamino.png")).getImage();

        JPanel fondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(pergamino, 0, 0, getWidth(), getHeight(), this);
            }
        };

        fondo.setPreferredSize(new Dimension(420, 300));
        fondo.setLayout(null);

        Font fuenteTexto = new Font("Serif", Font.BOLD, 22);

        JLabel lbl1 = new JLabel("Nombre:");
        lbl1.setBounds(50, 80, 150, 30);
        lbl1.setFont(fuenteTexto);
        lbl1.setForeground(new Color(60, 40, 20));

        JLabel lbl2 = new JLabel("Nivel:");
        lbl2.setBounds(50, 140, 150, 30);
        lbl2.setFont(fuenteTexto);
        lbl2.setForeground(new Color(60, 40, 20));

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(160, 80, 200, 32);
        txtNombre.setBackground(new Color(245, 230, 200));
        txtNombre.setForeground(new Color(50, 30, 10));
        txtNombre.setBorder(BorderFactory.createLineBorder(new Color(100, 70, 30), 3));
        txtNombre.setFont(new Font("Serif", Font.PLAIN, 20));

        JTextField txtNivel = new JTextField();
        txtNivel.setBounds(160, 140, 80, 32);
        txtNivel.setBackground(new Color(245, 230, 200));
        txtNivel.setForeground(new Color(50, 30, 10));
        txtNivel.setBorder(BorderFactory.createLineBorder(new Color(100, 70, 30), 3));
        txtNivel.setFont(new Font("Serif", Font.PLAIN, 20));

        fondo.add(lbl1);
        fondo.add(txtNombre);
        fondo.add(lbl2);
        fondo.add(txtNivel);

        // PERSONALIZACIÓN DE BOTONES
        UIManager.put("OptionPane.background", new Color(230, 210, 170));
        UIManager.put("Panel.background", new Color(230, 210, 170));

        UIManager.put("Button.background", new Color(200, 180, 120));
        UIManager.put("Button.font", new Font("Serif", Font.BOLD, 20));
        UIManager.put("Button.foreground", new Color(60, 40, 20));
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(90, 60, 20), 3));

        int r = JOptionPane.showConfirmDialog(
                this,
                fondo,
                "Registrar Aventurero",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (r == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                int nivel = Integer.parseInt(txtNivel.getText().trim());

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.");
                    return;
                }

                Aventurero av = new Aventurero(nombre, nivel);
                colaAventureros.add(av);

                areaTexto.append("Registrado: " + av + "\n");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nivel inválido.");
            }
        }
    }

    // =============================================================
    // ================= BOTÓN ESTÉTICA RPG ==========================
    // =============================================================

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Serif", Font.BOLD, 22));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(200, 180, 120));
        btn.setBorder(BorderFactory.createLineBorder(new Color(70, 50, 30), 3));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =============================================================
    // ========================= LÓGICA =============================
    // =============================================================

    private void atenderSiguiente() {
        Aventurero atendido = colaAventureros.poll();

        if (atendido == null) {
            areaTexto.append("No hay aventureros en espera.\n");
        } else {
            areaTexto.append("Atendiendo a: " + atendido + "\n");
        }
    }

    private void mostrarSiguiente() {
        Aventurero siguiente = colaAventureros.peek();

        if (siguiente == null) {
            areaTexto.append("No hay aventureros en la fila.\n");
        } else {
            areaTexto.append("Siguiente en turno: " + siguiente + "\n");
        }
    }

    private void mostrarCola() {
        if (colaAventureros.isEmpty()) {
            areaTexto.append("La cola está vacía.\n");
        } else {
            areaTexto.append("Aventureros en espera:\n");
            for (Aventurero av : colaAventureros) {
                areaTexto.append("- " + av + "\n");
            }
        }
    }

    private void vaciarCola() {
        colaAventureros.clear();
        areaTexto.append("La cola ha sido vaciada.\n");
    }
}
