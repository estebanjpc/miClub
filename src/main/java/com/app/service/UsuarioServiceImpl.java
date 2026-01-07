package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dao.IUsuarioDao;
import com.app.entity.Club;
import com.app.entity.Usuario;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioDao usuarioDao;
	

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> findAll() {
		return (List<Usuario>) usuarioDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findById(Long id) {
		return usuarioDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void save(Usuario usuario) {
		usuarioDao.save(usuario);
	}

	@Override
	public void delete(Usuario usuario) {
		usuarioDao.delete(usuario);
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findByEmail(String email) {
		List<Usuario> usuarios = usuarioDao.findByEmail(email);
	    if (usuarios == null || usuarios.isEmpty()) {
	        return null;
	    }

	    return usuarios.stream()
	            .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
	            .findFirst()
	            .orElse(usuarios.get(0));
	}

	@Override
	public List<Usuario> findUsuarioByAuthority(String role) {
		return usuarioDao.findUsuarioByAuthority(role);
	}

	@Override
	public List<Usuario> findUsuarioByIdClub(Long idClubSession, String role) {
		return usuarioDao.findUsuarioByIdClub(idClubSession,role);
	}

	@Override
	public List<Long> findClubIdsByUsuario(String email) {
		return usuarioDao.findClubIdsByUsuario(email);
	}

	@Override
	public List<Club> findClubesByUsuario(String email) {
//		return usuarioDao.findClubesByUsuario(email);
		return usuarioDao.findClubesHabilitadosByUsuario(email);
	}
}
