package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Categoria;
import com.app.entity.Club;

public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByClub(Club club);

    Categoria findByNombreAndClub(String nombre, Club club);

}

