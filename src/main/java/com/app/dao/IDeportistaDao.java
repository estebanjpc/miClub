package com.app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.app.entity.Deportista;

public interface IDeportistaDao extends CrudRepository<Deportista, Long> {


    List<Deportista> findByUsuarioId(Long usuarioId);
    
    @Query("select d from Deportista d where d.usuario.club.id = ?1 and d.estado = '1' ")
    	List<Deportista> findByClub(Long idClub);

	
}
