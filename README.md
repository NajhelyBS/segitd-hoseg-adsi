# SEGITD-HÖSÉG — Módulo de Escritorio

Back-office administrativo de **Höség Store (14-DIEZ S.A.C.)** desarrollado en **Java 25 + Swing + JDBC** conectado a **PostgreSQL en Supabase Cloud** [SEGITD-HOSEG.md]. Consulta la especificación técnica en [SEGITD-HOSEG.md] y el protocolo de pruebas en [GUIA_PRUEBAS_SISTEMA.md].

## Requisitos y Base de Datos (`bd-adsi/`)
- **Requisitos:** JDK 25, Maven 3.9+ y base de datos PostgreSQL/Supabase.
- **Modelo físico:** 14 tablas normalizadas (`usuario`, `cliente`, `producto`, `venta`, `lote_donacion`, `ong`, etc.) [SEGITD-HOSEG.md].
- **Scripts de inicialización (en orden):**
  1. `bd-adsi/00_reiniciar.sql`: Limpia el esquema público.
  2. `bd-adsi/01_esquema.sql`: Crea las 14 tablas y restricciones.
  3. `bd-adsi/02_datos_demo.sql`: Carga usuarios (`admin`, `encargado`, `gestor`), catálogo y ONG [SEGITD-HOSEG.md].
  4. `bd-adsi/03_simular_pedido_web.sql`: Inserta un pedido web de demostración.
  5. `bd-adsi/04_verificacion.sql`: Consultas de verificación rápida.

## Configuración y Ejecución
1. Configura `config.properties` a partir de `config.properties.example` con las credenciales de Supabase (puerto 5432) [SEGITD-HOSEG.md].
2. Compila y ejecuta mediante Maven:
   ```bash
   mvn compile exec:java
   mvn package
   java -jar target/segitd-hoseg-desktop.jar
   ```

## Archivos Generados y Arquitectura
- **Archivos generados:** Reportes Excel (`reporte_impacto_<timestamp>.xlsx`), boletas digitales y certificados en PDF, y respaldos en CSV [SEGITD-HOSEG.md].
- **Arquitectura:** Capas estrictas (`vista` → `controlador` → `servicio` → `dao` → `db`) [SEGITD-HOSEG.md].
