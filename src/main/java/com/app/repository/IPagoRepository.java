package com.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

public interface IPagoRepository extends JpaRepository<Pago, Long> {

	Optional<Pago> findByDeportistaIdAndMesAndAnio(Long deportistaId, Integer mes, Integer anio);
	
	boolean existsByDeportistaIdAndMesAndAnioAndEstadoIn(
			Long deportistaId,
			Integer mes,
			Integer anio,
			Collection<EstadoPago> estados);
	
	@Query("""
			select case when count(p) > 0 then true else false end
			from Pago p
			where p.deportista.id = :deportistaId
			  and p.mes = :mes
			  and p.anio = :anio
			  and (p.concepto is null or p.concepto = com.app.enums.ConceptoPago.MENSUALIDAD)
			  and (
			       p.estado = com.app.enums.EstadoPago.PAGADO
			       or p.estado = com.app.enums.EstadoPago.PENDIENTE_KHIPU
			       or (p.estado = com.app.enums.EstadoPago.PENDIENTE and p.medioPago is not null)
			  )
			""")
	boolean existsPagoBloqueante(@Param("deportistaId") Long deportistaId,
			@Param("mes") Integer mes,
			@Param("anio") Integer anio);

	List<Pago> findByDeportistaId(Long deportistaId);
	
	List<Pago> findByClubIdAndEstadoAndMedioPagoOrderByFechaDesc(Long clubId, EstadoPago estado, MedioPago medioPago);

	List<Pago> findByClubIdAndEstadoAndMedioPagoInOrderByFechaDesc(Long clubId, EstadoPago estado,
			Collection<MedioPago> mediosPago);

	@Query("""
			select count(p) from Pago p
			where p.club.id = :idClub and p.mes = :mes and p.anio = :anio and p.estado = :estado
			  and (p.concepto is null or p.concepto = com.app.enums.ConceptoPago.MENSUALIDAD)
			""")
	long countPagadosMensualesEnMes(@Param("idClub") Long clubId, @Param("mes") Integer mes, @Param("anio") Integer anio,
			@Param("estado") EstadoPago estado);

	@Query("select p from Pago p left join fetch p.ordenPago where p.club.id = ?1 and p.deportista.id = ?2 order by p.fecha desc")
	List<Pago> findByClub_IdAndDeportista_IdOrderByFechaDesc(Long clubId, Long deportistaId);

	List<Pago> findByIdIn(List<Long> ids);

	@Query("select p from Pago p join fetch p.deportista d where p.club.id = ?1 order by p.fecha desc")
	List<Pago> findByClubIdOrderByFechaDesc(Long idClubSession);

	@Query("select count(distinct d.id) from Deportista d where d.usuario.club.id = ?1")
	Long totalDeportistas(Long idClub);

	/**
	 * Deportistas con cuota del período (mes/año) en estado PAGADO.
	 * No usa la fecha de registro del pago: un pago de enero registrado en marzo cuenta como enero.
	 */
	@Query("""
			SELECT COUNT(DISTINCT p.deportista.id) FROM Pago p
			WHERE p.club.id = :idClub
			  AND p.mes = :mes AND p.anio = :anio
			  AND p.estado = com.app.enums.EstadoPago.PAGADO
			  AND (p.concepto is null or p.concepto = com.app.enums.ConceptoPago.MENSUALIDAD)
			""")
	Long deportistasAlDia(@Param("idClub") Long idClub, @Param("mes") Integer mes, @Param("anio") Integer anio);

	/**
	 * Suma el valor de cuota de todos los pagos PAGADO del club en el mes/año del período
	 * (efectivo validado y Khipu confirmado). No usa OrdenPago: el monto vive en la categoría del deportista.
	 */
	@Query("""
			select coalesce(sum(coalesce(p.monto, c.valorCuota)), 0)
			from Pago p
			join p.deportista d
			join d.categoria c
			where p.club.id = :idClub
			  and p.mes = :mes
			  and p.anio = :anio
			  and p.estado = com.app.enums.EstadoPago.PAGADO
			""")
	Long totalRecaudadoEnMes(@Param("idClub") Long idClub, @Param("mes") Integer mes, @Param("anio") Integer anio);

	/** Periodo como año*100+mes (ej. 202503) para comparar rangos. Solo pagos PAGADO. */
	@Query("""
			select coalesce(sum(coalesce(p.monto, c.valorCuota)), 0)
			from Pago p
			join p.deportista d
			join d.categoria c
			where p.club.id = :idClub
			  and p.estado = com.app.enums.EstadoPago.PAGADO
			  and (p.anio * 100 + p.mes) between :desde and :hasta
			""")
	Long totalRecaudadoRango(@Param("idClub") Long idClub, @Param("desde") Integer desde, @Param("hasta") Integer hasta);

	@Query("""
			select p from Pago p
			where p.club.id = :idClub
			  and (p.anio * 100 + p.mes) between :desde and :hasta
			order by p.anio desc, p.mes desc, p.id desc
			""")
	List<Pago> findByClubRangoPeriodo(@Param("idClub") Long idClub, @Param("desde") Integer desde, @Param("hasta") Integer hasta);

	@Query(" select p from Pago p where p.club.id = ?1 and (?2 is null or p.mes = ?2) and (?3 is null or p.estado = ?3) and (:idDeportista is null or p.deportista.id = :idDeportista) order by p.fecha desc ")
	List<Pago> buscarPagosFiltrados(Long idClub, Integer mes, EstadoPago estado, Long idDeportista);

	@Query(" select p from Pago p where p.club.id = ?1 and p.mes = ?2 and p.anio = ?3 and (p.concepto is null or p.concepto = com.app.enums.ConceptoPago.MENSUALIDAD) order by p.deportista.id, p.fecha desc, p.id desc ")
	List<Pago> findByClubMesAnio(Long idClub, Integer mes, Integer anio);

	@Query(" select p from Pago p WHERE p.club.id = ?1 AND ( p.anio < ?3 OR (p.anio = ?3 AND p.mes <= ?2)) and (p.concepto is null or p.concepto = com.app.enums.ConceptoPago.MENSUALIDAD) order by p.deportista.id, p.anio desc, p.mes desc, p.fecha desc, p.id desc ")
	List<Pago> obtenerEstadoAcumulado(Long idClub, Integer mes, Integer anio);

	@Query("""
			select p from Pago p
			join fetch p.club
			join fetch p.deportista d
			join fetch d.categoria
			join fetch d.usuario
			where p.id = :id
			""")
	Optional<Pago> findByIdWithDetalle(@Param("id") Long id);

	@Query("""
			select p from Pago p
			where p.estado in :estados
			order by p.fecha desc
			""")
	List<Pago> findByEstadoInOrderByFechaDesc(@Param("estados") Collection<EstadoPago> estados, Pageable pageable);
}

