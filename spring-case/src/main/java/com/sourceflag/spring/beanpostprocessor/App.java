package com.sourceflag.spring.beanpostprocessor;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 22:59:43
 */
@ComponentScan("com.sourceflag.spring.beanpostprocessor")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);
		System.out.println(context.getBean("userService"));
	}
}
