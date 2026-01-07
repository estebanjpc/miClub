package com.app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.app.entity.Club;
import com.app.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long>{

//	@Query("select u from Usuario u where email = ?1")
//	public Usuario findByEmail(String email);
	
	@Query("select u from Usuario u left join fetch u.roles where email = ?1")
	public List<Usuario> findByEmail(String email);

	@Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE r.authority = ?1")
	public List<Usuario> findUsuarioByAuthority(String role);

	@Query("SELECT DISTINCT u FROM Usuario u JOIN u.club c JOIN u.roles r WHERE c.id = ?1 and r.authority = ?2")
	public List<Usuario> findUsuarioByIdClub(Long idClubSession,String role);
	
	@Query("SELECT c FROM Club c JOIN c.usuarios u WHERE u.email = ?1")
	public List<Club> findClubesByUsuario(String email);
	
	@Query("SELECT u.club.id FROM Usuario u WHERE u.email = ?1 AND u.club.estado = '1' ")
	public List<Long> findClubIdsByUsuario(String email);

	@Query("SELECT c FROM Club c JOIN c.usuarios u WHERE u.email = ?1AND c.estado = '1' ")
	public List<Club> findClubesHabilitadosByUsuario(String email);
}
