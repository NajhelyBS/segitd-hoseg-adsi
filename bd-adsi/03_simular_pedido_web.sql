-- =====================================================================
-- SEGITD-HÖSÉG (ADSI) · Simular un pedido web nuevo
-- Paso 4 del flujo de demostración. En esta base no hay portal web, así
-- que este script hace lo que haría la web al cerrar un carrito:
--   · venta PENDIENTE con comprobante WEB-nnnnnn (seq_comprobante_web)
--   · una línea de detalle_venta por producto
--   · una donación EN_ESPERA por cada producto con compromiso social
-- Después, en el escritorio, el pedido aparece en Pedidos web y se
-- confirma (CU02): baja el stock, la donación pasa a PENDIENTE (CU04) y,
-- si algo queda bajo el mínimo, salta la alerta (CU07).
--
-- Se puede ejecutar todas las veces que se quiera: cada ejecución crea
-- un pedido nuevo. Para cambiar el pedido, editar solo las dos listas
-- marcadas con "EDITAR". Debe devolver 1 fila con el comprobante creado.
-- =====================================================================

WITH cliente_pedido AS (
    -- EDITAR: documento del cliente que compra (ver tabla cliente).
    SELECT id FROM cliente WHERE num_doc = '72621025'
), lineas (codigo, cantidad) AS (
    -- EDITAR: productos y cantidades. Un producto por línea.
    -- HSG-ACC-004 tiene stock 3: al confirmar queda bajo el mínimo (CU07).
    VALUES ('HSG-CAS-002', 1),
           ('HSG-ACC-004', 2)
), nueva_venta AS (
    INSERT INTO venta (codigo_comprobante, id_cliente, origen, estado, total)
    SELECT 'WEB-' || lpad(nextval('seq_comprobante_web')::text, 6, '0'),
           c.id, 'WEB', 'PENDIENTE',
           (SELECT SUM(l.cantidad * p.precio)
              FROM lineas l JOIN producto p ON p.codigo = l.codigo AND p.activo)
      FROM cliente_pedido c
    RETURNING id, codigo_comprobante, total
), nuevo_detalle AS (
    INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
    SELECT v.id, l.codigo, l.cantidad, p.precio, l.cantidad * p.precio
      FROM nueva_venta v
     CROSS JOIN lineas l
      JOIN producto p ON p.codigo = l.codigo AND p.activo
    RETURNING id, codigo_producto, cantidad
), nuevas_donaciones AS (
    INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado)
    SELECT d.id, d.codigo_producto, d.cantidad, p.tipo_compromiso, 'EN_ESPERA'
      FROM nuevo_detalle d
      JOIN producto p ON p.codigo = d.codigo_producto
     WHERE p.tipo_compromiso IS NOT NULL
    RETURNING id
)
SELECT v.codigo_comprobante                     AS comprobante,
       v.total,
       (SELECT COUNT(*) FROM nuevo_detalle)     AS lineas,
       (SELECT COUNT(*) FROM nuevas_donaciones) AS donaciones_en_espera
  FROM nueva_venta v;
