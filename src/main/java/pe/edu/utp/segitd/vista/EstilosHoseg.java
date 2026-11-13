package pe.edu.utp.segitd.vista;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Paleta de colores, tipografía y componentes visuales reutilizables
 * de Höség Store (14-DIEZ S.A.C.) para todas las pantallas Swing.
 */
public final class EstilosHoseg {

    public static final Color FONDO_VENTANA = new Color(0xF5, 0xF5, 0xF3); // Crema suave
    public static final Color PRIMARIO = new Color(0x2D, 0x3A, 0x33);      // Verde Sea Pine
    public static final Color PRIMARIO_HOVER = new Color(0x3D, 0x4E, 0x45);
    public static final Color BURDEO = new Color(0x8C, 0x2D, 0x19);        // Terracota
    public static final Color BURDEO_HOVER = new Color(0xA6, 0x3A, 0x24);
    public static final Color GRIS_TEXTO = new Color(0x55, 0x55, 0x55);
    public static final Color TEXTO_MAIN = new Color(0x1A, 0x1A, 0x1A);
    public static final Color BORDE_SUAVE = new Color(0xE2, 0xE2, 0xE0);

    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FUENTE_INPUT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 13);

    private EstilosHoseg() {
    }

    /** Devuelve la franja degradada característica de Höség (Azul -> Morado -> Rosa -> Rojo). */
    public static JComponent crearFranja() {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(100, 4));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                LinearGradientPaint degradado = new LinearGradientPaint(
                        0, 0, getWidth(), 0,
                        new float[]{0.0f, 0.35f, 0.70f, 1.0f},
                        new Color[]{
                                new Color(0x00, 0x33, 0xAA),
                                new Color(0x6A, 0x1B, 0x9A),
                                new Color(0xD8, 0x1B, 0x60),
                                new Color(0xD3, 0x2F, 0x2F)
                        }
                );
                g2d.setPaint(degradado);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    /** Encabezado estándar con título grande + franja degradada debajo. */
    public static JPanel crearEncabezadoModulo(String tituloTexto) {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(FONDO_VENTANA);

        JLabel titulo = new JLabel(tituloTexto);
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(TEXTO_MAIN);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(titulo);
        top.add(Box.createVerticalStrut(10));
        JComponent franja = crearFranja();
        franja.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(franja);
        return top;
    }

    public static void estilizarBotonPrincipal(JButton boton, Color fondo, Color hover) {
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (boton.isEnabled()) boton.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { if (boton.isEnabled()) boton.setBackground(fondo); }
        });
    }

    public static void estilizarBotonSecundario(JButton boton, Color colorBorde) {
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBorde);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(colorBorde, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(colorBorde); boton.setForeground(Color.WHITE); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (boton.isEnabled()) { boton.setBackground(Color.WHITE); boton.setForeground(colorBorde); }
            }
        });
    }

    public static void estilizarTabla(JTable t) {
        t.setRowHeight(26);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        t.setSelectionForeground(TEXTO_MAIN);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        t.setFont(FUENTE_INPUT);

        JTableHeader header = t.getTableHeader();
        header.setFont(FUENTE_LABEL);
        header.setBackground(PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static void estilizarInput(JComponent campo) {
        campo.setFont(FUENTE_INPUT);
        campo.setBackground(Color.WHITE);
        campo.setForeground(TEXTO_MAIN);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD3, 0xD3, 0xD3), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }
}
