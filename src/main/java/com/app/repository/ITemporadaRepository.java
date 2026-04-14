package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Temporada;

public interface ITemporadaRepository extends JpaRepository<Temporada, Long> {

	List<Temporada> findByClub_IdAndActivaTrueOrderByFechaInicioDesc(Long clubId);
}
