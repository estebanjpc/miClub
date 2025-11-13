package com.app.service;

import java.util.List;

import com.app.entity.Usuario;

public interface IUsuarioService {

	
	public List<Usuario> findAll();
	public Usuario findById(Long id);
	public void save(Usuario usuario);
	public Usuario findByEmail(String email);
	public void delete(Usuario usuario);
	public List<Usuario> findUsuarioByAuthority(String role);
	public List<Usuario> findUsuarioByIdClub(Long idClubSession, String role);
	
}
