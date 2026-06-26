package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.entity.CategoriaValorVigencia;

public interface ICategoriaValorVigenciaRepository extends JpaRepository<CategoriaValorVigencia, Long> {

	boolean existsByCategoria_IdAndAnioAndMes(Long categoriaId, Integer anio, Integer mes);

	List<CategoriaValorVigencia> findByCategoria_IdOrderByAnioDescMesDesc(Long categoriaId);

	@Query("""
			select v
			from CategoriaValorVigencia v
			where v.categoria.id = :categoriaId
			  and (v.anio < :anio or (v.anio = :anio and v.mes <= :mes))
			order by v.anio desc, v.mes desc
			""")
	List<CategoriaValorVigencia> findVigenciaAplicable(@Param("categoriaId") Long categoriaId, @Param("anio") Integer anio,
			@Param("mes") Integer mes, Pageable pageable);

	Optional<CategoriaValorVigencia> findTopByCategoria_IdOrderByAnioDescMesDesc(Long categoriaId);
}
