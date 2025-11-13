package com.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
	
	
	public void addViewControllers(ViewControllerRegistry registry) {
		
		registry.addViewController("/error_403").setViewName("error_403");
		
	}
	
	@Bean("serviceRest")
	public RestTemplate registrarRestTemplate() {
		return new RestTemplate();
	}

}
