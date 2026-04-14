package com.app.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.app.entity.Usuario;
import com.app.service.IUsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final IUsuarioService usuarioService;

    public LoginFailureHandler(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String msjLogin;

        String errorCode = extractErrorCode(exception);
        if (!StringUtils.hasText(errorCode)) {
            errorCode = "";
        }

        if (isUsuarioDeshabilitado(request, errorCode)) {
            msjLogin = "error;Usuario desactivado;Tu usuario está desactivado. Comunícate con el administrador.";
            request.getSession().setAttribute("msjLogin", msjLogin);
            response.sendRedirect("/login?error");
            return;
        }
        
        switch (errorCode) {
        case "CLUB_DESHABILITADO":
            msjLogin = "error;Club deshabilitado;El club se encuentra deshabilitado. Contacte al administrador.";
            break;
        case "USUARIO_SIN_CLUB":
            msjLogin = "error;Acceso inválido;El usuario no tiene un club asignado.";
            break;
        case "USUARIO_DESHABILITADO":
            msjLogin = "error;Usuario desactivado;Tu usuario está desactivado. Comunícate con el administrador.";
            break;
        default:
            msjLogin = "error;Error en el login;Nombre de usuario o contraseña incorrecta.";
    }


        request.getSession().setAttribute("msjLogin", msjLogin);

        response.sendRedirect("/login?error");
    }

    private String extractErrorCode(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (StringUtils.hasText(current.getMessage())) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return "";
    }

    private boolean isUsuarioDeshabilitado(HttpServletRequest request, String errorCode) {
        if ("USUARIO_DESHABILITADO".equals(errorCode) || "User is disabled".equalsIgnoreCase(errorCode)) {
            return true;
        }
        String email = request.getParameter("email");
        if (!StringUtils.hasText(email)) {
            return false;
        }
        List<Usuario> usuarios = usuarioService.findAllByEmail(email.trim());
        return usuarios != null && !usuarios.isEmpty()
                && usuarios.stream().noneMatch(u -> Boolean.TRUE.equals(u.getEnabled()));
    }
}
