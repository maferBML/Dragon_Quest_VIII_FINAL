package vista;

import utils.GuardarPartida;
import utils.GuardarPartida.PartidaCargada;
import controlador.ControlJuego;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VentanaCargarPartida extends JFrame {

    private JList<String> listaPartidas;
    private DefaultListModel<String> modeloLista;
    private JFrame ventanaInicio;

    private static final String RUTA_CARPETA = "partidas";

    public VentanaCargarPartida(JFrame ventanaInicio) {
        this.ventanaInicio = ventanaInicio;

        setTitle("Cargar partida");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Al cerrar, volver al menú principal
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                ventanaInicio.setVisible(true);
            }
        });

        construirInterfaz();
        cargarArchivosPartida();

        setVisible(true);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Selecciona una partida guardada:", JLabel.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>();
        listaPartidas = new JList<>(modeloLista);
        listaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(listaPartidas);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCargar = new JButton("Cargar");
        JButton btnCancelar = new JButton("Cancelar");

        btnCargar.addActionListener(e -> cargarSeleccionada());

        btnCancelar.addActionListener(e -> {
            ventanaInicio.setVisible(true);
            dispose();
        });

        panelBotones.add(btnCargar);
        panelBotones.add(btnCancelar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void cargarArchivosPartida() {
        modeloLista.clear();

        File carpeta = new File(RUTA_CARPETA);
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            return;
        }

        File[] archivos = carpeta.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (archivos == null || archivos.length == 0) {
            return;
        }

        for (File f : archivos) {
            modeloLista.addElement(f.getName());
        }
    }

    private void cargarSeleccionada() {
        String seleccionado = listaPartidas.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes seleccionar una partida.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        PartidaCargada partida = GuardarPartida.cargar(seleccionado);
        if (partida == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar la partida seleccionada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        ControlJuego ctrl = partida.getControlador();
        int turno = partida.getTurno();

        // Abrir la batalla cargada
        new VentanaBatalla(ctrl, turno);

        ventanaInicio.dispose();
        dispose();
    }
}
