package com.sourceflag.spring.circular;

import com.sourceflag.spring.circular.service.AService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * App
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-25 22:48
 * @since 1.0
 */

public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();

		// 关闭循环依赖
		// DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ctx.getBeanFactory();
		// beanFactory.setAllowCircularReferences(false);

		AService aService = ctx.getBean(AService.class);
		System.out.println(aService);
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.circular")
	@EnableAspectJAutoProxy
	public static class Config {

	}

}
