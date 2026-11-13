-- =====================================================================
-- SEGITD-HÖSÉG (ADSI) · Reiniciar la base (BORRA TODO)
-- Elimina las 14 tablas y sus datos para volver a cargar 01 y 02 desde
-- cero, por ejemplo antes de la sustentación. No se puede deshacer.
-- Sobre una base vacía no hace nada.
-- =====================================================================

BEGIN;

DROP TABLE IF EXISTS
    movimiento_inventario, pedido_proveedor, proveedor,
    donacion, lote_donacion, ong, comunidad,
    detalle_venta, venta, cliente,
    producto, coleccion, categoria, usuario,
    _lote, _venta, _linea          -- auxiliares de 02 si quedó a medias
    CASCADE;

DROP SEQUENCE IF EXISTS seq_comprobante_web;

COMMIT;
