package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.EmailEnvio;

public interface IEmailEnvioRepository extends JpaRepository<EmailEnvio, Long> {

	List<EmailEnvio> findTop50ByClubIdOrderByFechaEnvioDesc(Long clubId);
}
