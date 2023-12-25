package com.sourceflag.spring.rootbd;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-15 17:54:30
 */
@Configuration
@ComponentScan("com.sourceflag.spring.rootbd")
public class App {
	public static void main(String[] args) {
		// AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

		// RootBeanDefinition parentBD = new RootBeanDefinition();
		// parentBD.setScope("prototype");
		// ctx.registerBeanDefinition("parentBD", parentBD);

		// GenericBeanDefinition childBD = new GenericBeanDefinition();
		// childBD.setParentName("parentBD"); // 设置父 BD
		// childBD.setBeanClass(UserService.class);
		// ctx.registerBeanDefinition("childBD", childBD);

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

		System.out.println(ctx.getBean(UserService.class)); // UserService@6283d8b8
		System.out.println(ctx.getBean(UserService.class)); // UserService@3b6ddd1d
		System.out.println(ctx.getBean(UserService.class)); // UserService@3f6b0be5

	}
}
