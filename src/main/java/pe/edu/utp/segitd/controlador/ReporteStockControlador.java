package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.ReporteStock;
import pe.edu.utp.segitd.servicio.ReporteStockService;
import pe.edu.utp.segitd.util.ReporteStockExcelExporter;

import java.io.File;
import java.io.IOException;

public class ReporteStockControlador {

    private final ReporteStockService reporteStockService = new ReporteStockService();
    private final ReporteStockExcelExporter excelExporter = new ReporteStockExcelExporter();

    public ReporteStock generarReporte() {
        return reporteStockService.generarReporte();
    }

    public File exportarAExcel(ReporteStock reporte) throws IOException {
        return excelExporter.exportar(reporte.sinStock(), reporte.criticos(), reporte.disponibles());
    }
}
