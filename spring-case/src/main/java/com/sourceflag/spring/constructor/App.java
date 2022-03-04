package com.sourceflag.spring.constructor;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.sourceflag.spring.constructor")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(App.class);
		ctx.refresh();

		ctx.getBean(UserService.class);
	}
}
