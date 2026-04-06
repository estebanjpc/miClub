package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.OrdenPago;

public interface IOrdenPagoRepository extends JpaRepository<OrdenPago, Long> {

	OrdenPago findByKhipuPaymentId(String khipuPaymentId);

	@Query("""
			select distinct o from OrdenPago o
			join fetch o.pagos p
			join fetch p.deportista d
			join fetch d.categoria
			join fetch p.club
			where o.khipuPaymentId = :pid
			""")
	OrdenPago findByKhipuPaymentIdWithDetalle(@Param("pid") String khipuPaymentId);

	@Query("""
			select distinct o from OrdenPago o
			join fetch o.pagos p
			join fetch p.deportista d
			join fetch d.categoria
			join fetch p.club
			where o.id = :id
			""")
	OrdenPago findByIdWithDetalle(@Param("id") Long id);

	@Query("""
			select distinct o from OrdenPago o
			join o.pagos p
			where p.club.id = :idClub
			order by o.fechaCreacion desc
			""")
	List<OrdenPago> findByClubIdOrderByFechaCreacionDesc(@Param("idClub") Long idClub);

}
