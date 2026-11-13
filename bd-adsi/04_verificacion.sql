-- =====================================================================
-- SEGITD-HÖSÉG (ADSI) · Verificación de la base (solo lectura)
-- Ejecutar después de 02_datos_demo.sql, y cuando se quiera comprobar
-- que la base sigue coherente tras usar el escritorio.
-- Devuelve una sola tabla (el editor SQL de Supabase solo muestra el
-- último resultado): todas las filas deben salir con ok = true.
-- =====================================================================

SELECT verificacion, esperado, obtenido, esperado = obtenido AS ok
  FROM (VALUES
    ('Tablas del modelo físico', 14,
     (SELECT COUNT(*)::int FROM information_schema.tables
       WHERE table_schema = 'public' AND table_type = 'BASE TABLE')),

    ('Tablas sin RLS activo', 0,
     (SELECT COUNT(*)::int FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
       WHERE n.nspname = 'public' AND c.relkind = 'r' AND NOT c.relrowsecurity)),

    ('Permisos de anon/authenticated sobre tablas', 0,
     (SELECT COUNT(*)::int FROM information_schema.role_table_grants
       WHERE table_schema = 'public' AND grantee IN ('anon', 'authenticated'))),

    ('Roles de usuario distintos (admin, encargado, gestor)', 3,
     (SELECT COUNT(DISTINCT rol)::int FROM usuario WHERE activo)),

    ('Stock comercial distinto de la suma de la bitácora', 0,
     (SELECT COUNT(*)::int FROM producto p
       WHERE p.stock_comercial <> (SELECT COALESCE(SUM(m.cantidad), 0) FROM movimiento_inventario m
                                    WHERE m.codigo_producto = p.codigo AND m.tipo_stock = 'COMERCIAL'))),

    ('Stock comprometido distinto de donaciones PENDIENTE + ASIGNADA', 0,
     (SELECT COUNT(*)::int FROM producto p
       WHERE p.stock_comprometido <> (SELECT COALESCE(SUM(d.cantidad), 0) FROM donacion d
                                       WHERE d.codigo_producto = p.codigo
                                         AND d.estado IN ('PENDIENTE', 'ASIGNADA')))),

    ('Donaciones listas para lote de ventas no pagadas', 0,
     (SELECT COUNT(*)::int FROM donacion d
        JOIN detalle_venta dv ON dv.id = d.id_detalle_venta
        JOIN venta v ON v.id = dv.id_venta
       WHERE d.estado <> 'EN_ESPERA' AND v.estado <> 'PAGADO')),

    ('Líneas de venta con más de una donación', 0,
     (SELECT COUNT(*)::int FROM (SELECT id_detalle_venta FROM donacion
                                  GROUP BY 1 HAVING COUNT(*) > 1) x)),

    ('Ventas cuyo total no cuadra con su detalle', 0,
     (SELECT COUNT(*)::int FROM venta v
       WHERE v.total <> (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta WHERE id_venta = v.id))),

    ('Lotes con código provisional', 0,
     (SELECT COUNT(*)::int FROM lote_donacion WHERE codigo_lote NOT LIKE 'HSG-L%')),

    ('Productos activos bajo stock mínimo (demo CU07)', 2,
     (SELECT COUNT(*)::int FROM producto WHERE activo AND stock_comercial < stock_minimo))
  ) AS t (verificacion, esperado, obtenido);
