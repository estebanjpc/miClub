package com.app.notification.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.mail.MailInlineLogoSupport;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationMailSender {

	private static final Logger log = LoggerFactory.getLogger(NotificationMailSender.class);

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailInlineLogoSupport mailInlineLogoSupport;

	@Value("${mail.set.from}")
	private String emailSetFrom;

	@Value("${app.public.url:http://localhost:8081}")
	private String appPublicUrl;

	public NotificationMailSender(JavaMailSender mailSender, SpringTemplateEngine templateEngine,
			MailInlineLogoSupport mailInlineLogoSupport) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.mailInlineLogoSupport = mailInlineLogoSupport;
	}

	public boolean sendCuotaReminder(String toEmail, String nombreApoderado, Club club, Deportista deportista,
			int mes, int anio, boolean beforeDue) {
		if (toEmail == null || toEmail.isBlank()) {
			return false;
		}
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			Context ctx = new Context(Locale.forLanguageTag("es-CL"));
			ctx.setVariable("nombreApoderado", nombreApoderado != null ? nombreApoderado : "");
			ctx.setVariable("nombreClub", club.getNombre());
			ctx.setVariable("nombreDeportista", deportista.getNombre() + " " + deportista.getApellido());
			ctx.setVariable("mes", mes);
			ctx.setVariable("anio", anio);
			ctx.setVariable("beforeDue", beforeDue);
			ctx.setVariable("loginUrl", loginUrl());

			String html = templateEngine.process("email/notificacionRecordatorioCuota.html", ctx);
			helper.setTo(toEmail);
			helper.setSubject((beforeDue ? "Recordatorio: cuota próxima a vencer — " : "Recordatorio: cuota vencida — ")
					+ club.getNombre());
			helper.setFrom(emailSetFrom);
			helper.setText(html, true);
			mailInlineLogoSupport.attachLogoClubById(helper, club.getId());
			mailSender.send(mimeMessage);
			return true;
		} catch (Exception e) {
			log.error("Error enviando recordatorio de cuota. email={} clubId={}", toEmail, club.getId(), e);
			return false;
		}
	}

	private String loginUrl() {
		String base = appPublicUrl != null ? appPublicUrl.trim() : "http://localhost:8081";
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/login";
	}
}
