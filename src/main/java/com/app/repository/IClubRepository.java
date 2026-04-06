package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Club;

public interface IClubRepository extends JpaRepository<Club, Long> {

	boolean existsByCodigo(String codigo);

}

