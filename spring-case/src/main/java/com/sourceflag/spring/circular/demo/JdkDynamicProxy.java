package com.sourceflag.spring.circular.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JdkDynamicProxy
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-11 23:21
 * @since 1.0
 */
public class JdkDynamicProxy implements InvocationHandler {

	Object target;

	public JdkDynamicProxy(Object target) {
		this.target = target;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxy() {
		return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("Do something before");
		Object result = method.invoke(target, args);
		System.out.println("Do something after");
		return result;
	}
}
