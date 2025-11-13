package com.app.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class JpaUserDetailsService implements UserDetailsService{

	
	@Autowired
	private IUsuarioDao usuarioDao;
	
	private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);
	
	
	@Override
	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		Usuario usuario = usuarioDao.findByEmail(email);
		
		if(usuario == null) {
//			logger.error("ERROR LOGIN :::::  email: "+email+" no existe en bd");
//			throw new UsernameNotFoundException("email: "+email+" no existe en bd");
			throw new BadCredentialsException("email: "+email+" no existe en bd");
		}
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		for (Role role: usuario.getRoles()) {
			logger.info("Role: "+role.getAuthority());
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}
		
		if(authorities.isEmpty()) {
			logger.error("ERROR LOGIN :::::  email: "+email+" no tiene roles asignados!");
			throw new UsernameNotFoundException("email: "+email+" no tiene roles asignados!");
		}
		
		User user = new User(usuario.getEmail(), usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
		return user;
	}
	
}
