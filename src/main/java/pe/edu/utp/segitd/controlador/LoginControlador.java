package pe.edu.utp.segitd.controlador;

import pe.edu.utp.segitd.modelo.Usuario;
import pe.edu.utp.segitd.servicio.AuthService;
import pe.edu.utp.segitd.util.SesionUsuario;

/** Traduce el evento de ingreso de LoginJFrame a una llamada de AuthService. */
public class LoginControlador {

    private final AuthService authService = new AuthService();

    /** Autentica al usuario y, si las credenciales son válidas, abre su sesión. */
    public Usuario iniciarSesion(String username, char[] password) {
        Usuario usuario = authService.autenticar(username, password);
        SesionUsuario.obtenerInstancia().iniciarSesion(usuario);
        return usuario;
    }
}