package com.sourceflag.spring.aop.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * MyAdvisor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-01 00:50
 * @since 1.0
 */
// @Component
public class MyAdvisor implements PointcutAdvisor {

	@Override
	public Pointcut getPointcut() {
		NameMatchMethodPointcut methodPointcut = new NameMatchMethodPointcut();
		methodPointcut.addMethodName("test");
		return methodPointcut;
	}

	@Override
	public Advice getAdvice() {
		return new MethodBeforeAdvice() {
			@Override
			public void before(Method method, Object[] args, Object target) throws Throwable {
				System.out.println("方法执行前...");
			}
		};
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}
}
