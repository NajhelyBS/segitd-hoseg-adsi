package pe.edu.utp.segitd.vista;

import pe.edu.utp.segitd.controlador.ProveedorControlador;
import pe.edu.utp.segitd.modelo.EstadoPedidoProveedor;
import pe.edu.utp.segitd.modelo.PedidoProveedor;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.Proveedor;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.Validador;
import pe.edu.utp.segitd.util.SesionUsuario;

import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font; 
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CRUD de proveedores y pedidos de reposición (RF-06, sección 8 pantalla 7).
 * Solo ADMINISTRADOR — ya viene deshabilitado en el menú para ENCARGADO.
 */
public class ProveedoresJFrame extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProveedorControlador controlador = new ProveedorControlador();

    // --- pestaña Proveedores ---
    private final ModeloProveedores modeloProveedores = new ModeloProveedores();
    private final JTable tablaProveedores = new JTable(modeloProveedores);
    private final JTextField txtNombreTaller = new JTextField(20);
    private final JTextField txtRuc = new JTextField(12);
    private final JTextField txtContacto = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(14);
    private Integer idProveedorEnEdicion;

    // --- pestaña Pedidos ---
    private final ModeloPedidos modeloPedidos = new ModeloPedidos();
    private final JTable tablaPedidos = new JTable(modeloPedidos);
    private final JComboBox<Proveedor> comboProveedorPedido = new JComboBox<>();
    private final JComboBox<Producto> comboProductoPedido = new JComboBox<>();
    private final JTextField txtDescripcionPedido = new JTextField(20);
    private final JSpinner spinnerCantidadPedido = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JButton botonRecibido = new JButton("Marcar recibido");
    private final JButton botonAnularPedido = new JButton("Anular pedido");

    public ProveedoresJFrame() {
        super("SEGITD-HÖSÉG · Proveedores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setPreferredSize(new Dimension(960, 580));
        setMinimumSize(new Dimension(880, 520));
        pack();
        setSize(960, 580);
        setLocationRelativeTo(null);

        cargarProveedores();
        cargarProductosCombo();
        cargarPedidos();
    }

     private JPanel construirContenido() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        raiz.setBackground(EstilosHoseg.FONDO_VENTANA);

        raiz.add(EstilosHoseg.crearEncabezadoModulo("Proveedores y Abastecimiento"), BorderLayout.NORTH);

        boolean esAdmin = SesionUsuario.obtenerInstancia().esAdministrador();
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(EstilosHoseg.FUENTE_LABEL);
        pestanas.setBackground(Color.WHITE);
        pestanas.setForeground(EstilosHoseg.PRIMARIO);

        if (esAdmin) {
            pestanas.addTab("Proveedores", construirTabProveedores());
        }
        pestanas.addTab("Pedidos de reposición", construirTabPedidos());
        raiz.add(pestanas, BorderLayout.CENTER);
        return raiz;
    }

    // ---------- Proveedores ----------

    private JPanel construirTabProveedores() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(EstilosHoseg.FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Tarjeta superior: tabla de talleres
        EstilosHoseg.estilizarTabla(tablaProveedores);
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProveedores.getSelectedRow() >= 0) {
                cargarFormularioProveedor(modeloProveedores.obtener(tablaProveedores.convertRowIndexToModel(tablaProveedores.getSelectedRow())));
            }
        });

        JPanel tarjetaTabla = new JPanel(new BorderLayout(0, 8));
        tarjetaTabla.setBackground(Color.WHITE);
        tarjetaTabla.putClientProperty("FlatLaf.style", "arc: 14");
        tarjetaTabla.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel lblLista = new JLabel("Directorio de Talleres y Proveedores Activos");
        lblLista.setFont(EstilosHoseg.FUENTE_SUBTITULO);
        lblLista.setForeground(EstilosHoseg.TEXTO_MAIN);
        tarjetaTabla.add(lblLista, BorderLayout.NORTH);

        JScrollPane scrollTabla = new JScrollPane(tablaProveedores);
        scrollTabla.setBorder(new LineBorder(EstilosHoseg.BORDE_SUAVE, 1));
        tarjetaTabla.add(scrollTabla, BorderLayout.CENTER);
        panel.add(tarjetaTabla, BorderLayout.CENTER);

        // Tarjeta inferior: formulario de registro / edición
        JPanel tarjetaForm = new JPanel(new BorderLayout(0, 12));
        tarjetaForm.setBackground(Color.WHITE);
        tarjetaForm.putClientProperty("FlatLaf.style", "arc: 14");
        tarjetaForm.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel lblForm = new JLabel("Ficha del Proveedor");
        lblForm.setFont(EstilosHoseg.FUENTE_SUBTITULO);
        lblForm.setForeground(EstilosHoseg.PRIMARIO);
        tarjetaForm.add(lblForm, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        EstilosHoseg.estilizarInput(txtNombreTaller);
        EstilosHoseg.estilizarInput(txtRuc);
        EstilosHoseg.estilizarInput(txtContacto);
        EstilosHoseg.estilizarInput(txtTelefono);

        agregarFilaForm(panelFormulario, gbc, 0, "Nombre del taller:", txtNombreTaller, "RUC:", txtRuc);
        agregarFilaForm(panelFormulario, gbc, 1, "Contacto:", txtContacto, "Teléfono:", txtTelefono);

        tarjetaForm.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonNuevo = new JButton("Nuevo");
        EstilosHoseg.estilizarBotonSecundario(botonNuevo, EstilosHoseg.PRIMARIO);
        botonNuevo.addActionListener(e -> limpiarFormularioProveedor());

        JButton botonGuardar = new JButton("Guardar");
        EstilosHoseg.estilizarBotonPrincipal(botonGuardar, EstilosHoseg.PRIMARIO, EstilosHoseg.PRIMARIO_HOVER);
        botonGuardar.addActionListener(e -> guardarProveedor());

        JButton botonBaja = new JButton("Dar de baja");
        EstilosHoseg.estilizarBotonSecundario(botonBaja, EstilosHoseg.BURDEO);
        botonBaja.addActionListener(e -> darDeBajaProveedor());

        panelBotones.add(botonNuevo);
        panelBotones.add(botonBaja);
        panelBotones.add(botonGuardar);
        tarjetaForm.add(panelBotones, BorderLayout.SOUTH);

        panel.add(tarjetaForm, BorderLayout.SOUTH);
        return panel;
    }

    private void agregarFilaForm(JPanel panel, GridBagConstraints gbc, int fila,
                                 String etiq1, JComponent campo1, String etiq2, JComponent campo2) {
        gbc.gridy = fila;
        gbc.gridx = 0; gbc.weightx = 0.12;
        JLabel l1 = new JLabel(etiq1); l1.setFont(EstilosHoseg.FUENTE_LABEL); l1.setForeground(EstilosHoseg.GRIS_TEXTO);
        panel.add(l1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.38;
        panel.add(campo1, gbc);

        gbc.gridx = 2; gbc.weightx = 0.12;
        JLabel l2 = new JLabel(etiq2); l2.setFont(EstilosHoseg.FUENTE_LABEL); l2.setForeground(EstilosHoseg.GRIS_TEXTO);
        panel.add(l2, gbc);

        gbc.gridx = 3; gbc.weightx = 0.38;
        panel.add(campo2, gbc);
    }

    private void cargarProveedores() {
        try {
            List<Proveedor> proveedores = controlador.listarProveedores();
            modeloProveedores.actualizar(proveedores);
            comboProveedorPedido.removeAllItems();
            proveedores.forEach(comboProveedorPedido::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarFormularioProveedor(Proveedor p) {
        idProveedorEnEdicion = p.getId();
        txtNombreTaller.setText(p.getNombreTaller());
        txtRuc.setText(nvl(p.getRuc()));
        txtContacto.setText(nvl(p.getContacto()));
        txtTelefono.setText(nvl(p.getTelefono()));
    }

    private void limpiarFormularioProveedor() {
        idProveedorEnEdicion = null;
        txtNombreTaller.setText("");
        txtRuc.setText("");
        txtContacto.setText("");
        txtTelefono.setText("");
        tablaProveedores.clearSelection();
    }

    private void guardarProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(idProveedorEnEdicion);
        proveedor.setNombreTaller(txtNombreTaller.getText().trim());
        proveedor.setRuc(Validador.vacioComoNulo(txtRuc.getText()));
        proveedor.setContacto(Validador.vacioComoNulo(txtContacto.getText()));
        proveedor.setTelefono(Validador.vacioComoNulo(txtTelefono.getText()));
        proveedor.setActivo(true);

        try {
            if (idProveedorEnEdicion == null) {
                controlador.crearProveedor(proveedor);
            } else {
                controlador.actualizarProveedor(proveedor);
            }
            cargarProveedores();
            limpiarFormularioProveedor();
            JOptionPane.showMessageDialog(this, "Proveedor guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darDeBajaProveedor() {
        if (idProveedorEnEdicion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un proveedor de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Dar de baja este proveedor?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.desactivarProveedor(idProveedorEnEdicion);
            cargarProveedores();
            limpiarFormularioProveedor();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Pedidos ----------

        private JPanel construirTabPedidos() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(EstilosHoseg.FONDO_VENTANA);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        if (SesionUsuario.obtenerInstancia().esAdministrador()) {
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    construirPanelListaPedidos(), construirPanelNuevoPedido());
            split.setResizeWeight(0.66);
            split.setDividerLocation(245);
            split.setBorder(null);
            split.setBackground(EstilosHoseg.FONDO_VENTANA);
            if (split.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI ui) {
                ui.getDivider().setBorder(null);
            }
            panel.add(split, BorderLayout.CENTER);
        } else {
            panel.add(construirPanelListaPedidos(), BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel construirPanelListaPedidos() {
        EstilosHoseg.estilizarTabla(tablaPedidos);
        tablaPedidos.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                if (!isSelected && value instanceof EstadoPedidoProveedor ep) {
                    switch (ep) {
                        case RECIBIDO -> setForeground(new Color(0x2E, 0x7D, 0x32));
                        case SOLICITADO -> setForeground(new Color(0xB2, 0x6A, 0x00));
                        case ANULADO -> setForeground(EstilosHoseg.BURDEO);
                    }
                }
                return c;
            }
        });

        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesPedido();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.putClientProperty("FlatLaf.style", "arc: 14");
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        // Cabecera con título a la izquierda y botones a la derecha para dar más espacio a la tabla
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Órdenes de Reposición a Talleres");
        lblTitulo.setFont(EstilosHoseg.FUENTE_SUBTITULO);
        lblTitulo.setForeground(EstilosHoseg.TEXTO_MAIN);
        cabecera.add(lblTitulo, BorderLayout.WEST);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotones.setBackground(Color.WHITE);

        EstilosHoseg.estilizarBotonPrincipal(botonRecibido, EstilosHoseg.PRIMARIO, EstilosHoseg.PRIMARIO_HOVER);
        EstilosHoseg.estilizarBotonSecundario(botonAnularPedido, EstilosHoseg.BURDEO);

        botonRecibido.setEnabled(false);
        botonAnularPedido.setEnabled(false);
        botonRecibido.addActionListener(e -> marcarRecibido());
        botonAnularPedido.addActionListener(e -> anularPedido());

        panelBotones.add(botonAnularPedido);
        panelBotones.add(botonRecibido);
        cabecera.add(panelBotones, BorderLayout.EAST);

        panel.add(cabecera, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tablaPedidos);
        scroll.setBorder(new LineBorder(EstilosHoseg.BORDE_SUAVE, 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }


    private JPanel construirPanelNuevoPedido() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.putClientProperty("FlatLaf.style", "arc: 14");
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstilosHoseg.BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel lblTitulo = new JLabel("Registrar Nueva Orden de Abastecimiento");
        lblTitulo.setFont(EstilosHoseg.FUENTE_SUBTITULO);
        lblTitulo.setForeground(EstilosHoseg.PRIMARIO);
        tarjeta.add(lblTitulo, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboProveedorPedido.setFont(EstilosHoseg.FUENTE_INPUT);
        comboProveedorPedido.setBackground(Color.WHITE);
        comboProductoPedido.setFont(EstilosHoseg.FUENTE_INPUT);
        comboProductoPedido.setBackground(Color.WHITE);
        EstilosHoseg.estilizarInput(txtDescripcionPedido);
        EstilosHoseg.estilizarInput((JComponent) spinnerCantidadPedido.getEditor());

        agregarFilaForm(panel, gbc, 0, "Proveedor:", comboProveedorPedido, "Producto:", comboProductoPedido);
        agregarFilaForm(panel, gbc, 1, "Descripción:", txtDescripcionPedido, "Cantidad:", spinnerCantidadPedido);

        tarjeta.add(panel, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setBackground(Color.WHITE);
        JButton botonRegistrar = new JButton("Registrar pedido");
        EstilosHoseg.estilizarBotonPrincipal(botonRegistrar, EstilosHoseg.PRIMARIO, EstilosHoseg.PRIMARIO_HOVER);
        botonRegistrar.addActionListener(e -> registrarPedido());
        pie.add(botonRegistrar);
        tarjeta.add(pie, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void cargarProductosCombo() {
        try {
            comboProductoPedido.removeAllItems();
            controlador.listarProductos().forEach(comboProductoPedido::addItem);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarPedidos() {
        try {
            modeloPedidos.actualizar(controlador.listarPedidos());
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        actualizarBotonesPedido();
    }

    private PedidoProveedor pedidoSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        return fila < 0 ? null : modeloPedidos.obtener(tablaPedidos.convertRowIndexToModel(fila));
    }

     private void actualizarBotonesPedido() {
     PedidoProveedor pedido = pedidoSeleccionado();
     boolean esAdmin = SesionUsuario.obtenerInstancia().esAdministrador();
     boolean solicitado = pedido != null && pedido.getEstado() == EstadoPedidoProveedor.SOLICITADO;
     botonRecibido.setEnabled(solicitado);
     botonAnularPedido.setEnabled(esAdmin && solicitado);
 }


    private void registrarPedido() {
        Proveedor proveedor = (Proveedor) comboProveedorPedido.getSelectedItem();
        Producto producto = (Producto) comboProductoPedido.getSelectedItem();
        String descripcion = txtDescripcionPedido.getText().trim();
        int cantidad = ((Number) spinnerCantidadPedido.getValue()).intValue();

        if (proveedor == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un proveedor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controlador.crearPedido(proveedor.getId(), producto == null ? null : producto.getCodigo(), descripcion, cantidad);
            txtDescripcionPedido.setText("");
            spinnerCantidadPedido.setValue(1);
            cargarPedidos();
            JOptionPane.showMessageDialog(this, "Pedido registrado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
     private void marcarRecibido() {
     PedidoProveedor pedido = pedidoSeleccionado();
     if (pedido == null) {
         return;
     }

     JSpinner spinnerRecibido = new JSpinner(new SpinnerNumberModel(pedido.getCantidad(), 1, 1_000_000, 1));
     JPanel panelDialogo = new JPanel(new GridBagLayout());
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.insets = new Insets(4, 4, 4, 4);
     gbc.anchor = GridBagConstraints.WEST;

     gbc.gridx = 0; gbc.gridy = 0;
     panelDialogo.add(new JLabel("Producto:"), gbc);
     gbc.gridx = 1;
     panelDialogo.add(new JLabel(pedido.getNombreProducto() != null ? pedido.getNombreProducto() : "—"), gbc);

     gbc.gridx = 0; gbc.gridy = 1;
     panelDialogo.add(new JLabel("Cantidad solicitada:"), gbc);
     gbc.gridx = 1;
     panelDialogo.add(new JLabel(String.valueOf(pedido.getCantidad())), gbc);

     gbc.gridx = 0; gbc.gridy = 2;
     panelDialogo.add(new JLabel("Cantidad recibida confirmada:"), gbc);
     gbc.gridx = 1;
     panelDialogo.add(spinnerRecibido, gbc);

     int opcion = JOptionPane.showConfirmDialog(
             this, panelDialogo, "Confirmar recepción de mercadería (CU08)",
             JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
     if (opcion != JOptionPane.OK_OPTION) {
         return;
     }

     int cantidadRecibida = ((Number) spinnerRecibido.getValue()).intValue();

     // CU08 - Flujo alternativo A3: avisar si las cantidades no coinciden antes de confirmar
     if (cantidadRecibida != pedido.getCantidad()) {
         int diferencia = cantidadRecibida - pedido.getCantidad();
         String signo = diferencia > 0 ? "+" + diferencia : String.valueOf(diferencia);
         int confirmarDiferencia = JOptionPane.showConfirmDialog(
                 this,
                 "La cantidad recibida (" + cantidadRecibida + ") no coincide con la solicitada ("
                         + pedido.getCantidad() + ").\nDiferencia: " + signo + " unidades.\n\n"
                         + "¿Desea confirmar la recepción con la cantidad ingresada?",
                 "Diferencia en recepción (CU08 - A3)",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE);
         if (confirmarDiferencia != JOptionPane.YES_OPTION) {
             return;
         }
     }

     try {
         controlador.marcarRecibido(pedido.getId(), cantidadRecibida);
         cargarPedidos();
         JOptionPane.showMessageDialog(this,
                 "Recepción registrada: se sumaron " + cantidadRecibida + " unidades al stock comercial.",
                 "Éxito", JOptionPane.INFORMATION_MESSAGE);
     } catch (ServicioException e) {
         JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
     }
 }


    private void anularPedido() {
        PedidoProveedor pedido = pedidoSeleccionado();
        if (pedido == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Anular este pedido?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.anularPedido(pedido.getId());
            cargarPedidos();
        } catch (ServicioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    private static final class ModeloProveedores extends AbstractTableModel {
        private final String[] columnas = {"Taller", "RUC", "Contacto", "Teléfono"};
        private List<Proveedor> proveedores = List.of();

        void actualizar(List<Proveedor> nuevos) {
            this.proveedores = nuevos;
            fireTableDataChanged();
        }

        Proveedor obtener(int fila) {
            return proveedores.get(fila);
        }

        @Override
        public int getRowCount() {
            return proveedores.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Proveedor p = proveedores.get(fila);
            return switch (columna) {
                case 0 -> p.getNombreTaller();
                case 1 -> p.getRuc();
                case 2 -> p.getContacto();
                case 3 -> p.getTelefono();
                default -> null;
            };
        }
    }

    private static final class ModeloPedidos extends AbstractTableModel {
        private final String[] columnas = {"Proveedor", "Producto", "Descripción", "Cantidad", "Fecha", "Estado"};
        private List<PedidoProveedor> pedidos = List.of();

        void actualizar(List<PedidoProveedor> nuevos) {
            this.pedidos = nuevos;
            fireTableDataChanged();
        }

        PedidoProveedor obtener(int fila) {
            return pedidos.get(fila);
        }

        @Override
        public int getRowCount() {
            return pedidos.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            PedidoProveedor p = pedidos.get(fila);
            return switch (columna) {
                case 0 -> p.getNombreTaller();
                case 1 -> p.getNombreProducto() == null ? "—" : p.getNombreProducto();
                case 2 -> p.getDescripcion();
                case 3 -> p.getCantidad();
                case 4 -> FORMATO_FECHA.format(p.getFecha());
                case 5 -> p.getEstado();
                default -> null;
            };
        }
    }
}
