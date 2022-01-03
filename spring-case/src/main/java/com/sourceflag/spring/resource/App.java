package com.sourceflag.spring.resource;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

public class App {
	public static void main(String[] args) throws IOException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

		Resource resource = context.getResource("file:///Users/keisun/IdeaProjects/research/source/spring/spring-framework-5.3.2/spring-case/src/main/java/com/sourceflag/spring/resource/App.java");
		System.out.println(resource.contentLength());
		System.out.println(resource.getFile().getName());

		System.out.println("==========");

		resource = context.getResource("https://www.baidu.com");
		System.out.println(resource.contentLength());
		System.out.println(resource.getURI());

		System.out.println("==========");

		resource = context.getResource("classpath:application.properties");
		System.out.println(resource.contentLength());
		System.out.println(resource.getFilename());

		System.out.println("==========");

		Resource[] resources = context.getResources("classpath:com/sourceflag/spring/resource/*.class");
		for (Resource r : resources) {
			System.out.println(r.getFilename());
		}
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.resource")
	@EnableAsync
	public static class AppConfig {
	}
}
