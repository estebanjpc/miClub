package com.app.service;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;

import com.app.entity.Email;
import com.app.entity.OrdenPago;
import com.app.entity.Usuario;

public interface IEmailService {

	void sendMail(SimpleMailMessage message);

	void creacionClub(Usuario usuario);

	void creacionUsuario(Usuario usuario);

	void creacionUsuario(Email email);

	void recuperacionClave(Usuario usuario);

	/** Club (ROLE_CLUB): nuevo pago en efectivo pendiente de aprobación. */
	void notificarClubNuevoPagoEfectivo(Long idPago);

	/** Un solo correo al club con todas las cuotas registradas en la misma operación (efectivo). */
	void notificarClubNuevoPagoEfectivoLote(List<Long> idsPagos);

	/** Club (ROLE_CLUB): orden Khipu confirmada como pagada. */
	void notificarClubOrdenKhipuPagada(OrdenPago orden);

	/** Usuario del deportista: resultado de validación en efectivo. */
	void notificarUsuarioEstadoPagoEfectivo(Long idPago, boolean aprobado, String motivo);

	/** Usuario: resultado del pago Khipu (varios ítems en un solo correo). */
	void notificarUsuarioResultadoKhipu(OrdenPago orden, boolean exitoso, String motivo);

}
