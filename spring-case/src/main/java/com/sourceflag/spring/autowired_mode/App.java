package com.sourceflag.spring.autowired_mode;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-15 14:23:33
 */
@Configuration
@ComponentScan("com.sourceflag.spring.autowired_mode")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);
		// ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("autowired-mode-spring.xml");

		UserService userService = ctx.getBean(UserService.class);
		userService.test();

	}

	@Configuration
	public static class AppConfig {
		@Bean(autowire = Autowire.BY_NAME)
		public UserService userService() {
			return new UserService();
		}

		@Bean
		public OrderService orderService() {
			return new OrderService();
		}
	}
}
