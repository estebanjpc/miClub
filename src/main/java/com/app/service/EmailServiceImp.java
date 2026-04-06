package com.app.service;

import java.io.File;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.app.entity.Email;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.entity.Usuario;
import com.app.enums.MedioPago;
import com.app.repository.IPagoRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImp implements IEmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private IPagoRepository pagoRepository;

	@Autowired
	private IUsuarioService usuarioService;

	@Value("${mail.set.from}")
	private String emailSetFrom;

	@Value("${app.public.url:http://localhost:8081}")
	private String appPublicUrl;

	@Override
	public void creacionUsuario(Usuario usuario) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = new Context();
			context.setVariable("nombre", usuario.getNombre());
			context.setVariable("apellido", usuario.getApellido());
			context.setVariable("email", usuario.getEmail());
			context.setVariable("password", usuario.getPassAux());
			context.setVariable("club", usuario.getClub() != null ? usuario.getClub().getNombre() : "Tu Club");
			context.setVariable("loginUrl", loginUrl());

			if (usuario.getDeportistas() != null && !usuario.getDeportistas().isEmpty()) {
				context.setVariable("deportistas", usuario.getDeportistas());
			} else {
				context.setVariable("deportistas", new ArrayList<>());
			}

			String htmlContent = templateEngine.process("email/creacionUsuario.html", context);

			helper.setTo(usuario.getEmail());
			helper.setSubject("Bienvenido al Club - " + usuario.getClub().getNombre());
			helper.setFrom(emailSetFrom);
			helper.setText(htmlContent, true);
			inlineLogo(helper);

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void creacionClub(Usuario usuario) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = new Context();
			context.setVariable("nombre", usuario.getNombre());
			context.setVariable("apellido", usuario.getApellido());
			context.setVariable("email", usuario.getEmail());
			context.setVariable("password", usuario.getPassAux());
			context.setVariable("loginUrl", loginUrl());

			String htmlContent = templateEngine.process("email/creacionClub.html", context);

			helper.setTo(usuario.getEmail());
			helper.setSubject("Bienvenido a la Plataforma - Creación de cuenta");
			helper.setFrom(emailSetFrom);
			helper.setText(htmlContent, true);
			inlineLogo(helper);

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void recuperacionClave(Usuario usuario) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = new Context();
			context.setVariable("nombre", usuario.getNombre());
			context.setVariable("apellido", usuario.getApellido());
			context.setVariable("email", usuario.getEmail());
			context.setVariable("password", usuario.getPassAux());
			context.setVariable("loginUrl", loginUrl());

			String htmlContent = templateEngine.process("email/recuperacionClave.html", context);

			helper.setTo(usuario.getEmail());
			helper.setSubject("Recuperación de Contraseña - Plataforma");
			helper.setFrom(emailSetFrom);
			helper.setText(htmlContent, true);
			inlineLogo(helper);

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notificarClubNuevoPagoEfectivo(Long idPago) {
		notificarClubNuevoPagoEfectivoLote(List.of(idPago));
	}

	@Override
	public void notificarClubNuevoPagoEfectivoLote(List<Long> idsPagos) {
		if (idsPagos == null || idsPagos.isEmpty()) {
			return;
		}
		List<Pago> cargados = new ArrayList<>();
		for (Long id : idsPagos) {
			pagoRepository.findByIdWithDetalle(id).ifPresent(cargados::add);
		}
		if (cargados.isEmpty()) {
			return;
		}
		Map<Long, List<Pago>> porClub = cargados.stream().collect(Collectors.groupingBy(p -> p.getClub().getId()));
		for (List<Pago> grupo : porClub.values()) {
			enviarCorreoClubPagoEfectivoGrupo(grupo);
		}
	}

	private void enviarCorreoClubPagoEfectivoGrupo(List<Pago> pagos) {
		if (pagos.isEmpty()) {
			return;
		}
		Pago primero = pagos.get(0);
		try {
			List<Usuario> destinatarios = usuarioService.findUsuarioByIdClub(primero.getClub().getId(), "ROLE_CLUB");
			if (destinatarios == null || destinatarios.isEmpty()) {
				System.out.println("Aviso club: no hay usuario ROLE_CLUB para club id " + primero.getClub().getId());
				return;
			}
			List<String> emails = destinatarios.stream()
					.map(Usuario::getEmail)
					.filter(e -> e != null && !e.isBlank())
					.distinct()
					.toList();
			if (emails.isEmpty()) {
				return;
			}

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = buildContextNuevoPagoClub(primero, null);
			context.setVariable("subtitulo", "Pago en efectivo — revisión pendiente");
			context.setVariable("estadoDescripcion", "Pendiente de aprobación en gestión de pagos");

			if (pagos.size() > 1) {
				List<String> lineas = pagos.stream().map(this::lineaResumen).toList();
				context.setVariable("lineas", lineas);
				int total = pagos.stream()
						.mapToInt(p -> p.getDeportista().getCategoria() != null
								? p.getDeportista().getCategoria().getValorCuota()
								: 0)
						.sum();
				context.setVariable("monto", formatMonto(total));
			} else {
				context.setVariable("lineas", new ArrayList<String>());
			}

			String asunto = pagos.size() > 1
					? "Nuevo pago en efectivo (" + pagos.size() + " cuotas) — " + primero.getClub().getNombre()
					: "Nuevo pago en efectivo — " + primero.getClub().getNombre();

			String html = templateEngine.process("email/nuevoPagoClub.html", context);
			helper.setTo(emails.toArray(new String[0]));
			helper.setSubject(asunto);
			helper.setFrom(emailSetFrom);
			helper.setText(html, true);
			inlineLogo(helper);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notificarClubOrdenKhipuPagada(OrdenPago orden) {
		if (orden.getPagos() == null || orden.getPagos().isEmpty()) {
			return;
		}
		Pago primero = orden.getPagos().get(0);
		try {
			List<Usuario> destinatarios = usuarioService.findUsuarioByIdClub(primero.getClub().getId(), "ROLE_CLUB");
			if (destinatarios == null || destinatarios.isEmpty()) {
				return;
			}
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = buildContextNuevoPagoClub(primero, orden);
			context.setVariable("subtitulo", "Pago en línea confirmado (Khipu)");
			context.setVariable("estadoDescripcion", "Pagado");
			if (orden.getMontoTotal() != null) {
				context.setVariable("monto", formatMonto(orden.getMontoTotal()));
			}

			String html = templateEngine.process("email/nuevoPagoClub.html", context);
			List<String> emails = destinatarios.stream()
					.map(Usuario::getEmail)
					.filter(e -> e != null && !e.isBlank())
					.distinct()
					.toList();
			if (emails.isEmpty()) {
				return;
			}
			helper.setTo(emails.toArray(new String[0]));
			helper.setSubject("Pago Khipu confirmado — " + primero.getClub().getNombre());
			helper.setFrom(emailSetFrom);
			helper.setText(html, true);
			inlineLogo(helper);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notificarUsuarioEstadoPagoEfectivo(Long idPago, boolean aprobado, String motivo) {
		pagoRepository.findByIdWithDetalle(idPago).ifPresent(pago -> {
			Usuario usuario = pago.getDeportista().getUsuario();
			if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
				return;
			}
			try {
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

				Context context = new Context();
				context.setVariable("exitoso", aprobado);
				context.setVariable("tituloEstado", aprobado ? "Pago aprobado" : "Pago rechazado");
				context.setVariable("nombreUsuario", usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""));
				context.setVariable("nombreClub", pago.getClub().getNombre());
				context.setVariable("nombreDeportista",
						pago.getDeportista().getNombre() + " " + pago.getDeportista().getApellido());
				context.setVariable("periodoLabel", periodoLabel(pago.getMes(), pago.getAnio()));
				context.setVariable("medioPago", MedioPago.EFECTIVO.name());
				context.setVariable("motivo", motivo != null ? motivo : "");
				context.setVariable("loginUrl", loginUrl());
				context.setVariable("lineas", new ArrayList<String>());

				String html = templateEngine.process("email/estadoPagoUsuario.html", context);
				helper.setTo(usuario.getEmail());
				helper.setSubject(aprobado ? "Tu pago fue aprobado — " + pago.getClub().getNombre()
						: "Tu pago fue rechazado — " + pago.getClub().getNombre());
				helper.setFrom(emailSetFrom);
				helper.setText(html, true);
				inlineLogo(helper);
				mailSender.send(mimeMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void notificarUsuarioResultadoKhipu(OrdenPago orden, boolean exitoso, String motivo) {
		if (orden.getIdUsuario() == null) {
			return;
		}
		Usuario usuario = usuarioService.findById(orden.getIdUsuario());
		if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
			return;
		}
		if (orden.getPagos() == null || orden.getPagos().isEmpty()) {
			return;
		}
		Pago primero = orden.getPagos().get(0);
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			List<String> lineas = new ArrayList<>();
			for (Pago p : orden.getPagos()) {
				lineas.add(lineaResumen(p));
			}

			Context context = new Context();
			context.setVariable("exitoso", exitoso);
			context.setVariable("tituloEstado", exitoso ? "Pago recibido" : "Pago no completado");
			context.setVariable("nombreUsuario", usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""));
			context.setVariable("nombreClub", primero.getClub().getNombre());
			context.setVariable("medioPago", MedioPago.KHIPU.name());
			context.setVariable("motivo", motivo != null ? motivo : "");
			context.setVariable("loginUrl", loginUrl());
			context.setVariable("lineas", lineas);
			context.setVariable("nombreDeportista", "");
			context.setVariable("periodoLabel", "");

			String html = templateEngine.process("email/estadoPagoUsuario.html", context);
			helper.setTo(usuario.getEmail());
			helper.setSubject(exitoso ? "Pago Khipu confirmado — " + primero.getClub().getNombre()
					: "Pago Khipu no completado — " + primero.getClub().getNombre());
			helper.setFrom(emailSetFrom);
			helper.setText(html, true);
			inlineLogo(helper);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Context buildContextNuevoPagoClub(Pago pago, OrdenPago orden) {
		Context context = new Context();
		context.setVariable("nombreClub", pago.getClub().getNombre());
		context.setVariable("nombreDeportista", pago.getDeportista().getNombre() + " " + pago.getDeportista().getApellido());
		context.setVariable("periodoLabel", periodoLabel(pago.getMes(), pago.getAnio()));
		context.setVariable("medioPago", pago.getMedioPago() != null ? pago.getMedioPago().name() : "");
		context.setVariable("loginUrl", loginUrl());
		Integer valor = pago.getDeportista().getCategoria() != null
				? pago.getDeportista().getCategoria().getValorCuota()
				: null;
		if (valor != null) {
			context.setVariable("monto", formatMonto(valor));
		}
		if (orden != null && orden.getPagos() != null && orden.getPagos().size() > 1) {
			List<String> lineas = new ArrayList<>();
			for (Pago p : orden.getPagos()) {
				lineas.add(lineaResumen(p));
			}
			context.setVariable("lineas", lineas);
		} else {
			context.setVariable("lineas", new ArrayList<String>());
		}
		return context;
	}

	private String lineaResumen(Pago p) {
		String nom = p.getDeportista().getNombre() + " " + p.getDeportista().getApellido();
		String per = periodoLabel(p.getMes(), p.getAnio());
		Integer v = p.getDeportista().getCategoria() != null ? p.getDeportista().getCategoria().getValorCuota() : null;
		if (v != null) {
			return nom + " — " + per + " — " + formatMonto(v);
		}
		return nom + " — " + per;
	}

	private String periodoLabel(Integer mes, Integer anio) {
		if (mes == null || anio == null) {
			return "";
		}
		String m = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
		if (m != null && !m.isEmpty()) {
			m = m.substring(0, 1).toUpperCase() + m.substring(1);
		}
		return m + " " + anio;
	}

	private String formatMonto(int valor) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
		return nf.format(valor);
	}

	private String loginUrl() {
		String base = appPublicUrl != null ? appPublicUrl.trim() : "http://localhost:8081";
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/login";
	}

	private void inlineLogo(MimeMessageHelper helper) {
		try {
			ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
			if (logoResource.exists()) {
				helper.addInline("logoImage", logoResource);
			} else {
				File logoFile = new File("src/main/resources/static/images/logo.png");
				if (logoFile.exists()) {
					helper.addInline("logoImage", new FileSystemResource(logoFile));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMail(SimpleMailMessage message) {
		try {
			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void creacionUsuario(Email email) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(email.getEmailFrom());
			message.setTo(email.getEmailTo());
			message.setSubject(email.getSubject());
			message.setText(email.getBody());
			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
