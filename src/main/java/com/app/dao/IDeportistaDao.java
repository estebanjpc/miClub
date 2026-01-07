package com.app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.app.entity.Deportista;

public interface IDeportistaDao extends CrudRepository<Deportista, Long> {


	@Query("SELECT d FROM Deportista d WHERE d.usuario.id = ?1 AND d.usuario.club.id = ?2 AND d.usuario.club.estado = '1' AND d.estado = '1' ")
    List<Deportista> findByUsuarioAndClub(Long usuarioId,Long clubId);
    
    @Query("select d from Deportista d where d.usuario.club.id = ?1 and d.estado = '1' ")
    	List<Deportista> findByClub(Long idClub);

	
}
