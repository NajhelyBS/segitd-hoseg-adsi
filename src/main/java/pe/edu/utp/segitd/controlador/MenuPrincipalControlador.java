package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.servicio.BackupService;
import pe.edu.utp.segitd.servicio.DashboardService;
import pe.edu.utp.segitd.servicio.IndicadoresDashboard;
import pe.edu.utp.segitd.util.SesionUsuario;

import java.nio.file.Path;

/** Traduce los eventos de MenuPrincipalJFrame a llamadas de servicio. */
public class MenuPrincipalControlador {

    private final DashboardService dashboardService = new DashboardService();
    private final BackupService backupService = new BackupService();

    public IndicadoresDashboard cargarIndicadores() {
        return dashboardService.obtenerIndicadores();
    }

    public boolean verificarConexion() {
        return dashboardService.hayConexion();
    }

    public void cerrarSesion() {
        SesionUsuario.obtenerInstancia().cerrarSesion();
    }

    /** Respaldo manual (botón "Respaldar ahora"). Devuelve la carpeta generada. */
    public Path respaldarAhora() {
        return backupService.ejecutarBackup();
    }

    /** RNF-05: respalda al cerrar la aplicación si pasaron más de 24 horas desde el último. */
    public void respaldarSiCorresponde() {
        if (backupService.debeRespaldarAutomaticamente()) {
            backupService.ejecutarBackup();
        }
    }
}
