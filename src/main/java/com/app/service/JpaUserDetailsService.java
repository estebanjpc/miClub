package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dao.IUsuarioDao;
import com.app.entity.Usuario;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioDao usuarioDao;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {

		List<Usuario> usuarios = usuarioDao.findByEmail(email);

		if (usuarios == null || usuarios.isEmpty()) {
			throw new BadCredentialsException("EMAIL_NO_EXISTE");
		}

		boolean algunoHabilitado = usuarios.stream().anyMatch(u -> Boolean.TRUE.equals(u.getEnabled()));

		if (!algunoHabilitado) {
			throw new BadCredentialsException("USUARIO_DESHABILITADO");
		}

		// password único por email
		Usuario ref = usuarios.get(0);
		List<SimpleGrantedAuthority> authorities = ref.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getAuthority())).toList();

		return new User(email, ref.getPassword(), true, true, true, true,authorities);
	}

}
