package com.sourceflag.spring.constructor;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

// @Component
@ComponentScan("com.sourceflag.spring.constructor")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(App.class);
		ctx.refresh();

		ctx.getBean(UserService.class);
	}

	// public static class InnerClass {
	// 	@Bean
	// 	public UserService userService() {
	// 		return new UserService();
	// 	}
	// }

	// @Bean
	// public UserService userService() {
	// 	return new UserService();
	// }
}
