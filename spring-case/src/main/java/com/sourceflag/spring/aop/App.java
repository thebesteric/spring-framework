package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.service.OrderService;
import com.sourceflag.spring.aop.service.UserServiceInterface;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.*;
import org.springframework.core.DecoratingProxy;

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

		UserServiceInterface userService = ctx.getBean(UserServiceInterface.class);
		// 因为 UserService 实现了接口，所以 Spring 会使用 JDK 动态代理
		// 除非 proxyTargetClass = true 会强制使用 CGLIB 动态代理
		userService.test();

		OrderService orderService = ctx.getBean(OrderService.class);
		orderService.test(); // CGLIB 动态代理

		// SpringProxy springProxy = (SpringProxy) orderService;
		//
		// Advised advised = (Advised) orderService;
		// System.out.println(advised.getAdvisorCount());
		//
		// // JDK 动态代理才会有 DecoratingProxy
		// DecoratingProxy decoratingProxy = (DecoratingProxy) userService;
		// System.out.println(decoratingProxy.getDecoratedClass().getName());
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.aop")
	@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true) // 不但会去寻找 Advisor 类型的 bean，还会去寻找 @Aspect 注解的 bean
	// @EnableAspectJAutoProxy(proxyTargetClass = true) // proxyTargetClass = true，表示强制使用 CGLIB 动态代理
	public static class Config {

		// @Bean // 需要关闭 @EnableAspectJAutoProxy
		public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
			// BeanNameAutoProxyCreator 其实是 BeanPostProcessor
			BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
			beanNameAutoProxyCreator.setBeanNames("userService");
			beanNameAutoProxyCreator.setInterceptorNames("myAdvisor");
			return beanNameAutoProxyCreator;
		}


		// @Bean // 需要关闭 @EnableAspectJAutoProxy
		public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
			// 寻找那些 bean 需要被代理（也就是 Advisor 类型的 bean），其实也是一个 BeanPostProcessor
			return new DefaultAdvisorAutoProxyCreator();
		}

	}

}
