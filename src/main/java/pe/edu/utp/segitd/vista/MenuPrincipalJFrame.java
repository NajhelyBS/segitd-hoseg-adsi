package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.MenuPrincipalControlador;
import pe.edu.utp.segitd.modelo.RolUsuario;
import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.SesionUsuario;
import pe.edu.utp.segitd.util.AlertaStockUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

/**
 * Dashboard principal: indicadores en vivo y accesos a cada módulo,
 * habilitados según el rol de la sesión activa (sección 8, pantalla 2).
 */
public class MenuPrincipalJFrame extends JFrame {

    private final MenuPrincipalControlador controlador = new MenuPrincipalControlador();

    private final JLabel indicadorConexion = new JLabel();
    private final JLabel valorProductosActivos = new JLabel("-");
    private final JLabel valorPedidosPendientes = new JLabel("-");
    private final JLabel valorDonacionesPendientes = new JLabel("-");
    
    private final JProgressBar barraStockCritico = new JProgressBar(0, 100);
    private final JProgressBar barraLotesRuta = new JProgressBar(0, 100);
    private final JLabel lblNumStockCritico = new JLabel("0");
    private final JLabel lblNumLotesRuta = new JLabel("0");

    // CONSTANTES DE DISEÑO 
    private final Color COLOR_FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema neutro muy suave
    private final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine 
    private final Color COLOR_PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    private final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);       // Terracota 
    private final Color COLOR_BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    private final Color COLOR_GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    private final Color COLOR_TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);

    private final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    private final Font FUENTE_INDICADOR_NUM = new Font("SansSerif", Font.BOLD, 36);
    private final Font FUENTE_INDICADOR_TXT = new Font("SansSerif", Font.PLAIN, 12);
    private final Font FUENTE_BOTONES = new Font("SansSerif", Font.BOLD, 13);

      public MenuPrincipalJFrame() {
        super("SEGITD-HÖSÉG · Panel principal");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //ventana contenedora
        JPanel panelRaiz = construirContenido();
        panelRaiz.setBackground(COLOR_FONDO_VENTANA);
        setContentPane(panelRaiz);
        
        setMinimumSize(new Dimension(980, 560)); 
        this.getContentPane().setBackground(COLOR_FONDO_VENTANA);
        pack();
        setLocationRelativeTo(null);
        actualizarPanel();
        SwingUtilities.invokeLater(() -> AlertaStockUtil.mostrarSiHayStockCritico(this));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion(); //
            }
        });
    }

    private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(COLOR_FONDO_VENTANA);
        
        JPanel contenedorNorte = new JPanel();
        contenedorNorte.setLayout(new BoxLayout(contenedorNorte, BoxLayout.Y_AXIS));
        contenedorNorte.setBackground(COLOR_FONDO_VENTANA);
        
        contenedorNorte.add(construirEncabezado());
        contenedorNorte.add(Box.createVerticalStrut(14));
        
        contenedorNorte.add(EstilosHoseg.crearFranja());

        
        raiz.add(contenedorNorte, BorderLayout.NORTH);
        raiz.add(construirIndicadores(), BorderLayout.CENTER);
        raiz.add(construirModulos(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel construirEncabezado() {
        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(COLOR_FONDO_VENTANA);

        //titulo izq
        JLabel titulo = new JLabel("Höség Store — Back office");
        titulo.setFont(EstilosHoseg.FUENTE_TITULO);
        titulo.setForeground(COLOR_TEXTO_MAIN);
        encabezado.add(titulo, BorderLayout.WEST);

        //contenedor der
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setBackground(COLOR_FONDO_VENTANA);

        //estado conexion
        indicadorConexion.setFont(new Font("SansSerif", Font.BOLD, 12));
        derecha.add(indicadorConexion);

        //usuario
        JLabel lblUser = new JLabel("👤 " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblUser.setForeground(COLOR_GRIS_TEXTO);
        derecha.add(lblUser);
        
        //boton actualizar
        JButton botonActualizar = crearBotónHeader("Actualizar", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonActualizar.addActionListener(e -> actualizarPanel()); //
        derecha.add(botonActualizar);

        //boton respaldar
        JButton botonRespaldar = crearBotónHeader("Respaldar ahora", COLOR_PRIMARIO, COLOR_PRIMARIO_HOVER);
        botonRespaldar.setEnabled(usuario.getRol() == RolUsuario.ADMINISTRADOR); //
        botonRespaldar.addActionListener(e -> respaldarAhora()); //
        derecha.add(botonRespaldar);
        
        //boton cerrar sesion
        JButton botonCerrarSesion = crearBotónHeader("Cerrar sesión", COLOR_BURDEO, COLOR_BURDEO_HOVER);
        botonCerrarSesion.addActionListener(e -> cerrarSesion()); //
        derecha.add(botonCerrarSesion);

        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

   private JPanel construirIndicadores() {
        JPanel contenedorControles = new JPanel(new BorderLayout(16, 0));
        contenedorControles.setBackground(EstilosHoseg.FONDO_VENTANA);

        JPanel panelTarjetasIzquierda = new JPanel(new GridLayout(1, 3, 14, 0));
        panelTarjetasIzquierda.setBackground(EstilosHoseg.FONDO_VENTANA);

        panelTarjetasIzquierda.add(tarjeta("CATÁLOGO", "Productos activos", "Disponibles en tienda",
                valorProductosActivos, new Color(0x00, 0x33, 0xAA)));
        panelTarjetasIzquierda.add(tarjeta("VENTAS WEB", "Pedidos pendientes", "Esperando confirmación",
                valorPedidosPendientes, new Color(0x6A, 0x1B, 0x9A)));
        panelTarjetasIzquierda.add(tarjeta("IMPACTO SOCIAL", "Donaciones por asignar", "Listas para armar lote",
                valorDonacionesPendientes, new Color(0x2E, 0x7D, 0x32)));

        contenedorControles.add(panelTarjetasIzquierda, BorderLayout.CENTER);

        // Panel derecho: Estado de Almacén y Logística con esquinas redondeadas
        JPanel panelGraficoDerecha = new JPanel(new BorderLayout());
        panelGraficoDerecha.setBackground(Color.WHITE);
        panelGraficoDerecha.putClientProperty("FlatLaf.style", "arc: 14");
        panelGraficoDerecha.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

               JLabel tituloGrafico = new JLabel("Estado de Almacén y Logística");
        tituloGrafico.setFont(new Font("SansSerif", Font.BOLD, 13));
        tituloGrafico.setForeground(EstilosHoseg.TEXTO_MAIN);
        panelGraficoDerecha.add(tituloGrafico, BorderLayout.NORTH);

        JPanel cuerpoGrafico = new JPanel(new GridLayout(2, 1, 0, 10));
        cuerpoGrafico.setBackground(Color.WHITE);
        cuerpoGrafico.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        barraStockCritico.setForeground(EstilosHoseg.BURDEO);
        barraStockCritico.setBackground(new Color(0xF2, 0xF2, 0xF0));
        barraStockCritico.setBorderPainted(false);
        barraStockCritico.setPreferredSize(new Dimension(120, 10));

        barraLotesRuta.setForeground(new Color(0x00, 0x33, 0xAA));
        barraLotesRuta.setBackground(new Color(0xF2, 0xF2, 0xF0));
        barraLotesRuta.setBorderPainted(false);
        barraLotesRuta.setPreferredSize(new Dimension(120, 10));

        JPanel fila1 = new JPanel(new BorderLayout(10, 0));
        fila1.setBackground(Color.WHITE);
        JLabel lblEtiq1 = new JLabel("Productos críticos:");
        lblEtiq1.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEtiq1.setPreferredSize(new Dimension(115, 20));
        lblNumStockCritico.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblNumStockCritico.setForeground(EstilosHoseg.BURDEO);
        fila1.add(lblEtiq1, BorderLayout.WEST);
        fila1.add(barraStockCritico, BorderLayout.CENTER);
        fila1.add(lblNumStockCritico, BorderLayout.EAST);

        JPanel fila2 = new JPanel(new BorderLayout(10, 0));
        fila2.setBackground(Color.WHITE);
        JLabel lblEtiq2 = new JLabel("Lotes en ruta:");
        lblEtiq2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEtiq2.setPreferredSize(new Dimension(115, 20));
        lblNumLotesRuta.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblNumLotesRuta.setForeground(new Color(0x00, 0x33, 0xAA));
        fila2.add(lblEtiq2, BorderLayout.WEST);
        fila2.add(barraLotesRuta, BorderLayout.CENTER);
        fila2.add(lblNumLotesRuta, BorderLayout.EAST);

        cuerpoGrafico.add(fila1);
        cuerpoGrafico.add(fila2);
        panelGraficoDerecha.add(cuerpoGrafico, BorderLayout.CENTER);

        JButton botonVerReporteStock = new JButton("Ver reporte de stock detallado");
        botonVerReporteStock.setFont(new Font("SansSerif", Font.BOLD, 11));
        botonVerReporteStock.setForeground(EstilosHoseg.PRIMARIO);
        botonVerReporteStock.setBackground(Color.WHITE);
        botonVerReporteStock.setFocusPainted(false);
        botonVerReporteStock.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonVerReporteStock.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.PRIMARIO, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        botonVerReporteStock.addActionListener(e -> new ReporteStockJFrame().setVisible(true));
        botonVerReporteStock.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                botonVerReporteStock.setBackground(EstilosHoseg.PRIMARIO);
                botonVerReporteStock.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                botonVerReporteStock.setBackground(Color.WHITE);
                botonVerReporteStock.setForeground(EstilosHoseg.PRIMARIO);
            }
        });
        panelGraficoDerecha.add(botonVerReporteStock, BorderLayout.SOUTH);
        panelGraficoDerecha.setPreferredSize(new Dimension(340, 155));

        contenedorControles.add(panelGraficoDerecha, BorderLayout.EAST);
        return contenedorControles;
    }


     private JPanel tarjeta(String categoria, String etiqueta, String subtitulo, JLabel valor, Color colorAcento) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.putClientProperty("FlatLaf.style", "arc: 14");
        wrapper.setBorder(new LineBorder(EstilosHoseg.BORDE_SUAVE, 1));

        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(colorAcento);
        barraSuperior.setPreferredSize(new Dimension(10, 4));
        wrapper.add(barraSuperior, BorderLayout.NORTH);

        JPanel cuerpoTarjeta = new JPanel();
        cuerpoTarjeta.setLayout(new BoxLayout(cuerpoTarjeta, BoxLayout.Y_AXIS));
        cuerpoTarjeta.setBackground(Color.WHITE);
        cuerpoTarjeta.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblCat = new JLabel("● " + categoria);
        lblCat.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblCat.setForeground(colorAcento);
        lblCat.setAlignmentX(Component.LEFT_ALIGNMENT);

        valor.setFont(new Font("SansSerif", Font.BOLD, 34));
        valor.setForeground(EstilosHoseg.PRIMARIO);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(etiqueta);
        descripcion.setFont(new Font("SansSerif", Font.BOLD, 13));
        descripcion.setForeground(EstilosHoseg.TEXTO_MAIN);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(EstilosHoseg.GRIS_TEXTO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        cuerpoTarjeta.add(lblCat);
        cuerpoTarjeta.add(Box.createVerticalStrut(6));
        cuerpoTarjeta.add(valor);
        cuerpoTarjeta.add(Box.createVerticalStrut(4));
        cuerpoTarjeta.add(descripcion);
        cuerpoTarjeta.add(Box.createVerticalStrut(2));
        cuerpoTarjeta.add(lblSub);

        wrapper.add(cuerpoTarjeta, BorderLayout.CENTER);
        return wrapper;
    }


    ///////////////////////////////////777
       private JPanel construirModulos() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setBackground(EstilosHoseg.FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        Usuario usuario = SesionUsuario.obtenerInstancia().getUsuarioActual();
        RolUsuario rol = usuario.getRol();
        boolean admin = rol == RolUsuario.ADMINISTRADOR;
        boolean almacen = admin || rol == RolUsuario.ENCARGADO;
        boolean impacto = admin || rol == RolUsuario.GESTOR_IMPACTO;

        panel.add(botonModulo("Gestión de productos", "Catálogo, stock comercial y comprometido",
                almacen, () -> new GestionProductosJFrame().setVisible(true)));
        panel.add(botonModulo("Pedidos web", "Validación de ventas y boletas con QR",
                true, () -> new PedidosWebJFrame().setVisible(true)));
        panel.add(botonModulo("Despacho de lotes", "Asignación y entrega a comunidades",
                impacto, () -> new DespachoLotesJFrame().setVisible(true)));
        panel.add(botonModulo("Reportes de impacto", "Trazabilidad Excel y Certificado B",
                impacto, () -> new ReportesImpactoJFrame().setVisible(true)));
        panel.add(botonModulo("Pedidos a proveedores", "Órdenes de reposición y recepción",
                almacen, () -> new ProveedoresJFrame().setVisible(true)));
        panel.add(botonModulo("Gestión de usuarios", "Cuentas, roles y control de acceso",
                admin, () -> new UsuariosJFrame().setVisible(true)));

        return panel;
    }

    private JButton botonModulo(String tituloModulo, String subtituloModulo, boolean habilitado, Runnable accion) {
        String html = "<html><div style='text-align:center;'>"
                + "<div style='font-size:13px; font-weight:bold;'>" + tituloModulo + "</div>"
                + "<div style='font-size:10px; margin-top:3px; opacity:0.85;'>" + subtituloModulo + "</div>"
                + "</div></html>";

        JButton boton = new JButton(html);
        boton.setEnabled(habilitado);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("FlatLaf.style", "arc: 12");

        if (habilitado) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(EstilosHoseg.PRIMARIO);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(EstilosHoseg.PRIMARIO, 1),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            boton.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    boton.setBackground(EstilosHoseg.PRIMARIO);
                    boton.setForeground(Color.WHITE);
                }
                @Override public void mouseExited(MouseEvent e) {
                    boton.setBackground(Color.WHITE);
                    boton.setForeground(EstilosHoseg.PRIMARIO);
                }
            });
        } else {
            boton.setBackground(new Color(0xEA, 0xEA, 0xE8));
            boton.setForeground(new Color(0x9E, 0x9E, 0x9E));
            boton.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xD5, 0xD5, 0xD3), 1),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        }

        boton.addActionListener(e -> {
            if (accion != null) {
                accion.run();
            }
        });
        return boton;
    }


    //metodo crear boton
    private JButton crearBotónHeader(String texto, Color base, Color hover) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(base);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(base);
            }
        });
        return boton;
    }

     private void actualizarPanel() {
        boolean conectado = controlador.verificarConexion();
        indicadorConexion.setText(conectado ? "● Conectado" : "● Sin conexión");
        indicadorConexion.setForeground(conectado ? new Color(0x2E, 0x7D, 0x32) : new Color(0xB0, 0x00, 0x20));

        if (!conectado) {
            limpiarIndicadores();
            return;
        }

        try {
            IndicadoresDashboard indicadores = controlador.cargarIndicadores();
            
            valorProductosActivos.setText(String.valueOf(indicadores.productosActivos()));
            valorPedidosPendientes.setText(String.valueOf(indicadores.pedidosWebPendientes()));
            valorDonacionesPendientes.setText(String.valueOf(indicadores.donacionesPendientes()));
            
            int bajoStock = indicadores.productosBajoStockMinimo();
            int lotesRuta = indicadores.lotesEnRuta();
            
            lblNumStockCritico.setText(String.valueOf(bajoStock));
            lblNumLotesRuta.setText(String.valueOf(lotesRuta));
            
            barraStockCritico.setValue(Math.min(100, (bajoStock * 100) / 10));
            barraLotesRuta.setValue(Math.min(100, (lotesRuta * 100) / 10));
            
        } catch (ServicioException e) {
            limpiarIndicadores();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

     private void limpiarIndicadores() {
        valorProductosActivos.setText("-");
        valorPedidosPendientes.setText("-");
        valorDonacionesPendientes.setText("-");
        
        lblNumStockCritico.setText("0");
        lblNumLotesRuta.setText("0");
        
        barraStockCritico.setValue(0);
        barraLotesRuta.setValue(0);
    }

    private void cerrarSesion() {
        controlador.cerrarSesion();
        dispose();
        new LoginJFrame().setVisible(true);
    }

    private void respaldarAhora() {
        try {
            Path carpeta = controlador.respaldarAhora();
            JOptionPane.showMessageDialog(this, "Respaldo generado en " + carpeta.toAbsolutePath(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarAplicacion() {
        try {
            controlador.respaldarSiCorresponde();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el respaldo automático: " + e.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        System.exit(0);
    }


}