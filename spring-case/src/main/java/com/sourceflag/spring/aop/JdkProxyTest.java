package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.service.UserService;
import com.sourceflag.spring.aop.service.UserServiceInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JdkProxyTest
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 11:45
 * @since 1.0
 */
public class JdkProxyTest {

	public static void main(String[] args) {
		UserServiceInterface proxyInstance = (UserServiceInterface) Proxy.newProxyInstance(JdkProxyTest.class.getClassLoader(), UserService.class.getInterfaces(), new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("代理前的逻辑...");
				Object result = method.invoke(new UserService(), args);
				System.out.println("代理后的逻辑...");
				return result;
			}
		});
		proxyInstance.test();
	}

}
