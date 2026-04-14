package com.app.security;

/**
 * Constantes de roles Spring Security ({@code ROLE_*}).
 * <p>
 * {@link #ENTRENADOR}: solo deportistas y apoderados (listado/fichas). Sin pagos, finanzas, correos masivos ni datos bancarios del club.
 */
public final class AppRoles {

	public static final String ADMIN = "ROLE_ADMIN";
	public static final String CLUB = "ROLE_CLUB";
	public static final String TESORERO = "ROLE_TESORERO";
	public static final String ENTRENADOR = "ROLE_ENTRENADOR";
	public static final String USER = "ROLE_USER";
	/** Socio / apoderado (mismo uso previsto que {@link #USER}; permite distinguir en BD y futuras reglas). */
	public static final String SOCIO = "ROLE_SOCIO";

	/** Club, tesorero y entrenador (navegación amplia; el entrenador tiene rutas restringidas aparte). */
	public static final String[] CLUB_STAFF = { CLUB, TESORERO, ENTRENADOR };
	/** Administración financiera y avisos de pago al club (sin entrenador). */
	public static final String[] CLUB_FINANZAS = { CLUB, TESORERO };
	public static final String[] APODERADO = { USER, SOCIO };

	private AppRoles() {
	}
}
