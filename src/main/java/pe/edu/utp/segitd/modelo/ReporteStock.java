package pe.edu.utp.segitd.modelo;

import java.util.List;

/**
 * Resultado del reporte de stock (RF-03), ya clasificado en tres grupos
 * independientes: sin stock, críticos y disponibles.
 */
public record ReporteStock(List<FilaReporteStock> sinStock, List<FilaReporteStock> criticos,
                           List<FilaReporteStock> disponibles) {

    public int totalCriticos() {
        return criticos.size();
    }

    public int totalProximosAAgotar() {
        return (int) disponibles.stream().filter(f -> f.estado() == EstadoStock.PROXIMO_A_AGOTAR).count();
    }
}