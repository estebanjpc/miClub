package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.Club;
import com.app.entity.Usuario;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

	long countByClub_Id(Long idClub);

	@Query("select u from Usuario u left join fetch u.roles where u.email = ?1 order by u.id")
	List<Usuario> findByEmail(String email);

	@Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE r.authority = ?1")
	List<Usuario> findUsuarioByAuthority(String role);

	@Query("SELECT DISTINCT u FROM Usuario u JOIN u.club c JOIN u.roles r WHERE c.id = ?1 and r.authority = ?2")
	List<Usuario> findUsuarioByIdClub(Long idClubSession, String role);

	@Query("SELECT DISTINCT u FROM Usuario u JOIN u.club c JOIN u.roles r WHERE c.id = :idClub AND r.authority IN :roles")
	List<Usuario> findUsuarioByIdClubAndRoles(@Param("idClub") Long idClub, @Param("roles") List<String> roles);

	@Query("SELECT c FROM Club c JOIN c.usuarios u WHERE u.email = ?1")
	List<Club> findClubesByUsuario(String email);

	@Query("SELECT u.club.id FROM Usuario u WHERE u.email = ?1 AND u.club.estado = '1' ")
	List<Long> findClubIdsByUsuario(String email);

	@Query("SELECT c FROM Club c JOIN c.usuarios u WHERE u.email = ?1  AND c.estado = '1' ")
	List<Club> findClubesHabilitadosByUsuario(String email);
}

