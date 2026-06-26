package com.app.dto;

/**
 * Alta de usuarios desde el club: apoderado (socio) o personal (tesorero / entrenador).
 */
public final class TipoAltaUsuarioClub {

	public static final String APODERADO = "APODERADO";
	public static final String TESORERO = "TESORERO";
	public static final String ENTRENADOR = "ENTRENADOR";

	private TipoAltaUsuarioClub() {
	}

	public static boolean esValido(String tipo) {
		return APODERADO.equals(tipo) || TESORERO.equals(tipo) || ENTRENADOR.equals(tipo);
	}
}
