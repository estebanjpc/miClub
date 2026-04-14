package com.app.dto;

/**
 * Fila del mantenedor de clubes (vista admin): contacto principal + métricas del club.
 */
public record ClubListadoAdminItem(
		Long idUsuarioPrincipal,
		Long idClub,
		String nombreClub,
		String emailContacto,
		String estadoUsuario,
		String estadoClub,
		long totalUsuarios,
		long totalDeportistas,
		long totalCategorias) {
}
