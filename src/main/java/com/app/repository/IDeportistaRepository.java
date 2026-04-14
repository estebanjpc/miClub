package com.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.Deportista;

public interface IDeportistaRepository extends JpaRepository<Deportista, Long> {

	long countByUsuario_Club_Id(Long idClub);

	@Query("SELECT d FROM Deportista d LEFT JOIN FETCH d.categoria WHERE d.usuario.id = ?1 ORDER BY d.id")
	List<Deportista> findByUsuarioIdOrderById(Long usuarioId);

	@Query("SELECT d FROM Deportista d WHERE d.usuario.id = ?1 AND d.usuario.club.id = ?2 AND d.usuario.club.estado = '1' AND d.estado = '1' ")
	List<Deportista> findByUsuarioAndClub(Long usuarioId, Long clubId);

	@Query("select d from Deportista d where d.usuario.club.id = ?1 and d.estado = '1' ")
	List<Deportista> findByClub(Long idClub);

	@Query("SELECT d FROM Deportista d JOIN FETCH d.usuario u LEFT JOIN FETCH d.categoria c WHERE u.club.id = ?1 ORDER BY d.apellido ASC, d.nombre ASC")
	List<Deportista> findAllByClubWithUsuarioAndCategoria(Long idClub);

	@Query("SELECT COUNT(d) FROM Deportista d WHERE d.usuario.club.id = :idClub AND d.fechaIngreso <= :hasta")
	Long countActivosHastaMes(@Param("idClub") Long idClub, @Param("hasta") LocalDate hasta);

}

