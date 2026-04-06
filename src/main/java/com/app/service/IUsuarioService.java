package com.app.service;

import java.util.List;

import com.app.entity.Club;
import com.app.entity.Usuario;

import jakarta.servlet.http.HttpServletRequest;

public interface IUsuarioService {

	
	public List<Usuario> findAll();
	public Usuario findById(Long id);
	public void save(Usuario usuario);
	public Usuario findByEmail(String email);
	public void delete(Usuario usuario);
	public List<Usuario> findUsuarioByAuthority(String role);
	public List<Usuario> findUsuarioByIdClub(Long idClubSession, String role);
	public List<Long> findClubIdsByUsuario(String email);
	public List<Club> findClubesByUsuario(String email);
	public List<Usuario> findAllByEmail(String email);

	/**
	 * Usuario de negocio para el email autenticado y el club en sesión (multi-club).
	 */
	Usuario resolveUsuarioActivo(String email, Long idClubSession);

	/** {@code true} si ya existe otro usuario (distinto id) con ese email en ese club. */
	boolean existsOtroUsuarioConEmailEnClub(String email, Long idClub, Long excludeUsuarioId);

	/**
	 * Resuelve el usuario del club activo y actualiza {@code usuarioLogin} en sesión con datos de BD.
	 * @return usuario del club o {@code null} si falta club en sesión, hay varios clubes sin elegir, o no hay fila coincidente
	 */
	Usuario refrescarUsuarioSesion(HttpServletRequest request, String emailPrincipal);

	
}
