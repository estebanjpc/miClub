package com.app.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.AsistenciaEstadisticaDTO;
import com.app.entity.AsistenciaClase;
import com.app.entity.Deportista;
import com.app.entity.Usuario;
import com.app.repository.IAsistenciaClaseRepository;
import com.app.repository.IDeportistaRepository;
import com.app.repository.IUsuarioRepository;

@Service
public class AsistenciaClaseServiceImpl implements IAsistenciaClaseService {

	private final IAsistenciaClaseRepository asistenciaRepository;
	private final IDeportistaRepository deportistaRepository;
	private final IUsuarioRepository usuarioRepository;

	public AsistenciaClaseServiceImpl(IAsistenciaClaseRepository asistenciaRepository,
			IDeportistaRepository deportistaRepository, IUsuarioRepository usuarioRepository) {
		this.asistenciaRepository = asistenciaRepository;
		this.deportistaRepository = deportistaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional
	public void registrarAsistenciaDia(Long clubId, Long entrenadorId, LocalDate fechaClase, Set<Long> presentesIds) {
		Set<Long> presentes = presentesIds != null ? presentesIds : Set.of();
		List<Deportista> deportistas = deportistaRepository.findByClub(clubId);
		Usuario entrenador = entrenadorId != null ? usuarioRepository.findById(entrenadorId).orElse(null) : null;

		for (Deportista d : deportistas) {
			AsistenciaClase a = asistenciaRepository.findByClub_IdAndDeportista_IdAndFechaClase(clubId, d.getId(), fechaClase)
					.orElseGet(AsistenciaClase::new);
			a.setClub(d.getUsuario().getClub());
			a.setDeportista(d);
			a.setEntrenador(entrenador);
			a.setFechaClase(fechaClase);
			a.setPresente(presentes.contains(d.getId()));
			asistenciaRepository.save(a);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Set<Long> obtenerPresentesEnFecha(Long clubId, LocalDate fechaClase) {
		List<AsistenciaClase> list = asistenciaRepository.findByClub_IdAndFechaClase(clubId, fechaClase);
		Set<Long> presentes = new HashSet<>();
		for (AsistenciaClase a : list) {
			if (a.isPresente() && a.getDeportista() != null) {
				presentes.add(a.getDeportista().getId());
			}
		}
		return presentes;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AsistenciaEstadisticaDTO> estadisticasClub(Long clubId, LocalDate desde, LocalDate hasta) {
		List<AsistenciaClase> rows = asistenciaRepository.findDetalleByClubAndRango(clubId, desde, hasta);
		return buildStats(rows);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AsistenciaEstadisticaDTO> estadisticasUsuario(Long clubId, Long usuarioId, LocalDate desde, LocalDate hasta) {
		List<AsistenciaClase> rows = asistenciaRepository.findDetalleByUsuarioAndRango(clubId, usuarioId, desde, hasta);
		return buildStats(rows);
	}

	private List<AsistenciaEstadisticaDTO> buildStats(List<AsistenciaClase> rows) {
		Map<Long, AsistenciaEstadisticaDTO> map = new HashMap<>();
		for (AsistenciaClase a : rows) {
			Long idDep = a.getDeportista().getId();
			AsistenciaEstadisticaDTO dto = map.get(idDep);
			if (dto == null) {
				dto = new AsistenciaEstadisticaDTO();
				dto.setDeportistaId(idDep);
				dto.setDeportistaNombre(a.getDeportista().getNombre() + " " + a.getDeportista().getApellido());
				dto.setCategoria(a.getDeportista().getCategoria() != null ? a.getDeportista().getCategoria().getNombre() : "—");
				map.put(idDep, dto);
			}
			dto.setClasesTotales(dto.getClasesTotales() + 1);
			if (a.isPresente()) {
				dto.setAsistencias(dto.getAsistencias() + 1);
			} else {
				dto.setAusencias(dto.getAusencias() + 1);
			}
		}
		List<AsistenciaEstadisticaDTO> out = new ArrayList<>(map.values());
		for (AsistenciaEstadisticaDTO dto : out) {
			if (dto.getClasesTotales() == 0) {
				dto.setPorcentajeAsistencia(0.0);
			} else {
				dto.setPorcentajeAsistencia((dto.getAsistencias() * 100.0) / dto.getClasesTotales());
			}
		}
		out.sort((a, b) -> a.getDeportistaNombre().compareToIgnoreCase(b.getDeportistaNombre()));
		return out;
	}
}
