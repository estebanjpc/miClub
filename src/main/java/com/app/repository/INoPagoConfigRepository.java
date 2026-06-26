package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.NoPagoConfig;

public interface INoPagoConfigRepository extends JpaRepository<NoPagoConfig, Long> {

	List<NoPagoConfig> findByClub_IdAndMesAndAnio(Long clubId, Integer mes, Integer anio);

	List<NoPagoConfig> findByClub_IdOrderByAnioDescMesDescIdDesc(Long clubId);
}
