package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.service.UserService;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * SpringProxyTest
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-30 14:16
 * @since 1.0
 */
public class SpringProxyTest {

	public static void main(String[] args) {
		ProxyFactory proxyFactory = new ProxyFactory();

		// proxyFactory.setOptimize(true); // CGLIB 动态代理
		// proxyFactory.setProxyTargetClass(true); // CGLIB 动态代理

		// 目标对象
		proxyFactory.setTarget(new UserService()); // CGLIB 动态代理
		// proxyFactory.addInterface(UserServiceInterface.class); // 如果设置了接口，就会使用 JDK 动态代理

		// 代理逻辑：方法执行之前
		// proxyFactory.addAdvice(new MethodBeforeAdvice() {
		// 	@Override
		// 	public void before(Method method, Object[] args, Object target) throws Throwable {
		// 		System.out.println(method.getName() + " 方法执行前的逻辑...");
		// 	}
		// });

		// 代理逻辑：方法执行之后
		// proxyFactory.addAdvice(new AfterReturningAdvice() {
		// 	@Override
		// 	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		// 		System.out.println(method.getName() + " 方法返回后的逻辑...");
		// 	}
		// });

		// 代理逻辑：发生异常
		// proxyFactory.addAdvice(new MyThrowsAdvice());

		// 代理逻辑：完全自己控制，相当于 around
		// proxyFactory.addAdvice(new MethodInterceptor() {
		// 	@Nullable
		// 	@Override
		// 	public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
		// 		System.out.println(invocation.getMethod().getName() + " 方法执行前的逻辑 around...");
		// 		Object result = invocation.proceed(); // 不仅是执行代理方法，还会按链路执行其他的 advice
		// 		System.out.println(invocation.getMethod().getName() + " 方法返回后的逻辑 around...");
		// 		return result;
		// 	}
		// });

		// Advisor 相当于 Pointcut + Advice
		// 我们可以利用 Pointcut 过滤哪些方法需要代理，哪些不需要
		proxyFactory.addAdvisor(new PointcutAdvisor() {
			@Override
			public Pointcut getPointcut() {
				return new StaticMethodMatcherPointcut() {
					@Override
					public boolean matches(Method method, Class<?> targetClass) {
						return method.getName().equals("test");
					}
				};
			}

			@Override
			public Advice getAdvice() {
				return new MethodInterceptor() {
					@Nullable
					@Override
					public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
						System.out.println(invocation.getMethod().getName() + " 方法执行前的逻辑 around...");
						Object result = invocation.proceed(); // 不仅是执行代理方法，还会按链路执行其他的 advice
						System.out.println(invocation.getMethod().getName() + " 方法返回后的逻辑 around...");
						return result;
					}
				};
			}

			@Override
			public boolean isPerInstance() {
				return false;
			}
		});


		// 生成代理对象，如果使用的是 proxyFactory.addInterface
		// UserServiceInterface proxy = (UserServiceInterface) proxyFactory.getProxy();

		UserService proxy = (UserService) proxyFactory.getProxy();
		proxy.other();
		proxy.test();
	}

	// 异常 advice 必须是独立的 public 类，不能是匿名内部类
	public static class MyThrowsAdvice implements ThrowsAdvice {
		public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
			System.out.println("发生了异常1..." + ex.getMessage());
		}
	}
}
