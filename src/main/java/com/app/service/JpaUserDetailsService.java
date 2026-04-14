package com.app.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.repository.IUsuarioRepository;
import com.app.entity.Usuario;

@Service
@Primary
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {

		List<Usuario> usuarios = usuarioRepository.findByEmail(email);

		if (usuarios == null || usuarios.isEmpty()) {
			throw new UsernameNotFoundException("EMAIL_NO_EXISTE");
		}

		boolean algunoHabilitado = usuarios.stream().anyMatch(u -> Boolean.TRUE.equals(u.getEnabled()));

		if (!algunoHabilitado) {
			Usuario ref = usuarios.get(0);
			return new User(email, ref.getPassword(), false, true, true, true, new ArrayList<>());
		}

		Set<String> authorityNames = new LinkedHashSet<>();
		for (Usuario u : usuarios) {
			if (Boolean.TRUE.equals(u.getEnabled())) {
				u.getRoles().forEach(r -> authorityNames.add(r.getAuthority()));
			}
		}
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		for (String a : authorityNames) {
			authorities.add(new SimpleGrantedAuthority(a));
		}

		Usuario ref = usuarios.stream().filter(u -> Boolean.TRUE.equals(u.getEnabled())).findFirst().orElse(usuarios.get(0));

		return new User(email, ref.getPassword(), true, true, true, true, authorities);
	}

}
