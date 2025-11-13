package com.app.service;

import com.app.entity.Email;
import com.app.entity.Usuario;

public interface IEmailService {

	public void creacionClub(Usuario usuario);
	public void creacionUsuario(Usuario usuario);
	public void creacionUsuario(Email email);
	public void recuperacionClave(Usuario usuario);


}
