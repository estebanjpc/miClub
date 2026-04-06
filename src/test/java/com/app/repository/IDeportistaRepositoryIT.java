package com.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class IDeportistaRepositoryIT {

	@Autowired
	private IDeportistaRepository deportistaRepository;

	@Test
	void countActivosHastaMes_cuentaIngresoHastaPrimerDiaMes() {
		assertThat(deportistaRepository.countActivosHastaMes(1L, LocalDate.of(2026, 6, 1))).isEqualTo(2L);
		assertThat(deportistaRepository.countActivosHastaMes(1L, LocalDate.of(2025, 12, 1))).isEqualTo(0L);
	}

}
