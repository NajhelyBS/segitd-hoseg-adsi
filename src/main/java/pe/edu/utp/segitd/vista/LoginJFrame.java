package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.LoginControlador;
import pe.edu.utp.segitd.servicio.ServicioException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Pantalla de autenticación (RF-01 / RNF-02). Valida contra Supabase con
 * SHA-256 + salt y aplica el bloqueo temporal tras 3 intentos fallidos.
 */
public class LoginJFrame extends JFrame {

    private final LoginControlador controlador = new LoginControlador();

    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoPassword = new JPasswordField(15);
    private final JLabel etiquetaMensaje = new JLabel(" ");
    private final JButton botonIngresar = new JButton("Ingresar");

    private final Color COLOR_ERROR = new Color(0xB0, 0x00, 0x20);
    private final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 22);
    private final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 12);


    public LoginJFrame() {
        super("SEGITD-HÖSÉG · Ingreso");
        construirInterfaz();
        botonIngresar.addActionListener(e -> intentarIngresar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

        private void construirInterfaz() {
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.setPreferredSize(new Dimension(680, 400));

        // Panel izquierdo
        JPanel panelIzquierdo = new PanelFondoImagen("/images/login-banner.jpg");
        panelIzquierdo.setBorder(new EmptyBorder(40, 30, 40, 30));

        JLabel labelLogo = new JLabel("🔥");
        labelLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        labelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelLogo.setForeground(EstilosHoseg.FONDO_VENTANA);

        JLabel txtHoseg = new JLabel("HÖSÉG");
        txtHoseg.setFont(new Font("SansSerif", Font.BOLD, 28));
        txtHoseg.setForeground(Color.WHITE);
        txtHoseg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txtEslogan = new JLabel("Sostenibilidad & Aventura");
        txtEslogan.setFont(FUENTE_SUBTITULO);
        txtEslogan.setForeground(new Color(0xCE, 0xD4, 0xDA));
        txtEslogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelIzquierdo.add(Box.createVerticalGlue());
        panelIzquierdo.add(labelLogo);
        panelIzquierdo.add(Box.createVerticalStrut(15));
        panelIzquierdo.add(txtHoseg);
        panelIzquierdo.add(Box.createVerticalStrut(5));
        panelIzquierdo.add(txtEslogan);
        panelIzquierdo.add(Box.createVerticalGlue());

        // Panel derecho
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(EstilosHoseg.FONDO_VENTANA);
        panelDerecho.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titulo = new JLabel("Back office");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(EstilosHoseg.TEXTO_MAIN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelDerecho.add(titulo, gbc);

        JLabel subtitulo = new JLabel("Ingresa tus credenciales para continuar");
        subtitulo.setFont(FUENTE_SUBTITULO);
        subtitulo.setForeground(EstilosHoseg.GRIS_TEXTO);
        gbc.gridy = 1;
        panelDerecho.add(subtitulo, gbc);

        gbc.gridy = 2;
        panelDerecho.add(Box.createVerticalStrut(8), gbc);

        JLabel lblUsuario = new JLabel("USUARIO");
        lblUsuario.setFont(EstilosHoseg.FUENTE_LABEL);
        lblUsuario.setForeground(EstilosHoseg.GRIS_TEXTO);
        gbc.gridy = 3;
        panelDerecho.add(lblUsuario, gbc);

        EstilosHoseg.estilizarInput(campoUsuario);
        campoUsuario.putClientProperty("JTextField.placeholderText", "Ej. admin, encargado o gestor");
        gbc.gridy = 4;
        panelDerecho.add(campoUsuario, gbc);

        JLabel lblPassword = new JLabel("CONTRASEÑA");
        lblPassword.setFont(EstilosHoseg.FUENTE_LABEL);
        lblPassword.setForeground(EstilosHoseg.GRIS_TEXTO);
        gbc.gridy = 5;
        panelDerecho.add(lblPassword, gbc);

        EstilosHoseg.estilizarInput(campoPassword);
        campoPassword.putClientProperty("JTextField.placeholderText", "••••••••");
        campoPassword.putClientProperty("FlatLaf.style", "showRevealButton: true");
        gbc.gridy = 6;
        panelDerecho.add(campoPassword, gbc);

        etiquetaMensaje.setFont(new Font("SansSerif", Font.PLAIN, 11));
        etiquetaMensaje.setForeground(COLOR_ERROR);
        gbc.gridy = 7;
        panelDerecho.add(etiquetaMensaje, gbc);

        EstilosHoseg.estilizarBotonPrincipal(botonIngresar, EstilosHoseg.BURDEO, EstilosHoseg.BURDEO_HOVER);
        botonIngresar.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8;
        panelDerecho.add(botonIngresar, gbc);

        panelPrincipal.add(panelIzquierdo);
        panelPrincipal.add(panelDerecho);

        getRootPane().setDefaultButton(botonIngresar);
        setContentPane(panelPrincipal);
    }

    private void intentarIngresar() {
        String usuario = campoUsuario.getText().trim();
        char[] password = campoPassword.getPassword();

        if (usuario.isEmpty() || password.length == 0) {
            mostrarMensaje("Ingresa usuario y contraseña.");
            return;
        }

        botonIngresar.setEnabled(false);
        try {
            controlador.iniciarSesion(usuario, password);
            abrirMenuPrincipal();
        } catch (ServicioException ex) {
            String mensajeError = ex.getMessage();
             if (mensajeError.toLowerCase().contains("bloqueado")) {
            JOptionPane.showMessageDialog(
                this, 
                mensajeError, 
                "Acceso Bloqueado", 
                JOptionPane.ERROR_MESSAGE
            );
            mostrarMensaje("Usuario bloqueado."); 
        } else {
           
            mostrarMensaje(mensajeError);
        }
        } finally {
            Arrays.fill(password, '\0');
            botonIngresar.setEnabled(true);
        }
    }

    private void abrirMenuPrincipal() {
        new MenuPrincipalJFrame().setVisible(true);
        dispose();
    }

    private void mostrarMensaje(String mensaje) {
        etiquetaMensaje.setText(mensaje);
    }


    //Clase para la imagen
        // Panel avanzado que escala la imagen y dibuja la franja multicolor de Höség
    private static class PanelFondoImagen extends JPanel {
        private Image imagen;

        public PanelFondoImagen(String rutaIcono) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            // Si el recurso no existe, imagen queda null y paintComponent
            // usa el color plano: no hace falta avisar por consola.
            java.net.URL url = getClass().getResource(rutaIcono);
            if (url != null) {
                this.imagen = new ImageIcon(url).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Activar calidad alta para los gráficos
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (imagen != null) {
                // 1. Dibujar la imagen estirada
                g2d.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
                
                // 2. Capa oscura elegante para legibilidad del texto blanco
                g2d.setColor(new Color(0, 0, 0, 140)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // Fondo verde oscuro de respaldo si no hay imagen
                g2d.setColor(new Color(0x2D, 0x3A, 0x33));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // 3. LA FRANJA MULTICOLOR (Dibujada en la parte inferior del panel izquierdo)
            int altoFranja = 6; // Grosor de la línea en píxeles
            int yFranja = getHeight() - altoFranja; // Posición abajo del todo

            // Degradado lineal horizontal: va desde Azul -> Morado -> Rosa -> Rojo (como tu foto)
            LinearGradientPaint degradadoHoseg = new LinearGradientPaint(
                0, yFranja, getWidth(), yFranja,
                new float[]{0.0f, 0.35f, 0.70f, 1.0f}, // Puntos de transición
                new Color[]{
                    new Color(0x00, 0x33, 0xAA), // Azul
                    new Color(0x6A, 0x1B, 0x9A), // Morado
                    new Color(0xD8, 0x1B, 0x60), // Rosa / Magenta
                    new Color(0xD3, 0x2F, 0x2F)  // Rojo
                }
            );

            g2d.setPaint(degradadoHoseg);
            g2d.fillRect(0, yFranja, getWidth(), altoFranja);
        }
    }

}

