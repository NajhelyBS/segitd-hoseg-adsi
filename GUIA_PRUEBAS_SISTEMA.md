# GUÍA DE PRUEBAS, EJECUCIÓN Y GUION DE DEMOSTRACIÓN (SEGITD)

**Sistema de Gestión de Inventario, Transacciones de Ventas y Logística de Donaciones — Höség Store (14-DIEZ S.A.C.)**  
**Universidad Tecnológica del Perú (UTP)**  
**Curso:** Análisis y Diseño de Sistemas de Información (Sección 15223)  
**Docente:** Ing. Eduardo Martin Reyes Rodriguez  
**Integrantes:**
1. Anca Vásquez, Aarón Asdrúbal (Cod. U24200704)
2. Banda Sanchez, Najhely Paola (Cod. U24262102)
3. Callo Valdivia, Anghela Queen (Cod. U24235720)
4. Chumpitaz Muñoz, Axel David (Cod. U24228040)
5. Ochoa Uribe, Luis Diego (Cod. U24236244)

---

## 1. Configuración de Base de Datos y Entorno

1. **Scripts SQL (`bd-adsi/`):** Ejecutar en el SQL Editor de Supabase en orden (`00` a `04`) para crear las **14 tablas** del modelo físico documentado, cargar el catálogo inicial, comunidades altoandinas, ONG Pachamama Raymi y los tres usuarios de prueba (`admin`, `encargado` y `gestor`).
2. **Archivo `config.properties`:** Crear el archivo `config.properties` en la raíz del proyecto a partir de `config.properties.example`. Este archivo está protegido por `.gitignore` y **no se sube al repositorio**.

```properties
# CONFIGURACIÓN LOCAL (Reemplazar con las credenciales del proyecto Supabase)
db.url=jdbc:postgresql://<HOST_SUPABASE>:5432/postgres?sslmode=require
db.user=<USUARIO_POSTGRES>
db.password=<CONTRASEÑA_SUPABASE>
db.pool.size=5
portal.consulta.url=http://localhost:8080/consulta-impacto
```

---

## 2. Comandos de Compilación y Ejecución

1. **Compilación y ejecución rápida en desarrollo:**
   ```bash
   mvn compile exec:java
   ```
2. **Generación del archivo ejecutable `.jar` único con dependencias:**
   ```bash
   mvn package
   ```
3. **Ejecución autónoma desde terminal:**
   ```bash
   java -jar target/segitd-hoseg-desktop.jar
   ```

---

## 3. Protocolo de Pruebas por Caso de Uso (CU01 – CU10)

### MÓDULO 1: Seguridad y Control de Accesos por Rol (CU01)
* **Caso de Prueba 1.1 - Inicio de Sesión y Cifrado:** Ingresar con `admin`, `encargado` o `gestor`. Las contraseñas se validan mediante cifrado **SHA-256 + salt de 16 bytes** (`HashUtil`).
* **Caso de Prueba 1.2 - Bloqueo de Seguridad Activo:** Al simular 3 intentos fallidos consecutivos, el sistema bloquea la cuenta durante 5 minutos mediante `bloqueado_hasta`.
* **Caso de Prueba 1.3 - Control de Accesos por Rol:**
  * `ADMINISTRADOR` (`admin`): Acceso completo a los 6 módulos, confirmación/anulación de pedidos, creación de órdenes a proveedores, gestión de usuarios y respaldos.
  * `ENCARGADO` (`encargado`): Acceso a *Gestión de productos* (solo botón **Ajustar stock**, CU03), *Pedidos a proveedores* (recepción de mercadería, CU08) y *Pedidos web* en modo consulta.
  * `GESTOR_IMPACTO` (`gestor`): Acceso a *Despacho de lotes* (CU05/CU06), *Reportes de impacto* (CU10) y *Pedidos web* en modo consulta.

### MÓDULO 2: Pedidos Web, Inventario Dual y Alertas (CU02, CU03, CU04, CU07, CU09)
* **Caso de Prueba 2.1 - Consulta de Inventario Dual (CU03):** En `GestionProductosJFrame` se verifica la separación estricta entre **Stock Comercial** (disponible para venta) y **Stock Comprometido** (reservado para donación).
* **Caso de Prueba 2.2 - Protección del Stock Comprometido (CU03 / CU09):** Verificar que `InventarioService` rechaza cualquier reducción manual negativa sobre el stock comprometido y registra cada ajuste en `movimiento_inventario`.
* **Caso de Prueba 2.3 - Procesamiento Atómico de Pedidos Web (CU02 / CU04):** Al confirmar un pedido web `PENDIENTE` (creado con `bd-adsi/03_simular_pedido_web.sql`), `PedidoWebService` descuenta el stock comercial, incrementa el comprometido y pasa sus donaciones de `EN_ESPERA` a `PENDIENTE` en una sola transacción.
* **Caso de Prueba 2.4 - Notificación Automática de Stock Mínimo (CU07):** Al iniciar sesión como `admin` o tras confirmar un pedido web, si algún producto alcanza o baja de su `stock_minimo` (por ejemplo, `HSG-ACC-004`), el sistema despliega una alerta visual detallando los artículos críticos.

### MÓDULO 3: Abastecimiento con Cantidades Confirmadas (CU08)
* **Caso de Prueba 3.1 - Orden y Recepción con Validación de Diferencias:** El Administrador registra una orden de reposición (`SOLICITADO`). Posteriormente, el Encargado ingresa a *Pedidos a proveedores*, selecciona la orden e ingresa la cantidad recibida confirmada. Si difiere de la solicitada, el sistema emite una advertencia (flujo alternativo A3) antes de sumar las unidades recibidas al stock comercial.

### MÓDULO 4: Logística de Donaciones, Reportes y Certificado (CU05, CU06, CU10)
* **Caso de Prueba 4.1 - Gestión y Seguimiento de Lotes (CU05 / CU06):** Como `gestor`, agrupar donaciones `PENDIENTES` en un lote destinado a una comunidad altoandina en coordinación con la **ONG Pachamama Raymi**, actualizando su estado de `PENDIENTE` a `EN_RUTA` y `ENTREGADO`.
* **Caso de Prueba 4.2 - Reportes de Trazabilidad y Validaciones (CU10):** En *Reportes de impacto*, verificar que el sistema rechaza rangos de fechas inválidos (`Desde > Hasta`, flujo A1), avisa cuando no existen registros en el periodo (flujo A2) y exporta el libro `.xlsx` con 4 hojas (*Resumen*, *Trazabilidad*, *Historial de Despachos* e *Inventario*).
* **Caso de Prueba 4.3 - Certificado de Impacto B y Boleta PDF con QR:** Emitir tanto la boleta digital desde *Pedidos web* como el **Certificado de Impacto B** desde *Reportes de impacto*, validando que ambos PDFs incluyan métricas reales de la base de datos y su código QR de verificación.
