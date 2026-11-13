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
    ('Administrador General',    'admin',     '3dec1f94ec26fc37269cbce15511ed51ca268ff46d5e0da9ebaa88f3344ecf3c', '105fcbe92d91615124787a57e50b728b', 'ADMINISTRADOR',  '2023-01-10 09:00:00-05'),
    ('Encargado de Almacén',     'encargado', 'd7ee60cf804514edcce056e94a746ecf6edcbf72a4bbcfe92204daf871237264', 'b121f545f8f72c864ae3b41979be29ad', 'ENCARGADO',      '2023-01-10 09:00:00-05'),
    ('Gestor de Impacto Social', 'gestor',    '3d796bb19603731f9d36045b033109d29eba67612fa1cb953cad0ced809eabbc', '862958b42f79590672cb21cf8dbc7af6', 'GESTOR_IMPACTO', '2023-01-10 09:00:00-05');

-- ---------- Tablas de referencia -------------------------------------
INSERT INTO categoria (nombre) VALUES ('Casacas'), ('Accesorios');
INSERT INTO coleccion (nombre) VALUES ('Älpafill'), ('Comunidad'), ('Urbana');

-- ---------- Catálogo ---------------------------------------------------
INSERT INTO producto
    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, precio,
     stock_minimo, tipo_compromiso, visible_web, activo, creado_en)
VALUES
    ('HSG-CAS-001', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'M',     'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-002', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'L',     'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-003', 'Chaleco Älpafill',           'Höség', 'Casacas',    'Älpafill',  'S',     'Chaleco acolchado, capa intermedia de la línea Älpafill.',           219.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-004', 'Casaca Älpafill Cusco',      'Höség', 'Casacas',    'Älpafill',  'XL',    'Casaca de abrigo con relleno sintético Älpafill, impermeable.',      259.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-005', 'Poncho Älpafill',            'Höség', 'Casacas',    'Älpafill',  'Única', 'Poncho impermeable de doble capa, ideal para lluvia intensa.',        189.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-006', 'Chompa Älpafill',            'Höség', 'Casacas',    'Älpafill',  'M',     'Chompa tejida con relleno térmico ligero.',                           149.90, 6,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-CAS-007', 'Cortavientos Älpafill',      'Höség', 'Casacas',    'Älpafill',  'L',     'Cortavientos ultraligero, plegable.',                                 129.90, 5,  'ABRIGO', TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-001', 'Gorro de lana Layo',         'Höség', 'Accesorios', 'Comunidad', 'Única', 'Gorro tejido a mano por artesanas de la comunidad de Layo.',           49.90, 8,  'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-002', 'Bufanda Paucartambo',        'Höség', 'Accesorios', 'Comunidad', 'Única', 'Bufanda de lana de oveja, tejido tradicional cusqueño.',                39.90, 8,  'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-003', 'Guantes de lana Omacha',     'Höség', 'Accesorios', 'Comunidad', 'Única', 'Guantes tejidos a mano por artesanas de Omacha.',                       34.90, 8,  'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-004', 'Chalina Ccatca',             'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chalina de lana de alpaca, tejido tradicional de Ccatca.',              44.90, 8,  'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-005', 'Chullo andino',              'Höség', 'Accesorios', 'Comunidad', 'Única', 'Chullo tradicional con orejeras, tejido a mano.',                       29.90, 10, 'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-006', 'Medias de alpaca',           'Höség', 'Accesorios', 'Comunidad', 'Única', 'Medias térmicas de fibra de alpaca.',                                   19.90, 10, 'ARBOL',  TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-007', 'Mochila artesanal Höség',    'Höség', 'Accesorios', 'Urbana',    'Única', 'Mochila de algodón orgánico, línea urbana sin compromiso social.',      79.90, 5,  NULL,     TRUE, TRUE, '2023-01-15 10:00:00-05'),
    ('HSG-ACC-008', 'Botella reutilizable Höség', 'Höség', 'Accesorios', 'Urbana',    'Única', 'Botella de acero inoxidable, línea urbana sin compromiso social.',      39.90, 10, NULL,     TRUE, TRUE, '2023-01-15 10:00:00-05');

-- ---------- ONG y comunidades altoandinas -----------------------------
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
    ('Textiles Ausangate S.A.C.',      '20456789123', 'Marco Quispe',    '+51 984 555 111'),
    ('Confecciones Andina E.I.R.L.',   '20489321456', 'Lucía Ttito',     '+51 984 555 222'),
    ('Manos de Ausangate E.I.R.L.',    '20512345678', 'Rosa Huamán',     '+51 984 555 333'),
    ('Textiles Vilcanota S.A.C.',      '20498765432', 'Edwin Ccahuana',  '+51 984 555 444'),
    ('Artesanía Textil Qero S.R.L.',   '20534567891', 'Nelly Pauccar',   '+51 984 555 555'),
    ('Hilados del Sur Peruano S.A.C.', '20567891234', 'César Condori',   '+51 984 555 666'),
    ('Taller Comunitario Chinchero',   '20591234567', 'Beatriz Sallo',   '+51 984 555 777');

-- ---------- Clientes (integrantes del grupo + clientes web) -----------
INSERT INTO cliente (nombre, apellido, tipo_doc, num_doc, email, telefono, creado_en) VALUES
    ('Aarón Asdrúbal', 'Anca Vásquez',      'DNI', '72400704', 'aaron.anca@hoseg-demo.pe',        '+51 987 101 001', '2023-02-10 10:00:00-05'),
    ('Najhely Paola',  'Banda Sanchez',     'DNI', '72621025', 'najhely.banda@hoseg-demo.pe',     '+51 987 101 002', '2023-03-14 11:30:00-05'),
    ('Anghela Queen',  'Callo Valdivia',    'DNI', '72357208', 'anghela.callo@hoseg-demo.pe',     '+51 987 101 003', '2023-04-20 15:00:00-05'),
    ('Axel David',     'Chumpitaz Muñoz',   'DNI', '72280401', 'axel.chumpitaz@hoseg-demo.pe',    '+51 987 101 004', '2023-05-05 16:20:00-05'),
    ('Luis Diego',     'Ochoa Uribe',       'DNI', '72362449', 'luis.ochoa@hoseg-demo.pe',        '+51 987 101 005', '2023-06-12 09:45:00-05'),
    ('María Fernanda', 'Gutiérrez',         'DNI', '45678912', 'maria.gutierrez@example.com',     '+51 987 111 222', '2023-07-01 12:00:00-05'),
    ('Jorge Luis',     'Ramírez',           'DNI', '41234567', 'jorge.ramirez@example.com',       '+51 987 222 333', '2024-01-18 14:10:00-05'),
    ('Ana Belén',      'Torres',            'DNI', '47812365', 'ana.torres@example.com',          '+51 987 333 444', '2024-05-22 17:00:00-05'),
    ('Camila',         'Rodríguez Paredes', 'DNI', '48912345', 'camila.rodriguez@example.com',    '+51 987 444 555', '2025-02-11 10:30:00-05'),
    ('Diego Alonso',   'Vega',              'DNI', '46123789', 'diego.vega@example.com',          '+51 987 555 666', '2025-08-09 18:15:00-05');

DROP TABLE IF EXISTS _lote, _venta, _linea;

CREATE TABLE _lote (
    clave TEXT, comunidad TEXT, estado TEXT,
    creado INTERVAL, despacho INTERVAL, entrega INTERVAL, observaciones TEXT
);

INSERT INTO _lote VALUES
    ('H1', 'Layo',        'ENTREGADO', '980 days', '976 days', '972 days', 'Campaña invierno 2023 - Sierra Sur.'),
    ('H2', 'Marcapata',   'ENTREGADO', '620 days', '616 days', '611 days', 'Entrega anual comunidades altoandinas 2024.'),
    ('H3', 'Omacha',      'ENTREGADO', '280 days', '275 days', '270 days', 'Campaña friaje 2025 - ONG Pachamama Raymi.'),
    ('A',  'Omacha',      'ENTREGADO', '22 days',  '20 days',  '18 days',  'Primera entrega de temporada 2026.'),
    ('B',  'Paucartambo', 'ENTREGADO', '10 days',  '8 days',   '5 days',   'Entrega validada en campo.'),
    ('C',  'Ccatca',      'EN_RUTA',   '6 days',   '4 days',   NULL,       'En tránsito hacia Ccatca.'),
    ('D',  'Marcapata',   'PENDIENTE', '2 days',   NULL,       NULL,       'Programado para salida semanal.');

CREATE TABLE _venta (
    comprobante TEXT, num_doc TEXT, estado TEXT, hace INTERVAL, lote TEXT
);

INSERT INTO _venta VALUES
    ('WEB-000081', '72400704', 'PAGADO',    '990 days', 'H1'),
    ('WEB-000082', '72621025', 'PAGADO',    '985 days', 'H1'),
    ('WEB-000083', '72357208', 'PAGADO',    '630 days', 'H2'),
    ('WEB-000084', '72280401', 'PAGADO',    '625 days', 'H2'),
    ('WEB-000085', '72362449', 'PAGADO',    '290 days', 'H3'),
    ('WEB-000086', '45678912', 'PAGADO',    '285 days', 'H3'),
    ('WEB-000095', '72621025', 'PAGADO',    '36 days',  NULL),
    ('WEB-000096', '72400704', 'PAGADO',    '19 days',  NULL),
    ('WEB-000101', '72621025', 'PENDIENTE', '2 days',   NULL),
    ('WEB-000102', '72400704', 'PENDIENTE', '1 day',    NULL),
    ('WEB-000103', '72357208', 'PENDIENTE', '18 hours', NULL),
    ('WEB-000104', '72280401', 'PENDIENTE', '6 hours',  NULL),
    ('WEB-000105', '72362449', 'PENDIENTE', '2 hours',  NULL),
    ('WEB-000106', '47812365', 'PAGADO',    '9 days',   NULL),
    ('WEB-000107', '48912345', 'PAGADO',    '8 days',   'C'),
    ('WEB-000108', '46123789', 'PAGADO',    '14 days',  'B'),
    ('WEB-000109', '41234567', 'ANULADO',   '5 days',   NULL),
    ('WEB-000110', '72621025', 'PAGADO',    '25 days',  'A'),
    ('WEB-000111', '72357208', 'PAGADO',    '24 days',  'A'),
    ('WEB-000112', '72362449', 'PAGADO',    '4 days',   'D');

CREATE TABLE _linea (
    comprobante TEXT, codigo TEXT, cantidad INTEGER
);

INSERT INTO _linea VALUES
    ('WEB-000081', 'HSG-CAS-001', 2), ('WEB-000081', 'HSG-ACC-001', 3),
    ('WEB-000082', 'HSG-CAS-002', 2), ('WEB-000082', 'HSG-ACC-002', 2),
    ('WEB-000083', 'HSG-CAS-003', 2), ('WEB-000083', 'HSG-ACC-003', 3),
    ('WEB-000084', 'HSG-CAS-004', 2), ('WEB-000084', 'HSG-ACC-005', 4),
    ('WEB-000085', 'HSG-CAS-001', 3), ('WEB-000085', 'HSG-ACC-006', 5),
    ('WEB-000086', 'HSG-CAS-005', 2), ('WEB-000086', 'HSG-ACC-002', 3),
    ('WEB-000095', 'HSG-CAS-003', 1),
    ('WEB-000096', 'HSG-ACC-001', 2),
    ('WEB-000101', 'HSG-CAS-001', 1),
    ('WEB-000102', 'HSG-ACC-001', 2),
    ('WEB-000103', 'HSG-CAS-003', 1), ('WEB-000103', 'HSG-ACC-002', 1),
    ('WEB-000104', 'HSG-CAS-004', 1),
    ('WEB-000105', 'HSG-CAS-005', 1), ('WEB-000105', 'HSG-ACC-003', 2),
    ('WEB-000106', 'HSG-ACC-005', 3),
    ('WEB-000107', 'HSG-ACC-006', 4),
    ('WEB-000108', 'HSG-CAS-006', 2),
    ('WEB-000109', 'HSG-ACC-007', 1),
    ('WEB-000110', 'HSG-CAS-002', 1), ('WEB-000110', 'HSG-ACC-008', 1),
    ('WEB-000111', 'HSG-ACC-001', 2), ('WEB-000111', 'HSG-ACC-002', 1),
    ('WEB-000112', 'HSG-CAS-007', 1), ('WEB-000112', 'HSG-ACC-007', 1);

-- ---------- Ventas y su detalle ---------------------------------------
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
INSERT INTO pedido_proveedor (id_proveedor, codigo_producto, descripcion, cantidad, fecha, estado, id_usuario)
SELECT pr.id, x.codigo, x.descripcion, x.cantidad, now() - x.hace, x.estado, u.id
  FROM (VALUES
        ('Textiles Ausangate S.A.C.',      'HSG-ACC-004', 'Pedido duplicado, se anula',                              15, interval '20 days', 'ANULADO'),
        ('Textiles Vilcanota S.A.C.',      'HSG-ACC-006', 'Reposición de medias de alpaca',                          30, interval '15 days', 'RECIBIDO'),
        ('Manos de Ausangate E.I.R.L.',    'HSG-CAS-004', 'Reposición de casacas talla XL para temporada de heladas', 20, interval '2 days',  'SOLICITADO'),
        ('Textiles Ausangate S.A.C.',      'HSG-ACC-004', 'Reposición de chalinas Ccatca bajo stock mínimo',         25, interval '1 day',   'SOLICITADO')
       ) AS x (taller, codigo, descripcion, cantidad, hace, estado)
  JOIN proveedor pr ON pr.nombre_taller = x.taller
  JOIN usuario u    ON u.username = 'admin'
 ORDER BY x.hace DESC;
-- ---------- Bitácora de inventario (CU09) ----------------------------
-- Calibrado para dejar: 2 sin stock, 3 críticos y 3 próximos a crítico
INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha)
SELECT x.codigo, 'COMERCIAL', 'INGRESO', x.cantidad, 'Inventario inicial', 'ESCRITORIO', u.id,
       '2023-01-15 10:30:00-05'
  FROM (VALUES
        ('HSG-CAS-001', 35),
        ('HSG-CAS-002', 30),
        ('HSG-CAS-003', 3),
        ('HSG-CAS-004', 5),
        ('HSG-CAS-005', 25),
        ('HSG-CAS-006', 6),
        ('HSG-CAS-007', 9),
        ('HSG-ACC-001', 45),
        ('HSG-ACC-002', 40),
        ('HSG-ACC-003', 11),
        ('HSG-ACC-004', 3),
        ('HSG-ACC-005', 45),
        ('HSG-ACC-006', 60),
        ('HSG-ACC-007', 1),
        ('HSG-ACC-008', 14)
       ) AS x (codigo, cantidad)
  JOIN usuario u ON u.username = 'admin'
 ORDER BY x.codigo;

INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha, id_pedido_proveedor)
SELECT pp.codigo_producto, 'COMERCIAL', 'INGRESO', pp.cantidad, 'Recepción de pedido a proveedor', 'ESCRITORIO', u.id,
       pp.fecha + interval '2 days', pp.id
  FROM pedido_proveedor pp
  JOIN usuario u ON u.username = 'encargado'
 WHERE pp.estado = 'RECIBIDO';

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

INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha)
SELECT 'HSG-ACC-008', 'COMERCIAL', 'AJUSTE', -1, 'Merma: unidad abollada en almacén', 'ESCRITORIO', u.id,
       now() - interval '8 days'
  FROM usuario u WHERE u.username = 'encargado';

INSERT INTO movimiento_inventario
    (codigo_producto, tipo_stock, tipo_movimiento, cantidad, motivo, origen_sistema, id_usuario, fecha, id_lote)
SELECT d.codigo_producto, 'COMPROMETIDO', 'SALIDA', -d.cantidad, 'Entrega de lote de donación', 'ESCRITORIO',
       l.id_usuario_responsable, l.fecha_entrega, l.id
  FROM donacion d
  JOIN lote_donacion l ON l.id = d.id_lote
 WHERE l.estado = 'ENTREGADO'
 ORDER BY l.fecha_entrega, d.id;

-- ---------- Cierre y recálculo automático ---------------------------
UPDATE producto p
   SET stock_comercial    = COALESCE(s.comercial, 0),
       stock_comprometido = COALESCE(s.comprometido, 0)
  FROM (SELECT codigo_producto,
               SUM(cantidad) FILTER (WHERE tipo_stock = 'COMERCIAL')    AS comercial,
               SUM(cantidad) FILTER (WHERE tipo_stock = 'COMPROMETIDO') AS comprometido
          FROM movimiento_inventario
         GROUP BY codigo_producto) s
 WHERE s.codigo_producto = p.codigo;

UPDATE lote_donacion SET codigo_lote = 'HSG-L' || lpad(id::text, 3, '0');

SELECT setval('seq_comprobante_web',
              (SELECT MAX(substring(codigo_comprobante FROM '^WEB-(\d+)$')::integer)
                 FROM venta WHERE codigo_comprobante ~ '^WEB-\d+$'),
              true);

DROP TABLE _lote, _venta, _linea;

COMMIT;
