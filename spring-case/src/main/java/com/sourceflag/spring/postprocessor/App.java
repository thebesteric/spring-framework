package com.sourceflag.spring.postprocessor;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 22:59:43
 */
@ComponentScan("com.sourceflag.spring.postprocessor")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);
		UserService userService = context.getBean(UserService.class);
		userService.test();
		System.out.println(context.getBean(OrderService.class));

		System.out.println(context.getBean(TestService.class));
	}
}
