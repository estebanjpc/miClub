package com.app.editors;

import java.beans.PropertyEditorSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.service.IBancoService;

@Component
public class BancoEditor extends PropertyEditorSupport{
	
	@Autowired
	private IBancoService bancoService;
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		
		try {
//			setValue(bancoService.findByNombre(text));
			setValue(bancoService.findById(Long.parseLong(text)));
		}catch(Exception e) {
			setValue(null);
		}
	}

}
