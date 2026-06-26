package com.app.notification.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.notification.domain.NotificationConfig;
import com.app.notification.domain.NotificationType;
import com.app.entity.Deportista;
import com.app.notification.dto.MassEmailRequest;
import com.app.notification.dto.MassEmailResponse;
import com.app.notification.dto.MorosoPreviewDto;
import com.app.notification.dto.NotificationConfigDto;
import com.app.notification.service.MassEmailService;
import com.app.notification.service.NotificationConfigService;
import com.app.security.AppRoles;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notifications")
@Secured({ AppRoles.CLUB, AppRoles.TESORERO })
public class NotificationRestController {

	private final NotificationConfigService notificationConfigService;
	private final MassEmailService massEmailService;

	public NotificationRestController(NotificationConfigService notificationConfigService,
			MassEmailService massEmailService) {
		this.notificationConfigService = notificationConfigService;
		this.massEmailService = massEmailService;
	}

	@GetMapping("/config")
	public ResponseEntity<List<NotificationConfigDto>> getConfig(HttpServletRequest request) {
		Long clubId = clubSession(request);
		if (clubId == null) {
			return ResponseEntity.status(401).build();
		}
		List<NotificationConfigDto> list = notificationConfigService.listForClub(clubId).stream().map(this::toDto)
				.collect(Collectors.toList());
		return ResponseEntity.ok(list);
	}

	@PutMapping("/config")
	public ResponseEntity<Void> putConfig(HttpServletRequest request,
			@RequestBody List<NotificationConfigDto> body) {
		Long clubId = clubSession(request);
		if (clubId == null) {
			return ResponseEntity.status(401).build();
		}
		if (body == null || body.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		for (NotificationConfigDto row : body) {
			if (row.getType() == null) {
				return ResponseEntity.badRequest().build();
			}
			int days = row.getDaysOffset();
			if (row.getType() == NotificationType.PAYMENT_RECEIVED) {
				days = 0;
			}
			notificationConfigService.update(clubId, row.getType(), row.isEnabled(), days);
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/mass-email", consumes = "application/json")
	public ResponseEntity<MassEmailResponse> massEmail(HttpServletRequest request,
			@RequestBody MassEmailRequest body) {
		Long clubId = clubSession(request);
		if (clubId == null) {
			return ResponseEntity.status(401).build();
		}
		try {
			int n = massEmailService.enviar(clubId, body);
			return ResponseEntity.ok(new MassEmailResponse(n));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/debtors-preview")
	public ResponseEntity<List<MorosoPreviewDto>> debtorsPreview(HttpServletRequest request,
			@RequestParam int month,
			@RequestParam(required = false) Integer year) {
		Long clubId = clubSession(request);
		if (clubId == null) {
			return ResponseEntity.status(401).build();
		}
		if (month < 1 || month > 12) {
			return ResponseEntity.badRequest().build();
		}
		int y = year != null ? year : java.time.YearMonth.now().getYear();
		List<MorosoPreviewDto> list = massEmailService.listarMorosos(clubId, month, y).stream()
				.map(NotificationRestController::toMorosoPreview)
				.collect(Collectors.toList());
		return ResponseEntity.ok(list);
	}

	@GetMapping("/debtors-count")
	public ResponseEntity<DebtorsCountResponse> debtorsCount(HttpServletRequest request,
			@RequestParam int month,
			@RequestParam(required = false) Integer year) {
		Long clubId = clubSession(request);
		if (clubId == null) {
			return ResponseEntity.status(401).build();
		}
		if (month < 1 || month > 12) {
			return ResponseEntity.badRequest().build();
		}
		int y = year != null ? year : java.time.YearMonth.now().getYear();
		long n = massEmailService.contarMorosos(clubId, month, y);
		return ResponseEntity.ok(new DebtorsCountResponse(n));
	}

	private NotificationConfigDto toDto(NotificationConfig c) {
		return new NotificationConfigDto(c.getType(), c.isEnabled(), c.getDaysOffset());
	}

	private static MorosoPreviewDto toMorosoPreview(Deportista d) {
		String nom = (d.getNombre() != null ? d.getNombre() : "") + " " + (d.getApellido() != null ? d.getApellido() : "");
		String email = d.getUsuario() != null && d.getUsuario().getEmail() != null ? d.getUsuario().getEmail() : "";
		String cat = d.getCategoria() != null && d.getCategoria().getNombre() != null ? d.getCategoria().getNombre() : "";
		return new MorosoPreviewDto(d.getId(), nom.trim(), email, cat);
	}

	private static Long clubSession(HttpServletRequest request) {
		Object v = request.getSession().getAttribute("idClubSession");
		return v instanceof Long ? (Long) v : null;
	}

	public static final class DebtorsCountResponse {
		private final long debtorCount;

		public DebtorsCountResponse(long debtorCount) {
			this.debtorCount = debtorCount;
		}

		public long getDebtorCount() {
			return debtorCount;
		}
	}
}
