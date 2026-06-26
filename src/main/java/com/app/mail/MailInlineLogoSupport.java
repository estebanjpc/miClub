package com.app.mail;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.app.entity.Club;
import com.app.service.IClubService;

@Component
public class MailInlineLogoSupport {

	private static final Logger log = LoggerFactory.getLogger(MailInlineLogoSupport.class);

	private final IClubService clubService;

	public MailInlineLogoSupport(IClubService clubService) {
		this.clubService = clubService;
	}

	public void attachLogoPlataforma(MimeMessageHelper helper) {
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
			log.warn("No se pudo adjuntar logo de plataforma inline en correo", e);
		}
	}

	public void attachLogoClub(MimeMessageHelper helper, Club club) {
		try {
			if (club != null && club.getLogo() != null && club.getLogo().length > 0) {
				ByteArrayResource res = new ByteArrayResource(club.getLogo());
				helper.addInline("logoImage", res, guessImageContentType(club.getLogo()));
				return;
			}
		} catch (Exception e) {
			log.warn("No se pudo adjuntar logo del club inline; usando logo de plataforma. clubId={}",
					club != null ? club.getId() : null, e);
		}
		attachLogoPlataforma(helper);
	}

	public void attachLogoClubById(MimeMessageHelper helper, Long clubId) {
		Club conLogo = clubId != null ? clubService.findById(clubId) : null;
		attachLogoClub(helper, conLogo);
	}

	private static String guessImageContentType(byte[] data) {
		if (data == null || data.length < 4) {
			return "image/jpeg";
		}
		if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
			return "image/jpeg";
		}
		if (data.length >= 8 && data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
			return "image/png";
		}
		if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
			return "image/gif";
		}
		if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F') {
			return "image/webp";
		}
		return "image/jpeg";
	}
}
