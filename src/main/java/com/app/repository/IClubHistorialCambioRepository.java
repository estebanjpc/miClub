package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Club;
import com.app.entity.ClubHistorialCambio;

public interface IClubHistorialCambioRepository extends JpaRepository<ClubHistorialCambio, Long> {

	List<ClubHistorialCambio> findByClubOrderByFechaDesc(Club club);

}

