package com.app.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.app.entity.Pago;
import com.app.enums.EstadoPago;

public interface IPagoDao extends CrudRepository<Pago, Long> {

    Optional<Pago> findByDeportistaIdAndMesAndAnio(Long deportistaId, Integer mes, Integer anio);

    List<Pago> findByDeportistaId(Long deportistaId);
    
    List<Pago> findByIdIn(List<Long> ids);

    @Query("select p from Pago p join fetch p.deportista d where p.club.id = ?1 order by p.fecha desc")
    List<Pago> findByClubIdOrderByFechaDesc(Long idClubSession);
    
    
    @Query("select count(distinct d.id) from Deportista d where d.usuario.club.id = ?1")
	Long totalDeportistas(Long idClub);
    
    @Query("select count(distinct p.deportista.id) from Pago p where p.club.id = ?1 and p.mes = ?2 and p.anio = ?3 and p.estado = 'PAGADO' ")
    Long deportistasAlDia(Long idClub,Integer mes,Integer anio);
    
    @Query(" select coalesce(sum(op.montoTotal),0) from OrdenPago op where op.estado = 'PAGADO' and month(op.fechaPago) = ?1 and year(op.fechaPago) = ?2 ")
	Integer totalRecaudadoMes(Integer mes,Integer anio);
    
    @Query(" select p from Pago p where p.club.id = ?1 and (?2 is null or p.mes = ?2) and (?3 is null or p.estado = ?3) and (:idDeportista is null or p.deportista.id = :idDeportista) order by p.fecha desc ")
	List<Pago> buscarPagosFiltrados(Long idClub, Integer mes,EstadoPago estado,Long idDeportista);

    @Query(" select p from Pago p where p.club.id = ?1 and p.mes = ?2 and p.anio = ?3 ")
    	List<Pago> findByClubMesAnio(Long idClub, Integer mes, Integer anio);
}
