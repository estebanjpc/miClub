package com.app.auth;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String msjLogin;

        String errorCode = exception.getMessage();

        if (exception.getCause() != null && exception.getCause().getMessage() != null) {
            errorCode = exception.getCause().getMessage();
        }
        
        switch (errorCode) {
        case "CLUB_DESHABILITADO":
            msjLogin = "error;Club deshabilitado;El club se encuentra deshabilitado. Contacte al administrador.";
            break;
        case "USUARIO_SIN_CLUB":
            msjLogin = "error;Acceso inválido;El usuario no tiene un club asignado.";
            break;
        default:
            msjLogin = "error;Error en el login;Nombre de usuario o contraseña incorrecta.";
    }


        request.getSession().setAttribute("msjLogin", msjLogin);

        response.sendRedirect("/login?error");
    }
}
