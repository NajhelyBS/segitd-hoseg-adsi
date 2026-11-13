# SEGITD-HÖSÉG — Módulo de Escritorio (Java Swing + Supabase)

Especificación técnica y de implementación del sistema back-office.  
**Universidad Tecnológica del Perú (UTP)**  
**Curso:** Análisis y Diseño de Sistemas de Información (Sección 15223)  
**Docente:** Ing. Eduardo Martin Reyes Rodriguez  
**Integrantes:**
- Anca Vásquez, Aarón Asdrúbal (Cod. U24200704)
- Banda Sanchez, Najhely Paola (Cod. U24262102)
- Callo Valdivia, Anghela Queen (Cod. U24235720)
- Chumpitaz Muñoz, Axel David (Cod. U24228040)
- Ochoa Uribe, Luis Diego (Cod. U24236244)

---

## 1. Contexto

**Empresa:** 14-DIEZ S.A.C., nombre comercial **Höség Store** (RUC 20566428386). Empresa B certificada peruana dedicada al diseño y comercialización de prendas outdoor sostenibles.

**Modelo de negocio (Triple Impacto):**
- *Programa de abrigo (`ABRIGO`)*: por cada prenda de abrigo comercializada, se destina una casaca térmica a niños y niñas de comunidades altoandinas (Cusco y Huancavelica).
- *Programa de reforestación (`ARBOL`)*: por cada producto con compromiso ambiental vendido, se planta un cedro andino en coordinación con la **ONG Pachamama Raymi**.

**Problema:** falta de integración y trazabilidad directa entre las ventas web, el control dual de existencias (stock comercial vs. stock comprometido) y la consolidación de lotes de donación para auditorías de Empresa B.

**Alcance del aplicativo:** solución de gestión interna (**back-office de escritorio**) en Java Swing + JDBC conectada a PostgreSQL (Supabase). Las ventas se originan en el canal web externo; **SEGITD** valida y confirma los pedidos, descuenta el stock comercial una única vez, genera los compromisos de donación y gestiona su despacho y trazabilidad.

---

## 2. Casos de Uso Implementados (CU01 – CU10)

| Código | Caso de Uso | Actores Autorizados | Pantalla / Componente |
|---|---|---|---|
| **CU01** | Iniciar sesión | Administrador, Encargado, Gestor de Impacto | `LoginJFrame` |
| **CU02** | Registrar ventas web | Administrador de Operaciones | `PedidosWebJFrame` |
| **CU03** | Gestionar stock de productos | Encargado de Almacén, Administrador | `GestionProductosJFrame` |
| **CU04** | Registrar impacto de ventas | Administrador (automático al confirmar CU02) | `PedidoWebService` |
| **CU05** | Gestionar lotes de donación | Gestor de Impacto Social, Administrador | `DespachoLotesJFrame` |
| **CU06** | Actualizar estado de lotes | Gestor de Impacto Social, Administrador | `DespachoLotesJFrame` |
| **CU07** | Notificar stock mínimo | Administrador de Operaciones (automático) | `AlertaStockUtil` |
| **CU08** | Gestionar abastecimiento | Administrador (órdenes) y Encargado (recepción) | `ProveedoresJFrame` |
| **CU09** | Registrar movimientos de inventario | Encargado de Almacén, Administrador | `MovimientoDAO` |
| **CU10** | Generar reportes de trazabilidad | Administrador, Gestor de Impacto Social | `ReportesImpactoJFrame` |

### Requisitos no funcionales clave
- **Seguridad (RFN02):** Contraseñas cifradas con **SHA-256 + salt aleatorio de 16 bytes** (`HashUtil`) y bloqueo de 5 minutos tras 3 intentos fallidos consecutivos.
- **Almacenamiento (RFN03):** Base de datos relacional PostgreSQL en Supabase con **14 tablas** normalizadas hasta 3FN.
- **Confiabilidad (RFN05):** Transacciones explícitas JDBC (`setAutoCommit(false)`) con rollback automático y respaldo CSV (`BackupService`).
- **Mantenibilidad:** Arquitectura estricta por capas (`vista` → `controlador` → `servicio` → `dao` → `db`).

---

## 3. Base de Datos (14 Tablas — `bd-adsi/`)

Los scripts de inicialización se encuentran en la carpeta `bd-adsi/` y replican exactamente el modelo físico documentado en el informe:

1. `bd-adsi/00_reiniciar.sql`: Reinicio limpio del esquema público.
2. `bd-adsi/01_schema.sql`: Creación de las **14 tablas** (`usuario`, `cliente`, `categoria`, `coleccion`, `producto`, `venta`, `detalle_venta`, `comunidad`, `ong`, `lote_donacion`, `donacion`, `proveedor`, `pedido_proveedor`, `movimiento_inventario`), claves foráneas, restricciones `CHECK` e índices.
3. `bd-adsi/02_datos_demo.sql`: Carga de usuarios (`admin`, `encargado`, `gestor`), catálogo inicial (incluyendo productos bajo stock mínimo como `HSG-ACC-004`), comunidades altoandinas, ONG Pachamama Raymi y proveedores.
4. `bd-adsi/03_simular_pedido_web.sql`: Inserción de un pedido web en estado `PENDIENTE` para demostraciones en vivo.
5. `bd-adsi/04_verificacion.sql`: Consultas rápidas de auditoría de tablas y saldos.

### Ciclos de estado
- **`venta.estado`:** `PENDIENTE` → `PAGADO` | `ANULADO`.
- **`donacion.estado`:** `EN_ESPERA` → `PENDIENTE` (al confirmar venta) → `ASIGNADA` (al crear lote) → `ENTREGADA` (al entregar lote).
- **`lote_donacion.estado`:** `PENDIENTE` → `EN_RUTA` (`fecha_despacho`) → `ENTREGADO` (`fecha_entrega`).
- **`pedido_proveedor.estado`:** `SOLICITADO` → `RECIBIDO` | `ANULADO`.

---

## 4. Seguridad de Credenciales

- La configuración de conexión se lee desde `config.properties` (en la raíz del proyecto), archivo incluido en `.gitignore` para evitar exponer credenciales en el repositorio.
- El repositorio incluye únicamente la plantilla `config.properties.example`.
- Se recomienda usar el **Session Pooler (puerto 5432)** de Supabase con `sslmode=require`.

---

## 5. Control de Accesos por Rol (`RolUsuario`)

| Módulo / Acción | `ADMINISTRADOR` (`admin`) | `ENCARGADO` (`encargado`) | `GESTOR_IMPACTO` (`gestor`) |
|---|---|---|---|
| **Gestión de productos** | Crear, editar, baja y ajustar stock | Solo **Ajustar stock** (CU03) | Deshabilitado |
| **Pedidos web** | Consulta, **Confirmar**, **Anular** y boleta PDF | Solo consulta y boleta PDF | Solo consulta y boleta PDF |
| **Despacho de lotes** | Total | Deshabilitado | Total (CU05 / CU06) |
| **Reportes de impacto y Certificado** | Total | Deshabilitado | Total (CU10) |
| **Pedidos a proveedores** | CRUD proveedores, crear órdenes y recibir | Solo **confirmar recepción** (CU08) | Deshabilitado |
| **Gestión de usuarios y Respaldo** | Total | Deshabilitado | Deshabilitado |

---

## 6. Estructura del Proyecto

```
segitd-hoseg-adsi/
├── pom.xml
├── README.md
├── SEGITD-HOSEG.md
├── GUIA_PRUEBAS_SISTEMA.md
├── .gitignore
├── config.properties.example
├── bd-adsi/                          # Scripts SQL de las 14 tablas
└── src/main/java/pe/edu/utp/segitd/
    ├── App.java                      # Punto de entrada (FlatLaf + LoginJFrame)
    ├── config/                       # AppConfig
    ├── db/                           # ConexionBD (HikariCP + JDBC)
    ├── modelo/                       # Entidades, DTOs (records) y enumeraciones
    ├── dao/                          # Acceso a datos con PreparedStatement
    ├── servicio/                     # Lógica de negocio, transacciones y PDFs
    ├── controlador/                  # Controladores MVC por vista
    ├── vista/                        # Interfaces gráficas Java Swing
    └── util/                         # HashUtil, ExcelExporter, AlertaStockUtil, Validador
```

---

## 7. Reglas de Negocio Transversales

1. **Concurrencia en inventario:** Las actualizaciones de stock delegan la aritmética a PostgreSQL (`stock_comercial = stock_comercial - ? WHERE codigo = ? AND stock_comercial >= ?`) dentro de transacciones JDBC para evitar saldos negativos o pérdida de actualizaciones concurrentes.
2. **Protección del stock comprometido:** El saldo reservado para donaciones solo aumenta al confirmar una venta con triple impacto y solo disminuye al entregar un lote (`ENTREGADO`), rechazando cualquier descuento manual negativo.
3. **Auditoría:** Toda variación de stock comercial o comprometido registra automáticamente un asiento en `movimiento_inventario` con usuario responsable, origen y motivo.
