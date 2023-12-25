package com.sourceflag.spring.initdestorymethod;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-19 23:44:03
 */
@Configuration
@ComponentScan("com.sourceflag.spring.initdestorymethod")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(UserService.class);
		beanDefinition.setInitMethodName("init");
		beanDefinition.setDestroyMethodName("destroy");
		beanDefinition.setEnforceInitMethod(true);
		beanDefinition.setEnforceDestroyMethod(true);

		ctx.registerBeanDefinition("userService", beanDefinition);

		System.out.println(ctx.getBean(UserService.class));

		ctx.close();
	}
}
