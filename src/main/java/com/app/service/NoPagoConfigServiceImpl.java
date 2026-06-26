package com.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.NoPagoConfigForm;
import com.app.entity.Categoria;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.NoPagoConfig;
import com.app.repository.ICategoriaRepository;
import com.app.repository.IClubRepository;
import com.app.repository.IDeportistaRepository;
import com.app.repository.INoPagoConfigRepository;

@Service
public class NoPagoConfigServiceImpl implements INoPagoConfigService {

	private final INoPagoConfigRepository repo;
	private final IClubRepository clubRepository;
	private final ICategoriaRepository categoriaRepository;
	private final IDeportistaRepository deportistaRepository;

	public NoPagoConfigServiceImpl(INoPagoConfigRepository repo, IClubRepository clubRepository,
			ICategoriaRepository categoriaRepository, IDeportistaRepository deportistaRepository) {
		this.repo = repo;
		this.clubRepository = clubRepository;
		this.categoriaRepository = categoriaRepository;
		this.deportistaRepository = deportistaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean aplicaNoPago(Long clubId, Deportista deportista, int mes, int anio) {
		List<NoPagoConfig> configs = repo.findByClub_IdAndMesAndAnio(clubId, mes, anio);
		for (NoPagoConfig c : configs) {
			if (NoPagoConfig.Scope.CLUB.name().equals(c.getScope())) {
				return true;
			}
			if (deportista != null && NoPagoConfig.Scope.CATEGORIA.name().equals(c.getScope())
					&& deportista.getCategoria() != null && c.getCategoria() != null
					&& deportista.getCategoria().getId().equals(c.getCategoria().getId())) {
				return true;
			}
			if (deportista != null && NoPagoConfig.Scope.DEPORTISTA.name().equals(c.getScope())
					&& c.getDeportista() != null && deportista.getId().equals(c.getDeportista().getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NoPagoConfig> listarPorClub(Long clubId) {
		return repo.findByClub_IdOrderByAnioDescMesDescIdDesc(clubId);
	}

	@Override
	@Transactional
	public void crear(Long clubId, NoPagoConfigForm form) {
		Club club = clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));
		NoPagoConfig n = new NoPagoConfig();
		n.setClub(club);
		n.setMes(form.getMes());
		n.setAnio(form.getAnio());
		n.setScope(form.getScope().name());
		n.setObservacion(form.getObservacion());

		if (form.getScope() == NoPagoConfigForm.Scope.CATEGORIA) {
			if (form.getCategoriaId() == null) {
				throw new IllegalArgumentException("Debe seleccionar una categoría.");
			}
			Categoria cat = categoriaRepository.findById(form.getCategoriaId())
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));
			if (cat.getClub() == null || !clubId.equals(cat.getClub().getId())) {
				throw new IllegalArgumentException("La categoría no pertenece al club.");
			}
			n.setCategoria(cat);
		}
		if (form.getScope() == NoPagoConfigForm.Scope.DEPORTISTA) {
			if (form.getDeportistaId() == null) {
				throw new IllegalArgumentException("Debe seleccionar un deportista.");
			}
			Deportista d = deportistaRepository.findById(form.getDeportistaId())
					.orElseThrow(() -> new IllegalArgumentException("Deportista no encontrado."));
			if (d.getUsuario() == null || d.getUsuario().getClub() == null || !clubId.equals(d.getUsuario().getClub().getId())) {
				throw new IllegalArgumentException("El deportista no pertenece al club.");
			}
			n.setDeportista(d);
		}

		boolean duplicado = repo.findByClub_IdAndMesAndAnio(clubId, form.getMes(), form.getAnio()).stream().anyMatch(c -> {
			if (!c.getScope().equals(n.getScope())) {
				return false;
			}
			if (NoPagoConfig.Scope.CLUB.name().equals(c.getScope())) {
				return true;
			}
			if (NoPagoConfig.Scope.CATEGORIA.name().equals(c.getScope())) {
				return c.getCategoria() != null && n.getCategoria() != null
						&& c.getCategoria().getId().equals(n.getCategoria().getId());
			}
			if (NoPagoConfig.Scope.DEPORTISTA.name().equals(c.getScope())) {
				return c.getDeportista() != null && n.getDeportista() != null
						&& c.getDeportista().getId().equals(n.getDeportista().getId());
			}
			return false;
		});
		if (duplicado) {
			throw new IllegalArgumentException("Ya existe configuración de no pago para ese alcance en ese período.");
		}

		repo.save(n);
	}

	@Override
	@Transactional
	public void eliminar(Long id, Long clubId) {
		NoPagoConfig n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Registro no encontrado."));
		if (n.getClub() == null || !clubId.equals(n.getClub().getId())) {
			throw new IllegalArgumentException("No autorizado.");
		}
		repo.delete(n);
	}
}
