package com.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.AsistenciaClase;

public interface IAsistenciaClaseRepository extends JpaRepository<AsistenciaClase, Long> {

	List<AsistenciaClase> findByClub_IdAndFechaClase(Long clubId, LocalDate fechaClase);

	Optional<AsistenciaClase> findByClub_IdAndDeportista_IdAndFechaClase(Long clubId, Long deportistaId, LocalDate fechaClase);

	@Query("""
			select a from AsistenciaClase a
			join fetch a.deportista d
			join fetch d.usuario u
			left join fetch d.categoria
			where a.club.id = :clubId
			  and a.fechaClase between :desde and :hasta
			order by a.fechaClase desc, d.apellido, d.nombre
			""")
	List<AsistenciaClase> findDetalleByClubAndRango(@Param("clubId") Long clubId, @Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta);

	@Query("""
			select a from AsistenciaClase a
			join fetch a.deportista d
			join fetch d.usuario u
			left join fetch d.categoria
			where a.club.id = :clubId
			  and u.id = :usuarioId
			  and a.fechaClase between :desde and :hasta
			order by a.fechaClase desc, d.apellido, d.nombre
			""")
	List<AsistenciaClase> findDetalleByUsuarioAndRango(@Param("clubId") Long clubId, @Param("usuarioId") Long usuarioId,
			@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
