package com.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dao.IUsuarioDao;
import com.app.entity.Role;
import com.app.entity.Usuario;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioDao usuarioDao;


	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

	    List<Usuario> usuarios = usuarioDao.findByEmail(email);

	    if (usuarios == null || usuarios.isEmpty()) {
	        throw new BadCredentialsException("EMAIL_NO_EXISTE");
	    }

	    Usuario usuario = usuarios.stream()
	            .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
	            .findFirst()
	            .orElseThrow(() -> new BadCredentialsException("USUARIO_DESHABILITADO"));

	    List<GrantedAuthority> authorities = new ArrayList<>();

	    for (Role role : usuario.getRoles()) {
	        authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
	    }

	    if (authorities.isEmpty()) {
	        throw new UsernameNotFoundException("SIN_ROLES");
	    }

	    return new User(
	            usuario.getEmail(),
	            usuario.getPassword(),
	            usuario.getEnabled(),
	            true,
	            true,
	            true,
	            authorities
	    );
	}

}
