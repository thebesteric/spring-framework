package com.sourceflag.spring.constructor;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// @Component
@ComponentScan("com.sourceflag.spring.constructor")
@Configuration
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(App.class);

		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		beanDefinition.setScope("prototype");
		beanDefinition.setBeanClass(TestService.class);
		// beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new OrderService());
		// beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new OrderService());
		// beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("orderService"));
		// beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, new OrderService());
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		// beanDefinition.setLenientConstructorResolution(false);
		// beanDefinition.setInstanceSupplier(TestService::new);
		ctx.registerBeanDefinition("testService", beanDefinition);

		ctx.refresh();

		ctx.getBean(TestService.class);
		ctx.getBean(TestService.class);

		ctx.getBean(UserService.class);
	}

	// public static class InnerClass {
	// 	@Bean
	// 	public UserService userService() {
	// 		return new UserService();
	// 	}
	// }

	@Bean
	public UserService userServiceBean() {
		return new UserService();
	}

	@Bean
	public UserService userServiceBean(OrderService	orderService) {
		return new UserService();
	}
}
