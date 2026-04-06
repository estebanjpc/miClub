package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.app.entity.Email;
import com.app.entity.OrdenPago;
import com.app.entity.Usuario;
import com.app.repository.IOrdenPagoRepository;

/**
 * Envío de correos en segundo plano para no bloquear peticiones HTTP ni transacciones.
 * Los controladores y servicios deben usar esta clase en lugar de {@link IEmailService} directo.
 */
@Service
public class AsyncEmailService {

	@Autowired
	private IEmailService emailService;

	@Autowired
	private IOrdenPagoRepository ordenPagoRepository;

	@Async("emailTaskExecutor")
	public void sendMail(SimpleMailMessage message) {
		emailService.sendMail(message);
	}

	@Async("emailTaskExecutor")
	public void creacionClub(Usuario usuario) {
		emailService.creacionClub(usuario);
	}

	@Async("emailTaskExecutor")
	public void creacionUsuario(Usuario usuario) {
		emailService.creacionUsuario(usuario);
	}

	@Async("emailTaskExecutor")
	public void creacionUsuario(Email email) {
		emailService.creacionUsuario(email);
	}

	@Async("emailTaskExecutor")
	public void recuperacionClave(Usuario usuario) {
		emailService.recuperacionClave(usuario);
	}

	@Async("emailTaskExecutor")
	public void notificarClubNuevoPagoEfectivo(Long idPago) {
		emailService.notificarClubNuevoPagoEfectivo(idPago);
	}

	@Async("emailTaskExecutor")
	public void notificarClubNuevoPagoEfectivoLote(List<Long> idsPagos) {
		emailService.notificarClubNuevoPagoEfectivoLote(idsPagos);
	}

	/** Recarga la orden con pagos/club/deportista en el hilo async (entidad válida tras commit). */
	@Async("emailTaskExecutor")
	public void notificarClubOrdenKhipuPagada(Long ordenId) {
		OrdenPago orden = ordenPagoRepository.findByIdWithDetalle(ordenId);
		if (orden != null) {
			emailService.notificarClubOrdenKhipuPagada(orden);
		}
	}

	@Async("emailTaskExecutor")
	public void notificarUsuarioResultadoKhipu(Long ordenId, boolean exitoso, String motivo) {
		OrdenPago orden = ordenPagoRepository.findByIdWithDetalle(ordenId);
		if (orden != null) {
			emailService.notificarUsuarioResultadoKhipu(orden, exitoso, motivo);
		}
	}

	@Async("emailTaskExecutor")
	public void notificarUsuarioEstadoPagoEfectivo(Long idPago, boolean aprobado, String motivo) {
		emailService.notificarUsuarioEstadoPagoEfectivo(idPago, aprobado, motivo);
	}
}
