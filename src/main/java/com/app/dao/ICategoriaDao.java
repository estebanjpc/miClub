package com.app.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.app.entity.Categoria;
import com.app.entity.Club;

public interface ICategoriaDao extends CrudRepository<Categoria, Long> {

    List<Categoria> findByClub(Club club);
    Categoria findByNombreAndClub(String nombre, Club club);
    
}
