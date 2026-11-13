package pe.edu.utp.segitd.util;

import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.InventarioService;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.vista.EstilosHoseg;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Despliega una alerta modal estilizada al Administrador cuando existen
 * productos cuyo stock comercial alcanzó o bajó de su stock mínimo.
 */
public final class AlertaStockUtil {

    private AlertaStockUtil() {
    }

    public static void mostrarSiHayStockCritico(Component padre) {
        if (!SesionUsuario.obtenerInstancia().esAdministrador()) {
            return;
        }
        try {
            List<Producto> criticos = new InventarioService().listarProductosBajoStockMinimo();
            if (criticos.isEmpty()) {
                return;
            }

            Window ventanaPadre = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
            JDialog dialogo = new JDialog(ventanaPadre, "SEGITD-HÖSÉG · Notificación de Stock Mínimo",
                    Dialog.ModalityType.APPLICATION_MODAL);
            dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialogo.setResizable(false);

            JPanel raiz = new JPanel(new BorderLayout());
            raiz.setBackground(EstilosHoseg.FONDO_VENTANA);

            // Franja degradada superior característica de Höség
            raiz.add(EstilosHoseg.crearFranja(), BorderLayout.NORTH);

            JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
            cuerpo.setBackground(EstilosHoseg.FONDO_VENTANA);
            cuerpo.setBorder(new EmptyBorder(18, 22, 18, 22));

            // Encabezado con título centrado y badge contador
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBackground(EstilosHoseg.FONDO_VENTANA);

            JLabel badge = new JLabel(criticos.size() == 1
                    ? "  1 ARTÍCULO EN NIVEL CRÍTICO  "
                    : "  " + criticos.size() + " ARTÍCULOS EN NIVEL CRÍTICO  ");
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBackground(new Color(0xFD, 0xED, 0xEC));
            badge.setForeground(EstilosHoseg.BURDEO);
            badge.setBorder(new LineBorder(new Color(0xF5, 0xB7, 0xB1), 1));
            badge.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblTitulo = new JLabel("Alerta de Reposición de Inventario");
            lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
            lblTitulo.setForeground(EstilosHoseg.TEXTO_MAIN);
            lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblSubtitulo = new JLabel("Los siguientes productos alcanzaron o bajaron de su stock mínimo configurado:");
            lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblSubtitulo.setForeground(EstilosHoseg.GRIS_TEXTO);
            lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            header.add(badge);
            header.add(Box.createVerticalStrut(8));
            header.add(lblTitulo);
            header.add(Box.createVerticalStrut(4));
            header.add(lblSubtitulo);
            cuerpo.add(header, BorderLayout.NORTH);

            // Tabla estilizada con scroll suave para cualquier cantidad de productos
            String[] columnas = {"Código", "Producto", "Talla", "Stock actual", "Mínimo"};
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Producto p : criticos) {
                modelo.addRow(new Object[]{
                        p.getCodigo(),
                        p.getNombre(),
                        p.getTalla() != null ? p.getTalla() : "—",
                        p.getStockComercial(),
                        p.getStockMinimo()
                });
            }

            JTable tabla = new JTable(modelo);
            EstilosHoseg.estilizarTabla(tabla);
            tabla.setRowSelectionAllowed(false);

            DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
            renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
            tabla.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
            tabla.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
            tabla.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

            DefaultTableCellRenderer renderCritico = new DefaultTableCellRenderer();
            renderCritico.setHorizontalAlignment(SwingConstants.CENTER);
            renderCritico.setForeground(EstilosHoseg.BURDEO);
            renderCritico.setFont(new Font("SansSerif", Font.BOLD, 12));
            tabla.getColumnModel().getColumn(3).setCellRenderer(renderCritico);

            tabla.getColumnModel().getColumn(0).setPreferredWidth(105);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(55);
            tabla.getColumnModel().getColumn(3).setPreferredWidth(85);
            tabla.getColumnModel().getColumn(4).setPreferredWidth(65);

            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setBorder(new LineBorder(EstilosHoseg.BORDE_SUAVE, 1));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            int altoTabla = Math.min(220, 34 + (criticos.size() * 26));
            scroll.setPreferredSize(new Dimension(540, altoTabla));
            cuerpo.add(scroll, BorderLayout.CENTER);

            // Pie con sugerencia y botón principal estilizado
            JPanel pie = new JPanel(new BorderLayout(0, 12));
            pie.setBackground(EstilosHoseg.FONDO_VENTANA);

            JLabel lblSugerencia = new JLabel(
                    "Sugerencia: Evalúe registrar una orden de compra en «Pedidos a proveedores».",
                    SwingConstants.CENTER);
            lblSugerencia.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblSugerencia.setForeground(EstilosHoseg.PRIMARIO);

            JButton botonCerrar = new JButton("Entendido");
            EstilosHoseg.estilizarBotonPrincipal(botonCerrar, EstilosHoseg.PRIMARIO, EstilosHoseg.PRIMARIO_HOVER);
            botonCerrar.addActionListener(e -> dialogo.dispose());

            JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panelBoton.setBackground(EstilosHoseg.FONDO_VENTANA);
            panelBoton.add(botonCerrar);

            pie.add(lblSugerencia, BorderLayout.NORTH);
            pie.add(panelBoton, BorderLayout.SOUTH);
            cuerpo.add(pie, BorderLayout.SOUTH);

            raiz.add(cuerpo, BorderLayout.CENTER);
            dialogo.setContentPane(raiz);
            dialogo.getRootPane().setDefaultButton(botonCerrar);
            dialogo.pack();
            dialogo.setLocationRelativeTo(ventanaPadre);
            dialogo.setVisible(true);
        } catch (ServicioException ignored) {
        }
    }
}
