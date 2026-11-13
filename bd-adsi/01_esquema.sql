-- =====================================================================
-- SEGITD-HÖSÉG (ADSI) · Esquema de la base de datos del escritorio
-- PostgreSQL / Supabase · proyecto exclusivo del sistema de escritorio
--
-- Es el estado final de los scripts 01..07 del repositorio original,
-- consolidado en un solo archivo para una base NUEVA y vacía. No incluye
-- 08 ni 09 (envío, reseñas, café, contenido y demás tablas del portal
-- web). Las 14 tablas son las del modelo físico del informe:
--   usuario, cliente, categoria, coleccion, producto, venta,
--   detalle_venta, comunidad, ong, lote_donacion, donacion, proveedor,
--   pedido_proveedor, movimiento_inventario
--
-- Cambios respecto de 01..07:
--   · usuario.rol admite GESTOR_IMPACTO (gestor de impacto social).
--   · uq_donacion_detalle: una línea de venta genera como máximo una
--     donación (CU04, alternativa A3: evitar duplicar el impacto).
--
-- Orden de ejecución: 01_esquema → 02_datos_demo → 04_verificacion.
-- 03_simular_pedido_web se ejecuta durante la demostración.
-- Todo corre en una transacción: si algo falla, no queda nada a medias.
-- =====================================================================

BEGIN;

-- ---------- Usuarios internos (personal de Höség) --------------------
-- No confundir con los clientes (tabla cliente).
CREATE TABLE usuario (
    id                SERIAL PRIMARY KEY,
    nombre            VARCHAR(120) NOT NULL,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(64)  NOT NULL,          -- SHA-256 en hexadecimal
    salt              VARCHAR(32)  NOT NULL,
    rol               VARCHAR(20)  NOT NULL
                      CONSTRAINT usuario_rol_check
                      CHECK (rol IN ('ADMINISTRADOR', 'ENCARGADO', 'GESTOR_IMPACTO')),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INTEGER      NOT NULL DEFAULT 0,
    bloqueado_hasta   TIMESTAMPTZ,
    creado_en         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------- Tablas de referencia del catálogo ------------------------
-- La clave es el propio nombre: producto.categoria sigue siendo texto,
-- pero solo admite valores registrados, y renombrar se propaga.
CREATE TABLE categoria (
    nombre VARCHAR(60) PRIMARY KEY
);

CREATE TABLE coleccion (
    nombre VARCHAR(80) PRIMARY KEY
);

-- ---------- Catálogo de productos ------------------------------------
CREATE TABLE producto (
    codigo                VARCHAR(30)   PRIMARY KEY,
    nombre                VARCHAR(150)  NOT NULL,
    marca                 VARCHAR(80),
    categoria             VARCHAR(60)   NOT NULL
                          CONSTRAINT producto_categoria_fkey
                          REFERENCES categoria (nombre) ON UPDATE CASCADE,
    coleccion             VARCHAR(80)
                          CONSTRAINT producto_coleccion_fkey
                          REFERENCES coleccion (nombre) ON UPDATE CASCADE,
    talla                 VARCHAR(10),
    descripcion           TEXT,
    url_imagen            TEXT,
    precio                NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    stock_comercial       INTEGER NOT NULL DEFAULT 0 CHECK (stock_comercial    >= 0),
    stock_comprometido    INTEGER NOT NULL DEFAULT 0 CHECK (stock_comprometido >= 0),
    stock_minimo          INTEGER NOT NULL DEFAULT 5,
    tipo_compromiso       VARCHAR(10)
                          CHECK (tipo_compromiso IN ('ABRIGO', 'ARBOL')),
    -- Derivada: la calcula la base, se lee pero no se escribe.
    aplica_triple_impacto BOOLEAN GENERATED ALWAYS AS (tipo_compromiso IS NOT NULL) STORED,
    visible_web           BOOLEAN NOT NULL DEFAULT TRUE,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- Clientes --------------------------------------------------
-- direccion y password_hash vienen de 06 (cuenta del comprador web); el
-- escritorio no las usa, se conservan para que el esquema sea 01..07.
CREATE TABLE cliente (
    id            SERIAL PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    apellido      VARCHAR(100) NOT NULL,
    tipo_doc      VARCHAR(10),
    num_doc       VARCHAR(20),
    email         VARCHAR(150) NOT NULL,
    telefono      VARCHAR(20),
    direccion     VARCHAR(255),
    password_hash VARCHAR(72),                         -- BCrypt; NULL = sin cuenta
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT cliente_documento_check CHECK (
        (tipo_doc IS NULL AND num_doc IS NULL) OR
        (tipo_doc = 'DNI'       AND num_doc ~ '^[0-9]{8}$') OR
        (tipo_doc = 'RUC'       AND num_doc ~ '^[0-9]{11}$') OR
        (tipo_doc = 'CE'        AND num_doc ~ '^[A-Za-z0-9]{9,12}$') OR
        (tipo_doc = 'PASAPORTE' AND num_doc ~ '^[A-Za-z0-9]{6,12}$')
    ),
    CONSTRAINT cliente_documento_key UNIQUE (tipo_doc, num_doc)
);

CREATE UNIQUE INDEX uq_cliente_email ON cliente (lower(email));

-- ---------- Ventas / pedidos -----------------------------------------
-- Llegan del canal web como PENDIENTE; el escritorio las confirma
-- (PAGADO) o las anula (CU02).
CREATE TABLE venta (
    id                 SERIAL PRIMARY KEY,
    codigo_comprobante VARCHAR(30) NOT NULL UNIQUE,
    fecha              TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_cliente         INTEGER REFERENCES cliente (id),
    id_usuario         INTEGER REFERENCES usuario (id),  -- quién la confirmó o anuló
    origen             VARCHAR(12) NOT NULL DEFAULT 'WEB'
                       CHECK (origen IN ('WEB', 'PRESENCIAL')),
    estado             VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                       CHECK (estado IN ('PENDIENTE', 'PAGADO', 'ANULADO')),
    total              NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- Correlativo de comprobantes WEB-nnnnnn (06). Lo usa
-- 03_simular_pedido_web.sql; 02_datos_demo.sql lo deja al día.
CREATE SEQUENCE seq_comprobante_web MINVALUE 0 START WITH 0;

CREATE TABLE detalle_venta (
    id              SERIAL PRIMARY KEY,
    id_venta        INTEGER NOT NULL REFERENCES venta (id) ON DELETE CASCADE,
    codigo_producto VARCHAR(30) NOT NULL REFERENCES producto (codigo),
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10,2) NOT NULL,
    subtotal        NUMERIC(10,2) NOT NULL,
    CONSTRAINT detalle_venta_importes_check
        CHECK (precio_unitario >= 0 AND subtotal = cantidad * precio_unitario)
);

-- ---------- Destinos de donación -------------------------------------
CREATE TABLE comunidad (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    distrito  VARCHAR(80),
    provincia VARCHAR(80),
    region    VARCHAR(80) NOT NULL DEFAULT 'Cusco'
);

CREATE TABLE ong (
    id       SERIAL PRIMARY KEY,
    nombre   VARCHAR(120) NOT NULL,
    contacto VARCHAR(120),
    telefono VARCHAR(20)
);

-- fecha_despacho: salida (EN_RUTA) · fecha_entrega: llegada (ENTREGADO)
CREATE TABLE lote_donacion (
    id                     SERIAL PRIMARY KEY,
    codigo_lote            VARCHAR(30) NOT NULL UNIQUE,  -- HSG-Lnnn, sale del id
    id_comunidad           INTEGER NOT NULL REFERENCES comunidad (id),
    id_ong                 INTEGER NOT NULL REFERENCES ong (id),
    id_usuario_responsable INTEGER REFERENCES usuario (id),
    fecha_creacion         TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_despacho         TIMESTAMPTZ,
    fecha_entrega          TIMESTAMPTZ,
    estado                 VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                           CHECK (estado IN ('PENDIENTE', 'EN_RUTA', 'ENTREGADO')),
    observaciones          TEXT,
    CONSTRAINT lote_donacion_fechas_segun_estado_check CHECK (
        (estado = 'PENDIENTE' AND fecha_despacho IS NULL     AND fecha_entrega IS NULL) OR
        (estado = 'EN_RUTA'   AND fecha_despacho IS NOT NULL AND fecha_entrega IS NULL) OR
        (estado = 'ENTREGADO' AND fecha_despacho IS NOT NULL AND fecha_entrega IS NOT NULL)
    ),
    CONSTRAINT lote_donacion_orden_fechas_check
        CHECK (fecha_creacion <= fecha_despacho AND fecha_despacho <= fecha_entrega)
);

-- ---------- Donación: la tabla bisagra del sistema -------------------
-- Cada compromiso social nace de una línea de venta y termina en un lote.
-- Ciclo de vida:
--   EN_ESPERA (venta sin pagar) → PENDIENTE (pago confirmado, lista para
--   lote) → ASIGNADA (en un lote) → ENTREGADA
CREATE TABLE donacion (
    id               SERIAL PRIMARY KEY,
    id_detalle_venta INTEGER NOT NULL REFERENCES detalle_venta (id),
    codigo_producto  VARCHAR(30) NOT NULL REFERENCES producto (codigo),
    cantidad         INTEGER NOT NULL CHECK (cantidad > 0),
    tipo             VARCHAR(10) NOT NULL CHECK (tipo IN ('ABRIGO', 'ARBOL')),
    estado           VARCHAR(12) NOT NULL DEFAULT 'EN_ESPERA'
                     CONSTRAINT donacion_estado_check
                     CHECK (estado IN ('EN_ESPERA', 'PENDIENTE', 'ASIGNADA', 'ENTREGADA')),
    id_lote          INTEGER REFERENCES lote_donacion (id),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Solo las donaciones asignadas o entregadas tienen lote.
    CONSTRAINT donacion_lote_segun_estado_check
        CHECK ((estado IN ('EN_ESPERA', 'PENDIENTE')) = (id_lote IS NULL))
);

-- CU04 A3: una línea de venta genera como máximo una donación.
CREATE UNIQUE INDEX uq_donacion_detalle ON donacion (id_detalle_venta);

-- ---------- Proveedores ----------------------------------------------
CREATE TABLE proveedor (
    id            SERIAL PRIMARY KEY,
    nombre_taller VARCHAR(150) NOT NULL,
    ruc           VARCHAR(11)
                  CONSTRAINT proveedor_ruc_check CHECK (ruc ~ '^[0-9]{11}$')
                  CONSTRAINT proveedor_ruc_key UNIQUE,
    contacto      VARCHAR(120),
    telefono      VARCHAR(20),
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE pedido_proveedor (
    id              SERIAL PRIMARY KEY,
    id_proveedor    INTEGER NOT NULL REFERENCES proveedor (id),
    codigo_producto VARCHAR(30) REFERENCES producto (codigo),  -- producto que repone
    descripcion     VARCHAR(255) NOT NULL,
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado          VARCHAR(12) NOT NULL DEFAULT 'SOLICITADO'
                    CHECK (estado IN ('SOLICITADO', 'RECIBIDO', 'ANULADO')),
    id_usuario      INTEGER REFERENCES usuario (id)
);

-- ---------- Bitácora de inventario (CU09) ----------------------------
-- Ningún stock se modifica sin registrar aquí. Cada movimiento apunta
-- como máximo a un origen (venta, lote o pedido a proveedor); los
-- ajustes manuales no tienen origen y se explican en motivo.
CREATE TABLE movimiento_inventario (
    id                  SERIAL PRIMARY KEY,
    codigo_producto     VARCHAR(30) NOT NULL REFERENCES producto (codigo),
    tipo_stock          VARCHAR(15) NOT NULL
                        CHECK (tipo_stock IN ('COMERCIAL', 'COMPROMETIDO')),
    tipo_movimiento     VARCHAR(10) NOT NULL
                        CHECK (tipo_movimiento IN ('INGRESO', 'SALIDA', 'AJUSTE')),
    cantidad            INTEGER NOT NULL,
    motivo              VARCHAR(150),
    origen_sistema      VARCHAR(12) NOT NULL DEFAULT 'ESCRITORIO'
                        CHECK (origen_sistema IN ('ESCRITORIO', 'WEB')),
    id_usuario          INTEGER REFERENCES usuario (id),
    fecha               TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_venta            INTEGER REFERENCES venta (id),
    id_lote             INTEGER REFERENCES lote_donacion (id),
    id_pedido_proveedor INTEGER REFERENCES pedido_proveedor (id),
    CONSTRAINT movimiento_inventario_un_origen_check
        CHECK (num_nonnulls(id_venta, id_lote, id_pedido_proveedor) <= 1)
);

-- ---------- Índices ---------------------------------------------------
CREATE INDEX idx_donacion_estado          ON donacion (estado);
CREATE INDEX idx_donacion_lote            ON donacion (id_lote);
CREATE INDEX idx_donacion_producto        ON donacion (codigo_producto);
CREATE INDEX idx_venta_estado_fecha       ON venta (estado, fecha);
CREATE INDEX idx_venta_cliente            ON venta (id_cliente);
CREATE INDEX idx_venta_usuario            ON venta (id_usuario);
CREATE INDEX idx_detalle_venta_venta      ON detalle_venta (id_venta);
CREATE INDEX idx_detalle_venta_producto   ON detalle_venta (codigo_producto);
CREATE INDEX idx_lote_comunidad           ON lote_donacion (id_comunidad);
CREATE INDEX idx_lote_ong                 ON lote_donacion (id_ong);
CREATE INDEX idx_lote_responsable         ON lote_donacion (id_usuario_responsable);
CREATE INDEX idx_pedido_prov_producto     ON pedido_proveedor (codigo_producto);
CREATE INDEX idx_pedido_prov_proveedor    ON pedido_proveedor (id_proveedor);
CREATE INDEX idx_pedido_prov_usuario      ON pedido_proveedor (id_usuario);
CREATE INDEX idx_mov_producto_fecha       ON movimiento_inventario (codigo_producto, fecha);
CREATE INDEX idx_mov_usuario              ON movimiento_inventario (id_usuario);
CREATE INDEX idx_mov_venta                ON movimiento_inventario (id_venta);
CREATE INDEX idx_mov_lote                 ON movimiento_inventario (id_lote);
CREATE INDEX idx_mov_pedido_proveedor     ON movimiento_inventario (id_pedido_proveedor);
CREATE INDEX idx_producto_activo          ON producto (activo);
CREATE INDEX idx_producto_categoria       ON producto (categoria);
CREATE INDEX idx_producto_coleccion       ON producto (coleccion);

-- ---------- API pública de Supabase cerrada ---------------------------
-- El escritorio se conecta por JDBC como postgres (dueño de las tablas,
-- no le afecta el RLS). Los roles anon y authenticated, que usa la clave
-- pública del proyecto, no deben ver nada: RLS activo sin políticas y
-- sin permisos, también para las tablas que se creen después.
ALTER TABLE usuario               ENABLE ROW LEVEL SECURITY;
ALTER TABLE categoria             ENABLE ROW LEVEL SECURITY;
ALTER TABLE coleccion             ENABLE ROW LEVEL SECURITY;
ALTER TABLE producto              ENABLE ROW LEVEL SECURITY;
ALTER TABLE cliente               ENABLE ROW LEVEL SECURITY;
ALTER TABLE venta                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE detalle_venta         ENABLE ROW LEVEL SECURITY;
ALTER TABLE comunidad             ENABLE ROW LEVEL SECURITY;
ALTER TABLE ong                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE lote_donacion         ENABLE ROW LEVEL SECURITY;
ALTER TABLE donacion              ENABLE ROW LEVEL SECURITY;
ALTER TABLE proveedor             ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedido_proveedor      ENABLE ROW LEVEL SECURITY;
ALTER TABLE movimiento_inventario ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON TABLES    FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON SEQUENCES FROM anon, authenticated;

COMMIT;
