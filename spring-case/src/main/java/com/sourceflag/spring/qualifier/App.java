package com.sourceflag.spring.qualifier;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-20 14:16:53
 */
@Configuration
@ComponentScan("com.sourceflag.spring.qualifier")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

		UserService userService = ctx.getBean(UserService.class);
		for (int i = 1; i <= 10; i++) {
			System.out.println(String.format("第 %s 次：%s", i, userService.next()));
		}
	}
}
