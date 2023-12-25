package com.sourceflag.spring.lookup;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
@ComponentScan("com.sourceflag.spring.lookup")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

		// UserService 有 @Lookup 注解的方法
		UserService userService = ctx.getBean(UserService.class);
		userService.test(); // User@130c12b7
		userService.test(); // User@3646a422
		userService.test(); // User@3e27aa33

	}
}
