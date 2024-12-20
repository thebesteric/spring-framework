package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.demo.SimpleCalculate;
import com.sourceflag.spring.aop.service.OrderService;
import com.sourceflag.spring.aop.service.UserService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

		System.out.println("====== 使用 ProxyFactoryBean 代理 OrderService ======");
		OrderService testService = (OrderService) ctx.getBean("testService");
		testService.test();
		System.out.println("====================================================");

		SimpleCalculate simpleCalculate = (SimpleCalculate) ctx.getBean("simpleCalculate");
		simpleCalculate.add(1, 2);

		// 引用，不会走到通知逻辑，只会默认实现改方法
		// ProgramCalculate programCalculate = (ProgramCalculate) ctx.getBean("simpleCalculate");
		// programCalculate.toBinary(1);

		UserService userService = ctx.getBean(UserService.class);
		// 因为 UserService 实现了接口，所以 Spring 会使用 JDK 动态代理
		// 除非 proxyTargetClass = true 会强制使用 CGLIB 动态代理
		userService.test();

		OrderService orderService = ctx.getBean("orderService", OrderService.class);
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
	// @Import(DefaultAdvisorAutoProxyCreator.class) // 只会去查找 Advisor 类型的 bean
	// @Import(AnnotationAwareAspectJAutoProxyCreator.class) // 不仅会去查找 Advisor 类型的 bean，还会解析 @Before、@After、@Around 等注解
	public static class Config {

		/**
		 * 让某个普通类成为代理对象
		 * 其实底层就是一个 FactoryBean
		 */
		@Bean
		public ProxyFactoryBean testService() {
			OrderService orderService = new OrderService();
			ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
			proxyFactoryBean.addAdvice(new MethodInterceptor() {
				@Nullable
				@Override
				public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
					System.out.println("before...");
					Object proceed = invocation.proceed();
					System.out.println("after...");
					return proceed;
				}
			});
			proxyFactoryBean.setTarget(orderService);
			return proxyFactoryBean;
		}

		/**
		 * 让某个 bean 成为代理对象，只能通过名字来代理
		 */
		// @Bean // 需要关闭 @EnableAspectJAutoProxy
		public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
			// BeanNameAutoProxyCreator 其实是 BeanPostProcessor
			BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
			beanNameAutoProxyCreator.setBeanNames("userSe*"); // 查找符合条件的 bean，作为代理对象
			beanNameAutoProxyCreator.setInterceptorNames("myAdvisor"); // 查找符合条件的 bean，作为代理逻辑
			return beanNameAutoProxyCreator;
		}

		// 会被 DefaultAdvisorAutoProxyCreator 寻找到
		@Bean
		public DefaultPointcutAdvisor defaultPointcutAdvisor() {
			NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
			pointcut.addMethodName("test");

			DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
			defaultPointcutAdvisor.setPointcut(pointcut);
			defaultPointcutAdvisor.setAdvice(new MyAroundAdvice());

			return defaultPointcutAdvisor;
		}


		// @Bean // 需要关闭 @EnableAspectJAutoProxy
		public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
			// 寻找那些 bean 需要被代理（也就是 Advisor 类型的 bean），其实也是一个 BeanPostProcessor
			// 也可以直接通过 @Import(DefaultAdvisorAutoProxyCreator.class) 直接导入
			return new DefaultAdvisorAutoProxyCreator();
		}

		@Component
		public static class Test {

		}

	}

}
