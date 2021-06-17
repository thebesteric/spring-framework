package com.sourceflag.spring.aop.demo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * SimpleCalcLogAspect
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-23 23:44
 * @since 1.0
 */
@Aspect
@Component
public class SimpleCalcLogAspect {

	// 引入：不会走到通知逻辑，只会默认实现改方法
	@DeclareParents(value = "com.sourceflag.spring.aop.demo.SimpleCalculate", // 动态实现类
			defaultImpl = SimpleProgramCalculate.class) // 引入的接口默认实现
	public static ProgramCalculate programCalculate; // 引入的接口

	@Pointcut("execution(* com.sourceflag.spring.aop.demo.SimpleCalculate.*(..))")
	public void pointcut(){}

	@Before(value = "pointcut()")
	public void before(JoinPoint joinPoint){
		String methodName = joinPoint.getSignature().getName();
		String log = String.format("执行目标方法 %s 的<前置通知>, 入参 %s", methodName, Arrays.toString(joinPoint.getArgs()));
		System.out.println(log);
	}

	@After(value = "pointcut()")
	public void around(JoinPoint joinPoint){
		String methodName = joinPoint.getSignature().getName();
		String log = String.format("执行目标方法 %s 的<后置通知>, 入参 %s", methodName, Arrays.toString(joinPoint.getArgs()));
		System.out.println(log);
	}

	@AfterReturning(value = "pointcut()", returning = "result")
	public void afterReturning(JoinPoint joinPoint, Object result) {
		String methodName = joinPoint.getSignature().getName();
		String log = String.format("执行目标方法 %s 的<返回通知>, 入参 %s, 返回值 %s", methodName, Arrays.toString(joinPoint.getArgs()), result);
		System.out.println(log);
	}

	@AfterThrowing(value = "pointcut()")
	public void afterThrowing(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		String log = String.format("执行目标方法 %s 的<异常通知>, 入参 %s", methodName, Arrays.toString(joinPoint.getArgs()));
		System.out.println(log);
	}

}
