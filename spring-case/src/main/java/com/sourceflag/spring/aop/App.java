package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.demo.SimpleCalculate;
import com.sourceflag.spring.aop.service.OrderService;
import com.sourceflag.spring.aop.service.UserServiceInterface;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

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

		// ctx.addBeanFactoryPostProcessor(xxx);

		ctx.register(Config.class);
		ctx.refresh();

		// ctx.scan();

		SimpleCalculate simpleCalculate = (SimpleCalculate) ctx.getBean("simpleCalculate");
		simpleCalculate.add(1, 2);

		// 引用，不会走到通知逻辑，只会默认实现改方法
		// ProgramCalculate programCalculate = (ProgramCalculate) ctx.getBean("simpleCalculate");
		// programCalculate.toBinary(1);

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
	@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true) // exposeProxy = true，不但会去寻找 Advisor 类型的 bean，还会去寻找 @Aspect 注解的 bean
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

		@Component
		public static class Test{

		}

	}

}
