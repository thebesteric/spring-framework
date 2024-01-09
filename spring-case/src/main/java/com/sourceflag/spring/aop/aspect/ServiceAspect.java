package com.sourceflag.spring.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * UserAspect
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-30 14:47
 * @since 1.0
 */
@Aspect
@Component
public class ServiceAspect {

	// 注解 + 方法 = PointCut + Advice = Advisor
	@Before("execution(public void com.sourceflag.spring.aop.service.*.*())") // PointCut
	public void before(JoinPoint joinPoint) { // Advice
		System.out.println(getMethodName(joinPoint) + " before");
	}

	@After("execution(public void com.sourceflag.spring.aop.service.*.*())")
	public void after(JoinPoint joinPoint) {
		System.out.println(getMethodName(joinPoint) + " after");
	}

	@AfterReturning(returning = "result", pointcut = "execution(public * com.sourceflag.spring.aop.service.*.*(..))")
	public void afterReturning(JoinPoint joinPoint, Object result) {
		System.out.println("result = " + result);
		System.out.println(getMethodName(joinPoint) + " afterReturning");
	}

	@AfterThrowing("execution(public void com.sourceflag.spring.aop.service.*.*())")
	public void afterThrowing(JoinPoint joinPoint) {
		System.out.println(getMethodName(joinPoint) + " afterThrowing");
	}

	@Around("execution(public void com.sourceflag.spring.aop.service.*.*())")
	public void around(ProceedingJoinPoint joinPoint) {
		try {
			System.out.println(getMethodName(joinPoint) + " around before");
			joinPoint.proceed();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		System.out.println(getMethodName(joinPoint) + " around after");
	}

	private String getMethodName(JoinPoint joinPoint) {
		return joinPoint.getSignature().getName();
	}
}
