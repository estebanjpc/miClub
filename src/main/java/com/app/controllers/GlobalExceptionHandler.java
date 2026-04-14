package com.app.controllers;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		log.warn("Error de validación de negocio. path={} message={}", request.getRequestURI(), ex.getMessage());
		if (isApiRequest(request)) {
			return ResponseEntity.badRequest().body(buildBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request));
		}
		return "error_403";
	}

	@ExceptionHandler(AccessDeniedException.class)
	public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		log.warn("Acceso denegado. path={} message={}", request.getRequestURI(), ex.getMessage());
		if (isApiRequest(request)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(buildBody(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request));
		}
		return "error_403";
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public Object handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
		log.error("Violación de integridad de datos. path={}", request.getRequestURI(), ex);
		if (isApiRequest(request)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(buildBody(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY",
							"No se pudo guardar los datos (restricción o tamaño).", request));
		}
		String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
		ModelAndView mv = new ModelAndView("error_500");
		if (detalle.contains("comprobante_transferencia") || detalle.contains("Data too long")) {
			mv.addObject("mensajeUsuario",
					"El comprobante es demasiado grande para el sistema. Prueba con una foto o PDF más liviano (por ejemplo menos de 5 MB).");
		} else {
			mv.addObject("mensajeUsuario",
					"No se pudo completar el guardado. Si el problema continúa, contacta al administrador.");
		}
		return mv;
	}

	@ExceptionHandler(Exception.class)
	public Object handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Error no controlado. path={}", request.getRequestURI(), ex);
		if (isApiRequest(request)) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
							"Ocurrió un error inesperado", request));
		}
		return "error_500";
	}

	private boolean isApiRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path != null && path.startsWith("/api/");
	}

	private Map<String, Object> buildBody(HttpStatus status, String error, String message, HttpServletRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("error", error);
		body.put("message", message);
		body.put("path", request.getRequestURI());
		return body;
	}
}
