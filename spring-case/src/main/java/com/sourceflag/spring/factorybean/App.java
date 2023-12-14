package com.sourceflag.spring.factorybean;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

		UserService factoryUserService1 = (UserService) context.getBean("myFactoryBean");
		UserService factoryUserService2 = (UserService) context.getBean("myFactoryBean");
		System.out.println(factoryUserService1); // UserService@55634720
		System.out.println(factoryUserService2); // UserService@55634720

		System.out.println(factoryUserService1.getA()); // null，因为 FactoryBean 只会经过初始化，其他生命周期步骤不会执行，比如依赖注入

		// 使用 & 前缀，可以直接获取到真正的 MyFactoryBean
		MyFactoryBean myFactoryBean = (MyFactoryBean) context.getBean("&myFactoryBean");
		System.out.println(myFactoryBean);

		UserService userService = (UserService) context.getBean("userService");
		System.out.println(userService); // UserService@4b0d79fc
		System.out.println(userService.getA()); // A@229c6181，经过完整的生命周期
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.factorybean")
	public static class AppConfig {

	}
}
