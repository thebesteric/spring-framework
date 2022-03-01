package com.sourceflag.spring.factorybean;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(context.getBean("myFactoryBean")); // UserService
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.factorybean")
	public static class AppConfig {

	}
}
