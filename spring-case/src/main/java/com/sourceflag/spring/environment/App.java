package com.sourceflag.spring.environment;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

@Configuration
@ComponentScan("com.sourceflag.spring.environment")
@PropertySource("classpath:resource.properties") // 指定要加载的变量
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);

		ConfigurableEnvironment environment = context.getEnvironment();

		// 所有环境变量的集合
		// [PropertiesPropertySource {name='systemProperties'}, SystemEnvironmentPropertySource {name='systemEnvironment'}, ResourcePropertySource {name='class path resource [resource.properties]'}]
		MutablePropertySources propertySources = environment.getPropertySources();
		System.out.println("propertySources = " + propertySources);

		// 运行应用时的变量，也就是用：-Dfile.encoding=UTF-8 指定的变量
		Map<String, Object> systemProperties = environment.getSystemProperties();
		System.out.println("systemProperties = " + systemProperties);

		// 操作系统级别的环境变量
		Map<String, Object> systemEnvironment = environment.getSystemEnvironment();
		System.out.println("systemEnvironment = " + systemEnvironment);

		// 从 @PropertySource 读取变量
		System.out.println("environment.getProperty(\"application.name\") from @PropertySource = " + environment.getProperty("application.name"));

		// 从 systemProperties 读取变量
		System.out.println("environment.getProperty(\"sun.jnu.encoding\") from systemProperties = " + environment.getProperty("sun.jnu.encoding"));

		// 从 systemEnvironment 读取变量
		System.out.println("environment.getProperty(\"PATH\") from  systemEnvironment = " + environment.getProperty("PATH"));
	}
}
