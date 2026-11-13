-- =====================================================================
-- SEGITD-HÖSÉG (ADSI) · Datos de demostración
-- Ejecutar una sola vez, sobre la base recién creada con 01_esquema.sql.
--
-- Reúne la semilla de 02..05 del repositorio original, ya escrita sobre
-- el esquema final: donaciones EN_ESPERA en pedidos sin pagar, bitácora
-- con claves foráneas (sin la columna referencia), lotes con fecha de
-- despacho y de entrega, y clientes con nombre y apellido separados.
--
-- El stock NO se escribe a mano: cada producto parte en 0 y su stock
-- final es la suma de sus movimientos (inventario inicial, ventas,
-- entregas de lotes, recepciones y ajustes). Así la bitácora (CU09)
-- explica cada unidad, como exige la regla "ningún stock se modifica sin
-- registrar el movimiento".
--
-- Las fechas son relativas al momento de ejecución (último mes), para
-- que los reportes por rango de fechas siempre tengan datos.
--
-- Logins de prueba (hash SHA-256 + salt, igual que util.HashUtil):
--   admin     / Admin#2026      ADMINISTRADOR
--   encargado / Encargado#2026  ENCARGADO
--   gestor    / Gestor#2026     GESTOR_IMPACTO
-- =====================================================================

BEGIN;

-- ---------- Usuarios internos ----------------------------------------
INSERT INTO usuario (nombre, username, password_hash, salt, rol, creado_en) VALUES
    ('Administrador General',    'admin',     '3dec1f94ec26fc37269cbce15511ed51ca268ff46d5e0da9ebaa88f3344ecf3c', '105fcbe92d91615124787a57e50b728b', 'ADMINISTRADOR',  now() - interval '35 days'),
    ('Encargado de Almacén',     'encargado', 'd7ee60cf804514edcce056e94a746ecf6edcbf72a4bbcfe92204daf871237264', 'b121f545f8f72c864ae3b41979be29ad', 'ENCARGADO',      now() - interval '35 days'),
    ('Gestor de Impacto Social', 'gestor',    '3d796bb19603731f9d36045b033109d29eba67612fa1cb953cad0ced809eabbc', '862958b42f79590672cb21cf8dbc7af6', 'GESTOR_IMPACTO', now() - interval '35 days');

-- ---------- Tablas de referencia -------------------------------------
INSERT INTO categoria (nombre) VALUES ('Casacas'), ('Accesorios');
INSERT INTO coleccion (nombre) VALUES ('Älpafill'), ('Comunidad'), ('Urbana');

-- ---------- Catálogo ---------------------------------------------------
-- Stock en 0: se carga con el movimiento "Inventario inicial" más abajo.
-- HSG-CAS-006 y HSG-ACC-004 terminan bajo su stock mínimo a propósito,
-- para la alerta de stock mínimo (CU07) y el resaltado en rojo.
-- HSG-ACC-007 y HSG-ACC-008 no tienen compromiso social (tipo NULL).
INSERT INTO producto
    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, precio,
     stock_minimo, tipo_compromiso, visible_web, activo, creado_en)
VALUES
    ('HSG-CAS-001', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'M',     'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-002', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'L',     'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-003', 'Chaleco Älpafill',           'Höség', 'Casacas',    'Älpafill',  'S',     'Chaleco acolchado, capa intermedia de la línea Älpafill.',           219.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-004', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'XL',    'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-005', 'Poncho Älpafill',            'Höség', 'Casacas',    'Älpafill',  'Única', 'Poncho impermeable de doble capa, ideal para lluvia intensa.',        189.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-006', 'Chompa Älpafill',            'Höség', 'Casacas',    'Älpafill',  'M',     'Chompa tejida con relleno térmico ligero.',                           149.90, 6,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-CAS-007', 'Cortavientos Älpafill',      'Höség', 'Casacas',    'Älpafill',  'L',     'Cortavientos ultraligero, plegable.',                                 129.90, 5,  'ABRIGO', TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-001', 'Gorro de lana Layo',         'Höség', 'Accesorios', 'Comunidad', 'Única', 'Gorro tejido a mano por artesanas de la comunidad de Layo.',           49.90, 8,  'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-002', 'Bufanda Paucartambo',        'Höség', 'Accesorios', 'Comunidad', 'Única', 'Bufanda de lana de oveja, tejido tradicional cusqueño.',                39.90, 8,  'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-003', 'Guantes de lana Omacha',     'Höség', 'Accesorios', 'Comunidad', 'Única', 'Guantes tejidos a mano por artesanas de Omacha.',                       34.90, 8,  'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-004', 'Chalina Ccatca',             'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chalina de lana de alpaca, tejido tradicional de Ccatca.',              44.90, 8,  'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-005', 'Chullo andino',              'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chullo tradicional con orejeras, tejido a mano.',                       29.90, 10, 'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-006', 'Medias de alpaca',           'Höség', 'Accesorios', 'Comunidad', 'Única', 'Medias térmicas de fibra de alpaca.',                                   19.90, 10, 'ARBOL',  TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-007', 'Mochila artesanal Höség',    'Höség', 'Accesorios', 'Urbana',    'Única', 'Mochila de algodón orgánico, línea urbana sin compromiso social.',      79.90, 5,  NULL,     TRUE, TRUE, now() - interval '30 days'),
    ('HSG-ACC-008', 'Botella reutilizable Höség', 'Höség', 'Accesorios', 'Urbana',    'Única', 'Botella de acero inoxidable, línea urbana sin compromiso social.',      39.90, 10, NULL,     TRUE, TRUE, now() - interval '30 days');

-- ---------- ONG y comunidades altoandinas de Cusco --------------------
INSERT INTO ong (nombre, contacto, telefono) VALUES
    ('Pachamama Raymi', 'Rocío Injante', '+51 984 123 456');

INSERT INTO comunidad (nombre, distrito, provincia, region) VALUES
    ('Omacha',      'Omacha',      'Paruro',       'Cusco'),
    ('Ccatca',      'Ccatca',      'Quispicanchi', 'Cusco'),
    ('Marcapata',   'Marcapata',   'Quispicanchi', 'Cusco'),
    ('Paucartambo', 'Paucartambo', 'Paucartambo',  'Cusco'),
    ('Layo',        'Layo',        'Canas',        'Cusco');

-- ---------- Proveedores (talleres textiles) ---------------------------
INSERT INTO proveedor (nombre_taller, ruc, contacto, telefono) VALUES
    ('Textiles Ausangate S.A.C.',    '20456789123', 'Marco Quispe',   '+51 984 555 111'),
    ('Confecciones Andina E.I.R.L.', '20489321456', 'Lucía Ttito',    '+51 984 555 222'),
    ('Manos de Ausangate E.I.R.L.',  '20512345678', 'Rosa Huamán',    '+51 984 555 333'),
    ('Textiles Vilcanota S.A.C.',    '20498765432', 'Edwin Ccahuana', '+51 984 555 444');

-- ---------- Clientes ---------------------------------------------------
INSERT INTO cliente (nombre, apellido, tipo_doc, num_doc, email, telefono, creado_en) VALUES
    ('María Fernanda', 'Gutiérrez',         'DNI', '45678912', 'maria.gutierrez@example.com',    '+51 987 111 222', now() - interval '40 days'),
    ('Jorge Luis',     'Ramírez',           'DNI', '41234567', 'jorge.ramirez@example.com',      '+51 987 222 333', now() - interval '40 days'),
    ('Ana Belén',      'Torres',            'DNI', '47812365', 'ana.torres@example.com',         '+51 987 333 444', now() - interval '40 days'),
    ('Camila',         'Rodríguez Paredes', 'DNI', '48912345', 'camila.rodriguez@example.com',   '+51 987 444 555', now() - interval '40 days'),
    ('Diego Alonso',   'Vega',              'DNI', '46123789', 'diego.vega@example.com',         '+51 987 555 666', now() - interval '40 days'),
    ('Valeria',        'Cusihuamán Quispe', 'DNI', '47234891', 'valeria.cusihuaman@example.com', '+51 987 666 777', now() - interval '40 days'),
    ('Sebastián',      'Flores Mamani',     'DNI', '45678234', 'sebastian.flores@example.com',   '+51 987 777 888', now() - interval '40 days');


-- =====================================================================
-- Guion de la demo: qué pasó en el último mes
-- (_lote, _venta y _linea son tablas auxiliares: se borran al final. No
-- son TEMP porque el editor SQL de Supabase no las conserva entre
-- instrucciones.)
-- =====================================================================

DROP TABLE IF EXISTS _lote, _venta, _linea;

-- Lotes de donación (CU05/CU06), a cargo del gestor de impacto.
CREATE TABLE _lote (
    clave TEXT, comunidad TEXT, estado TEXT,
    creado INTERVAL, despacho INTERVAL, entrega INTERVAL, observaciones TEXT
);

INSERT INTO _lote VALUES
    ('A', 'Omacha',      'ENTREGADO', '20 days', '19 days', '17 days', 'Primera entrega de la temporada de heladas.'),
    ('B', 'Paucartambo', 'ENTREGADO', '8 days',  '6 days',  '4 days',  NULL),
    ('C', 'Ccatca',      'EN_RUTA',   '7 days',  '5 days',  NULL,      'Sale con el transporte de la ONG.');

-- Pedidos web. lote: a qué lote fueron sus donaciones (NULL = aún no).
--   PENDIENTE → llegó de la web, sin confirmar; donación EN_ESPERA
--   PAGADO    → confirmado en el escritorio (CU02); donación PENDIENTE,
--               ASIGNADA o ENTREGADA según el lote
--   ANULADO   → rechazado; sin donación
CREATE TABLE _venta (
    comprobante TEXT, num_doc TEXT, estado TEXT, hace INTERVAL, lote TEXT
);

INSERT INTO _venta VALUES
    ('WEB-000101', '45678912', 'PENDIENTE', '2 days',   NULL),
    ('WEB-000102', '41234567', 'PENDIENTE', '1 day',    NULL),
    ('WEB-000103', '47812365', 'PENDIENTE', '20 hours', NULL),
    ('WEB-000104', '48912345', 'PENDIENTE', '6 hours',  NULL),
    ('WEB-000105', '46123789', 'PENDIENTE', '3 hours',  NULL),
    ('WEB-000106', '47234891', 'PAGADO',    '10 days',  NULL),
    ('WEB-000107', '45678234', 'PAGADO',    '9 days',   'C'),
    ('WEB-000108', '48912345', 'PAGADO',    '12 days',  'B'),
    ('WEB-000109', '46123789', 'ANULADO',   '5 days',   NULL),
    ('WEB-000110', '45678912', 'PAGADO',    '25 days',  'A'),
    ('WEB-000111', '41234567', 'PAGADO',    '22 days',  'A'),
    ('WEB-000112', '47812365', 'PAGADO',    '7 days',   NULL);

CREATE TABLE _linea (
    comprobante TEXT, codigo TEXT, cantidad INTEGER
);

INSERT INTO _linea VALUES
    ('WEB-000101', 'HSG-CAS-001', 1),
    ('WEB-000102', 'HSG-ACC-001', 2),
    ('WEB-000103', 'HSG-CAS-003', 1),
    ('WEB-000103', 'HSG-ACC-002', 1),
    ('WEB-000104', 'HSG-CAS-004', 1),
    ('WEB-000105', 'HSG-CAS-005', 1),
    ('WEB-000105', 'HSG-ACC-003', 2),
    ('WEB-000106', 'HSG-ACC-005', 3),
    ('WEB-000107', 'HSG-ACC-006', 4),
    ('WEB-000108', 'HSG-CAS-006', 2),
    ('WEB-000109', 'HSG-ACC-007', 1),
    ('WEB-000110', 'HSG-CAS-002', 1),
    ('WEB-000110', 'HSG-ACC-008', 1),
    ('WEB-000111', 'HSG-ACC-001', 2),
    ('WEB-000111', 'HSG-ACC-002', 1),
    ('WEB-000112', 'HSG-CAS-007', 1),
    ('WEB-000112', 'HSG-ACC-007', 1);


-- ---------- Ventas y su detalle ---------------------------------------
-- El total es la suma de las líneas al precio de catálogo.
INSERT INTO venta (codigo_comprobante, fecha, id_cliente, id_usuario, origen, estado, total)
SELECT v.comprobante,
       now() - v.hace,
       c.id,
       CASE WHEN v.estado = 'PENDIENTE' THEN NULL
            ELSE (SELECT id FROM usuario WHERE username = 'admin') END,
       'WEB',
       v.estado,
       (SELECT SUM(l.cantidad * p.precio)
          FROM _linea l JOIN producto p ON p.codigo = l.codigo
         WHERE l.comprobante = v.comprobante)
  FROM _venta v
  JOIN cliente c ON c.num_doc = v.num_doc
 ORDER BY v.comprobante;

INSERT INTO detalle_venta (id_venta, codigo_producto, cantidad, precio_unitario, subtotal)
SELECT ve.id, l.codigo, l.cantidad, p.precio, l.cantidad * p.precio
  FROM _linea l
  JOIN venta ve   ON ve.codigo_comprobante = l.comprobante
  JOIN producto p ON p.codigo = l.codigo
 ORDER BY l.comprobante, l.codigo;

-- ---------- Lotes de donación ----------------------------------------
-- El código definitivo (HSG-Lnnn) sale del id, igual que LoteDAO.crear;
-- mientras tanto se usa uno provisional para enlazar las donaciones.
INSERT INTO lote_donacion
    (codigo_lote, id_comunidad, id_ong, id_usuario_responsable,
     fecha_creacion, fecha_despacho, fecha_entrega, estado, observaciones)
SELECT 'TMP-' || lo.clave, com.id, o.id, u.id,
       now() - lo.creado, now() - lo.despacho, now() - lo.entrega,
       lo.estado, lo.observaciones
  FROM _lote lo
  JOIN comunidad com ON com.nombre = lo.comunidad
  JOIN ong o         ON o.nombre = 'Pachamama Raymi'
  JOIN usuario u     ON u.username = 'gestor'
 ORDER BY lo.creado DESC;

-- ---------- Donaciones (CU04) ----------------------------------------
-- Una por cada línea de un producto con compromiso social, salvo en los
-- pedidos anulados (al anular se eliminan las que no tenían lote).
INSERT INTO donacion (id_detalle_venta, codigo_producto, cantidad, tipo, estado, id_lote, creado_en)
SELECT dv.id, dv.codigo_producto, dv.cantidad, p.tipo_compromiso,
       CASE WHEN v.estado = 'PENDIENTE' THEN 'EN_ESPERA'
            WHEN lo.estado IS NULL      THEN 'PENDIENTE'
            WHEN lo.estado = 'ENTREGADO' THEN 'ENTREGADA'
            ELSE 'ASIGNADA' END,
       l.id,
       ve.fecha
  FROM _venta v
  JOIN venta ve         ON ve.codigo_comprobante = v.comprobante
  JOIN detalle_venta dv ON dv.id_venta = ve.id
  JOIN producto p       ON p.codigo = dv.codigo_producto
  LEFT JOIN _lote lo         ON lo.clave = v.lote
  LEFT JOIN lote_donacion l  ON l.codigo_lote = 'TMP-' || v.lote
 WHERE v.estado <> 'ANULADO'
   AND p.tipo_compromiso IS NOT NULL
 ORDER BY dv.id;

-- ---------- Pedidos a proveedores (CU08) ------------------------------
-- Dos SOLICITADO para que el encargado registre la recepción en la demo.
INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, fecha, estado, id_usuario)
SELECT pr.id, x.codigo, x.descripcion, x.cantidad, now() - x.hace, x.estado, u.id
  FROM (VALUES
        ('Textiles Ausangate S.A.C.',   'HSG-ACC-004', 'Pedido duplicado, se anula',                              15, interval '20 days', 'ANULADO'),
        ('Textiles Vilcanota S.A.C.',   'HSG-ACC-006', 'Reposición de medias de alpaca',                          30, interval '15 days', 'RECIBIDO'),
        ('Manos de Ausangate E.I.R.L.', 'HSG-CAS-004', 'Reposición de casacas talla XL para temporada de heladas', 20, interval '2 days',  'SOLICITADO'),
        ('Textiles Ausangate S.A.C.',   'HSG-ACC-004', 'Reposición de chalinas Ccatca bajo stock mínimo',         25, interval '1 day',   'SOLICITADO')
       ) AS x (taller, codigo, descripcion, cantidad, hace, estado)
  JOIN proveedor pr ON pr.nombre_taller = x.taller
  JOIN usuario u    ON u.username = 'admin'
 ORDER BY x.hace DESC;


-- =====================================================================
-- Bitácora de inventario (CU09): mismos motivos que escribe el código
-- =====================================================================

-- Inventario inicial, hace 30 días.
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha)
SELECT x.codigo, 'COMERCIAL', 'INGRESO', x.cantidad, 'Inventario inicial', 'ESCRITORIO', u.id,
       now() - interval '30 days'
  FROM (VALUES
        ('HSG-CAS-001', 30), ('HSG-CAS-002', 25), ('HSG-CAS-003', 20), ('HSG-CAS-004', 18),
        ('HSG-CAS-005', 22), ('HSG-CAS-006', 4),  ('HSG-CAS-007', 27),
        ('HSG-ACC-001', 40), ('HSG-ACC-002', 35), ('HSG-ACC-003', 30), ('HSG-ACC-004', 3),
        ('HSG-ACC-005', 45), ('HSG-ACC-006', 60), ('HSG-ACC-007', 15), ('HSG-ACC-008', 40)
       ) AS x (codigo, cantidad)
  JOIN usuario u ON u.username = 'admin'
 ORDER BY x.codigo;

-- Recepción del pedido a proveedor (ProveedorService.marcarRecibido).
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha, id_pedido_proveedor)
SELECT pp.codigo_producto, 'COMERCIAL', 'INGRESO', pp.cantidad, 'Recepción de pedido a proveedor', 'ESCRITORIO', u.id,
       pp.fecha + interval '2 days', pp.id
  FROM pedido_proveedor pp
  JOIN usuario u ON u.username = 'encargado'
 WHERE pp.estado = 'RECIBIDO';

-- Confirmación de pedidos web, 2 horas después de la compra
-- (PedidoWebService.confirmarPedido): sale del stock comercial y, si el
-- producto tiene compromiso social, entra al comprometido.
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha, id_venta)
SELECT m.codigo_producto, m.tipo_stock, m.tipo_movimiento, m.cantidad, 'Venta web confirmada', 'ESCRITORIO',
       ve.id_usuario, ve.fecha + interval '2 hours', ve.id
  FROM venta ve
  JOIN detalle_venta dv ON dv.id_venta = ve.id
  JOIN producto p       ON p.codigo = dv.codigo_producto
  CROSS JOIN LATERAL (
        SELECT dv.codigo_producto, 'COMERCIAL', 'SALIDA', -dv.cantidad
        UNION ALL
        SELECT dv.codigo_producto, 'COMPROMETIDO', 'INGRESO', dv.cantidad
         WHERE p.tipo_compromiso IS NOT NULL
       ) AS m (codigo_producto, tipo_stock, tipo_movimiento, cantidad)
 WHERE ve.estado = 'PAGADO'
 ORDER BY ve.fecha, dv.id, m.tipo_stock DESC;

-- Ajuste manual del encargado (CU03).
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha)
SELECT 'HSG-ACC-008', 'COMERCIAL', 'AJUSTE', -1, 'Merma: unidad abollada en almacén', 'ESCRITORIO', u.id,
       now() - interval '8 days'
  FROM usuario u WHERE u.username = 'encargado';

-- Entrega de lotes (DespachoService): sale del stock comprometido.
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha, id_lote)
SELECT d.codigo_producto, 'COMPROMETIDO', 'SALIDA', -d.cantidad, 'Entrega de lote de donación', 'ESCRITORIO',
       l.id_usuario_responsable, l.fecha_entrega, l.id
  FROM donacion d
  JOIN lote_donacion l ON l.id = d.id_lote
 WHERE l.estado = 'ENTREGADO'
 ORDER BY l.fecha_entrega, d.id;


-- =====================================================================
-- Cierre
-- =====================================================================

-- Stock = suma de la bitácora.
UPDATE producto p
   SET stock_comercial    = COALESCE(s.comercial, 0),
       stock_comprometido = COALESCE(s.comprometido, 0)
  FROM (SELECT codigo_producto,
               SUM(cantidad) FILTER (WHERE tipo_stock = 'COMERCIAL')    AS comercial,
               SUM(cantidad) FILTER (WHERE tipo_stock = 'COMPROMETIDO') AS comprometido
          FROM movimiento_inventario
         GROUP BY codigo_producto) s
 WHERE s.codigo_producto = p.codigo;

-- Código definitivo de los lotes.
UPDATE lote_donacion SET codigo_lote = 'HSG-L' || lpad(id::text, 3, '0');

-- El próximo pedido simulado será WEB-000113.
SELECT setval('seq_comprobante_web',
              (SELECT MAX(substring(codigo_comprobante FROM '^WEB-(\d+)$')::integer)
                 FROM venta WHERE codigo_comprobante ~ '^WEB-\d+$'),
              true);

-- Fuera las tablas auxiliares del guion.
DROP TABLE _lote, _venta, _linea;

COMMIT;
