package com.sourceflag.spring.init_destroy_method;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
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
@ComponentScan("com.sourceflag.spring.init_destroy_method")
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

		RootBeanDefinition userServiceBeanDefinition = new RootBeanDefinition();
		userServiceBeanDefinition.setBeanClass(UserService.class);
		userServiceBeanDefinition.setInitMethodName("init");
		userServiceBeanDefinition.setDestroyMethodName("over");
		userServiceBeanDefinition.setEnforceInitMethod(true);
		userServiceBeanDefinition.setEnforceDestroyMethod(true);
		ctx.registerBeanDefinition("userService", userServiceBeanDefinition);

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(InferredService.class);
		beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
		userServiceBeanDefinition.setEnforceDestroyMethod(true);
		ctx.registerBeanDefinition("inferredService", beanDefinition);


		System.out.println(ctx.getBean(UserService.class));
		System.out.println(ctx.getBean(InferredService.class));


		// 关闭容器会触发调用销毁方法
		// ctx.close();
		ctx.registerShutdownHook();
	}
}
