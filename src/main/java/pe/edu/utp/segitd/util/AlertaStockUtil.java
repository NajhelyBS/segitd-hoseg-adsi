package pe.edu.utp.segitd.util;

import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.InventarioService;
import pe.edu.utp.segitd.servicio.ServicioException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public final class AlertaStockUtil {

    private static final Color COLOR_PRIMARIO = new Color(0x2D, 0x3A, 0x33); // Verde Sea Pine
    private static final Color COLOR_BURDEO = new Color(0x8C, 0x2D, 0x19);   // Terracota
    private static final Color COLOR_FONDO = new Color(0xF5, 0xF5, 0xF3);    // Crema suave

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

            JPanel contenedor = new JPanel(new BorderLayout(0, 12));
            contenedor.setBackground(COLOR_FONDO);
            contenedor.setBorder(new EmptyBorder(8, 8, 8, 8));

            // Encabezado visual
            JPanel header = new JPanel(new BorderLayout(0, 4));
            header.setBackground(COLOR_FONDO);

            JLabel lblTitulo = new JLabel("Productos en nivel crítico de inventario");
            lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
            lblTitulo.setForeground(COLOR_BURDEO);

            JLabel lblSubtitulo = new JLabel("Los siguientes artículos alcanzaron o bajaron de su stock mínimo:");
            lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblSubtitulo.setForeground(new Color(0x44, 0x44, 0x44));

            header.add(lblTitulo, BorderLayout.NORTH);
            header.add(lblSubtitulo, BorderLayout.SOUTH);
            contenedor.add(header, BorderLayout.NORTH);

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
                        p.getTalla() != null ? p.getTalla() : "-",
                        p.getStockComercial(),
                        p.getStockMinimo()
                });
            }

            JTable tabla = new JTable(modelo);
            tabla.setRowHeight(26);
            tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
            tabla.setShowVerticalLines(false);
            tabla.setGridColor(new Color(0xEE, 0xEE, 0xEE));
            tabla.setRowSelectionAllowed(false);

            JTableHeader th = tabla.getTableHeader();
            th.setFont(new Font("SansSerif", Font.BOLD, 12));
            th.setBackground(COLOR_PRIMARIO);
            th.setForeground(Color.WHITE);
            th.setPreferredSize(new Dimension(th.getWidth(), 28));

            DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
            renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
            tabla.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
            tabla.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

            DefaultTableCellRenderer renderCritico = new DefaultTableCellRenderer();
            renderCritico.setHorizontalAlignment(SwingConstants.CENTER);
            renderCritico.setForeground(COLOR_BURDEO);
            renderCritico.setFont(new Font("SansSerif", Font.BOLD, 12));
            tabla.getColumnModel().getColumn(3).setCellRenderer(renderCritico);

            tabla.getColumnModel().getColumn(0).setPreferredWidth(105);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(210);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(55);
            tabla.getColumnModel().getColumn(3).setPreferredWidth(85);
            tabla.getColumnModel().getColumn(4).setPreferredWidth(65);

            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setBorder(new LineBorder(new Color(0xD3, 0xD3, 0xD3), 1));
            int altoTabla = Math.min(180, 32 + (criticos.size() * 26));
            scroll.setPreferredSize(new Dimension(520, altoTabla));
            contenedor.add(scroll, BorderLayout.CENTER);

            JLabel lblPie = new JLabel("Sugerencia: Evalúe generar una orden de reposición en «Pedidos a proveedores».");
            lblPie.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblPie.setForeground(COLOR_PRIMARIO);
            contenedor.add(lblPie, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(
                    padre,
                    contenedor,
                    "SEGITD-HÖSÉG · Alerta de Stock Mínimo (CU07)",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (ServicioException ignored) {
        }
    }
}
