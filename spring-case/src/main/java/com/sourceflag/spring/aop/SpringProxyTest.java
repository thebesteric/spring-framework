package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.service.UserService;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;

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

		// 代理逻辑
		proxyFactory.addAdvice(new MethodBeforeAdvice() {
			@Override
			public void before(Method method, Object[] args, Object target) throws Throwable {
				System.out.println(method.getName() + " 方法执行前的逻辑...");
			}
		});
		// 代理逻辑
		proxyFactory.addAdvice(new AfterReturningAdvice() {
			@Override
			public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
				System.out.println(method.getName() + " 方法返回后的逻辑...");
			}
		});

		// 生成代理对象
		// UserServiceInterface proxy = (UserServiceInterface) proxyFactory.getProxy();
		UserService proxy = (UserService) proxyFactory.getProxy();
		proxy.test();
	}

}
