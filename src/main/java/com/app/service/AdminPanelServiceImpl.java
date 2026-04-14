package com.app.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.ClubListadoAdminItem;
import com.app.entity.Club;
import com.app.entity.Pago;
import com.app.entity.Usuario;
import com.app.enums.EstadoPago;
import com.app.repository.ICategoriaRepository;
import com.app.repository.IClubRepository;
import com.app.repository.IDeportistaRepository;
import com.app.repository.IPagoRepository;
import com.app.repository.IUsuarioRepository;

@Service
public class AdminPanelServiceImpl implements IAdminPanelService {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IDeportistaRepository deportistaRepository;

	@Autowired
	private ICategoriaRepository categoriaRepository;

	@Autowired
	private IPagoRepository pagoRepository;

	@Autowired
	private IClubRepository clubRepository;

	@Override
	@Transactional(readOnly = true)
	public List<ClubListadoAdminItem> listarClubesConConteos() {
		List<Usuario> principales = usuarioRepository.findUsuarioByAuthority("ROLE_CLUB");
		List<ClubListadoAdminItem> out = new ArrayList<>();
		for (Usuario u : principales) {
			if (u.getClub() == null) {
				continue;
			}
			Long cid = u.getClub().getId();
			long nu = usuarioRepository.countByClub_Id(cid);
			long nd = deportistaRepository.countByUsuario_Club_Id(cid);
			long nc = categoriaRepository.countByClub_Id(cid);
			out.add(new ClubListadoAdminItem(u.getId(), cid, u.getNombre(), u.getEmail(), u.getEstado(),
					u.getClub().getEstado(), nu, nd, nc));
		}
		return out;
	}

	@Override
	@Transactional(readOnly = true)
	public Club findClubById(Long idClub) {
		return clubRepository.findById(idClub).orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pago> listarIncidenciasPagosRecientes(int max) {
		var estados = EnumSet.of(EstadoPago.RECHAZADO, EstadoPago.PENDIENTE, EstadoPago.PENDIENTE_KHIPU);
		return pagoRepository.findByEstadoInOrderByFechaDesc(estados, PageRequest.of(0, Math.max(1, max)));
	}
}
