package com.sourceflag.spring.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Locale;

public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(context.getMessage("hello", null, Locale.SIMPLIFIED_CHINESE));
		System.out.println(context.getMessage("hello", null, Locale.ENGLISH));
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.i18n")
	@EnableAsync
	public static class AppConfig {
		@Bean
		public MessageSource messageSource() {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasename("messages");
			return messageSource;
		}
	}
}
