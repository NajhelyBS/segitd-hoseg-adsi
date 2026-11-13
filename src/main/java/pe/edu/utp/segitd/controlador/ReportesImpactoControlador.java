package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.FilaHistorialDespacho;
import pe.edu.utp.segitd.modelo.FilaTrazabilidad;
import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.servicio.CertificadoImpactoService;
import pe.edu.utp.segitd.servicio.ReporteService;
import pe.edu.utp.segitd.servicio.ResumenImpacto;
import pe.edu.utp.segitd.servicio.ServicioException;
import pe.edu.utp.segitd.util.ExcelExporter;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/** Traduce los eventos de ReportesImpactoJFrame a llamadas de ReporteService + ExcelExporter. */
public class ReportesImpactoControlador {

    private final ReporteService reporteService = new ReporteService();
    private final ExcelExporter excelExporter = new ExcelExporter();
    private final CertificadoImpactoService certificadoService = new CertificadoImpactoService();


    public List<Comunidad> listarComunidades() {
        return reporteService.listarComunidades();
    }

    public File exportar(OffsetDateTime desde, OffsetDateTime hasta, Integer idComunidad) throws IOException {
        // CU10 - Flujo alternativo A1: rango de fechas inválido
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new ServicioException(
                    "El rango de fechas es inválido: la fecha «Desde» no puede ser posterior a la fecha «Hasta».");
        }

        List<FilaTrazabilidad> trazabilidad = reporteService.generarTrazabilidad(desde, hasta, idComunidad);
        List<FilaHistorialDespacho> historial = reporteService.generarHistorialDespachos(desde, hasta, idComunidad);

        // CU10 - Flujo alternativo A2: sin resultados para los filtros seleccionados
        if (trazabilidad.isEmpty() && historial.isEmpty()) {
            throw new ServicioException(
                    "No se encontraron registros de ventas ni donaciones para el rango de fechas y filtros seleccionados.");
        }

        ResumenImpacto resumen = reporteService.calcularResumen(desde, hasta, historial);
        List<Producto> inventario = reporteService.listarInventario();
        return excelExporter.exportarReporteImpacto(desde, hasta, resumen, trazabilidad, historial, inventario);
    }

    public File emitirCertificado(OffsetDateTime desde, OffsetDateTime hasta, Comunidad comunidad) {
    return certificadoService.emitirCertificado(desde, hasta, comunidad);
}

}
