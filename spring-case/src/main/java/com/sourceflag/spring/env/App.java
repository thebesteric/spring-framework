package com.sourceflag.spring.env;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.Map;

public class App {
	public static void main(String[] args) throws IOException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

		// 操作系统的环境变量
		Map<String, Object> systemEnvironment = context.getEnvironment().getSystemEnvironment();
		System.out.println(systemEnvironment);

		System.out.println("=======");

		// Java 的参数，比如 -Dname=eric
		Map<String, Object> systemProperties = context.getEnvironment().getSystemProperties();
		System.out.println(systemProperties);

		System.out.println("=======");

		// 包含了 systemProperties，systemEnvironment 和 @PropertySource 指定的文件
		MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
		System.out.println(propertySources);

		System.out.println("=======");

		System.out.println(context.getEnvironment().getProperty("NO_PROXY"));
		System.out.println(context.getEnvironment().getProperty("sun.jnu.encoding"));
		System.out.println(context.getEnvironment().getProperty("application.name"));

	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.env")
	@PropertySource("classpath:resource.properties")
	public static class AppConfig {

	}
}
