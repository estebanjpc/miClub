package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.repository.IUsuarioRepository;
import com.app.entity.Club;
import com.app.entity.Usuario;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioRepository usuarioRepository;
	

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> findAll() {
		return (List<Usuario>) usuarioRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findById(Long id) {
		return usuarioRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void save(Usuario usuario) {
		usuarioRepository.save(usuario);
	}

	@Override
	public void delete(Usuario usuario) {
		usuarioRepository.delete(usuario);
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findByEmail(String email) {
		List<Usuario> usuarios = usuarioRepository.findByEmail(email);
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
		return usuarioRepository.findUsuarioByAuthority(role);
	}

	@Override
	public List<Usuario> findUsuarioByIdClub(Long idClubSession, String role) {
		return usuarioRepository.findUsuarioByIdClub(idClubSession,role);
	}

	@Override
	public List<Long> findClubIdsByUsuario(String email) {
		return usuarioRepository.findClubIdsByUsuario(email);
	}

	@Override
	public List<Club> findClubesByUsuario(String email) {
//		return usuarioRepository.findClubesByUsuario(email);
		return usuarioRepository.findClubesHabilitadosByUsuario(email);
	}

	@Override
	public List<Usuario> findAllByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario resolveUsuarioActivo(String email, Long idClubSession) {
		List<Usuario> porEmail = usuarioRepository.findByEmail(email);
		if (porEmail.isEmpty()) {
			return null;
		}
		if (porEmail.size() > 1 && idClubSession == null) {
			return null;
		}
		if (idClubSession != null) {
			return porEmail.stream()
					.filter(u -> u.getClub() != null && idClubSession.equals(u.getClub().getId()))
					.findFirst()
					.orElse(null);
		}
		return porEmail.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsOtroUsuarioConEmailEnClub(String email, Long idClub, Long excludeUsuarioId) {
		if (email == null || email.isBlank() || idClub == null) {
			return false;
		}
		return usuarioRepository.findByEmail(email).stream()
				.filter(u -> u.getClub() != null && idClub.equals(u.getClub().getId()))
				.anyMatch(u -> excludeUsuarioId == null || !excludeUsuarioId.equals(u.getId()));
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario refrescarUsuarioSesion(HttpServletRequest request, String emailPrincipal) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		Usuario u = resolveUsuarioActivo(emailPrincipal, idClubSession);
		if (u == null) {
			return null;
		}
		Usuario fresh = findById(u.getId());
		if (fresh != null) {
			request.getSession().setAttribute("usuarioLogin", fresh);
		}
		return fresh;
	}
}
