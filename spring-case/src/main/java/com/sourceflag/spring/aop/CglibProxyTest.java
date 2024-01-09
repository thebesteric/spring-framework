package com.sourceflag.spring.aop;

import com.sourceflag.spring.aop.service.UserService;
import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * TODO
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022/3/17
 */
public class CglibProxyTest {
	public static void main(String[] args) {
		UserService target = new UserService();

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(UserService.class);
		enhancer.setCallbacks(new Callback[]{new MethodInterceptor() {
			@Override
			public Object intercept(Object proxyObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
				System.out.println("代理前的逻辑...");

				// 执行 代理对象 的 代理方法（会循环调用，报错）
				// Object result = methodProxy.invoke(proxyObject, args);

				// 执行 代理对象 的 原始方法（会循环调用，报错）
				// Object result = method.invoke(proxyObject, args);

				// 执行 目标对象 的 原始方法
				// Object result = method.invoke(target, args);

				// 执行 目标对象 的 代理方法
				// Object result = methodProxy.invoke(target, args);

				// 执行 代理对象 的 父类的方法，就相当于执行 target 的 原始方法
				Object result = methodProxy.invokeSuper(proxyObject, args);

				System.out.println("代理后的逻辑...");
				return result;
			}
		}, NoOp.INSTANCE});

		enhancer.setCallbackFilter(new CallbackFilter() {
			@Override
			public int accept(Method method) {
				// 返回的数字，就是 Callback[] 数组的下标
				if (method.getName().equals("other")) {
					return 1; // 执行第二个代理逻辑
				}
				return 0; // 执行第一个代理逻辑
			}
		});

		UserService userServiceProxy = (UserService) enhancer.create();
		userServiceProxy.test();
		userServiceProxy.other();
	}
}
