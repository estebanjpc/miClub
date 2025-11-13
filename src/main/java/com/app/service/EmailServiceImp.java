package com.app.service;

import java.io.File;
import java.util.ArrayList;

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
import com.app.entity.Usuario;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImp implements IEmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Value("${mail.set.from}")
	public String emailSetFrom;
	
	public void creacionUsuario(Usuario usuario) {
	    try {
	        // Crear mensaje MIME
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

	        // Crear contexto Thymeleaf
	        Context context = new Context();
	        context.setVariable("nombre", usuario.getNombre());
	        context.setVariable("apellido", usuario.getApellido());
	        context.setVariable("email", usuario.getEmail());
	        context.setVariable("password", usuario.getPassAux());
	        context.setVariable("club", usuario.getClub() != null ? usuario.getClub().getNombre() : "Tu Club");

	        // Si tiene deportistas, los pasamos a la plantilla
	        if (usuario.getDeportistas() != null && !usuario.getDeportistas().isEmpty()) {
	            context.setVariable("deportistas", usuario.getDeportistas());
	        } else {
	            context.setVariable("deportistas", new ArrayList<>());
	        }

	        // Procesar plantilla Thymeleaf
	        String htmlContent = templateEngine.process("email/creacionUsuario.html", context);

	        // Configurar correo
	        helper.setTo(usuario.getEmail());
	        helper.setSubject("Bienvenido al Club - " + usuario.getClub().getNombre());
	        helper.setFrom(emailSetFrom);
	        helper.setText(htmlContent, true); // true = HTML

	        // Adjuntar logo embebido
	        ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
	        if (logoResource.exists()) {
	            helper.addInline("logoImage", logoResource);
	        } else {
	            File logoFile = new File("src/main/resources/static/images/logo.png");
	            if (logoFile.exists()) {
	                FileSystemResource logoFs = new FileSystemResource(logoFile);
	                helper.addInline("logoImage", logoFs);
	            } else {
	                System.out.println("Logo no encontrado ni en classpath ni en src/main/resources");
	            }
	        }

	        // Enviar correo
	        mailSender.send(mimeMessage);
	        System.out.println("Correo de bienvenida al club enviado correctamente");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	public void creacionClub(Usuario usuario) {
	    try {
	        // Crear mensaje MIME
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

	        // Crear contexto Thymeleaf
	        Context context = new Context();
	        context.setVariable("nombre", usuario.getNombre());
	        context.setVariable("apellido", usuario.getApellido());
	        context.setVariable("email", usuario.getEmail());
	        context.setVariable("password", usuario.getPassAux());

	        // Procesar plantilla Thymeleaf desde classpath
	        String htmlContent = templateEngine.process("email/creacionClub.html", context);

	        // Configurar correo
	        helper.setTo(usuario.getEmail());
	        helper.setSubject("Bienvenido a la Plataforma - Creación de cuenta");
	        helper.setFrom(emailSetFrom);
	        helper.setText(htmlContent, true); // true = HTML

	        // Adjuntar logo embebido
	        File logoFile;
	        ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");

	        if (logoResource.exists()) {
	            // Si existe en el classpath (Docker o JAR)
	            helper.addInline("logoImage", logoResource);
	        } else {
	            // Si estás en desarrollo y el logo está en src/main/resources
	            logoFile = new File("src/main/resources/static/images/logo.png");
	            if (logoFile.exists()) {
	                FileSystemResource logoFs = new FileSystemResource(logoFile);
	                helper.addInline("logoImage", logoFs);
	            } else {
	                System.out.println("Logo no encontrado ni en classpath ni en src/main/resources");
	            }
	        }

	        // Enviar correo
	        mailSender.send(mimeMessage);
	        System.out.println("Correo de creación de usuario enviado correctamente");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void recuperacionClave(Usuario usuario) {
	    try {
	        // Crear mensaje MIME
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

	        // Crear contexto Thymeleaf con las variables del template
	        Context context = new Context();
	        context.setVariable("nombre", usuario.getNombre());
	        context.setVariable("apellido", usuario.getApellido());
	        context.setVariable("email", usuario.getEmail());
	        context.setVariable("password", usuario.getPassAux());

	        // Procesar plantilla Thymeleaf (correo de recuperación)
	        String htmlContent = templateEngine.process("email/recuperacionClave.html", context);

	        // Configurar correo
	        helper.setTo(usuario.getEmail());
	        helper.setSubject("Recuperación de Contraseña - Plataforma");
	        helper.setFrom(emailSetFrom);
	        helper.setText(htmlContent, true); // true = HTML

	        // Adjuntar logo embebido
	        try {
	            ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
	            if (logoResource.exists()) {
	                helper.addInline("logoImage", logoResource);
	            } else {
	                File logoFile = new File("src/main/resources/static/images/logo.png");
	                if (logoFile.exists()) {
	                    FileSystemResource logoFs = new FileSystemResource(logoFile);
	                    helper.addInline("logoImage", logoFs);
	                } else {
	                    System.out.println("Logo no encontrado ni en classpath ni en src/main/resources");
	                }
	            }
	        } catch (Exception e) {
	            System.out.println("Error al adjuntar logo: " + e.getMessage());
	        }

	        // Enviar correo
	        mailSender.send(mimeMessage);
	        System.out.println("Correo de recuperación de contraseña enviado correctamente a " + usuario.getEmail());

	    } catch (Exception e) {
	        System.out.println("Error al enviar correo de recuperación: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	
	public void sendMail(SimpleMailMessage message) {
		try {
			mailSender.send(message);		
		}catch(Exception e) {
			System.out.println("######## ERROR ENVIO EMAIL ########");
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
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	


}
