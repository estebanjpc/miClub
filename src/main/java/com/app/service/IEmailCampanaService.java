package com.app.service;

import java.util.List;

import com.app.dto.EmailCampanaForm;
import com.app.entity.EmailEnvio;

public interface IEmailCampanaService {

	int enviarCampana(Long clubId, EmailCampanaForm form);

	List<EmailEnvio> historialPorClub(Long clubId);
}
