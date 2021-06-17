package com.sourceflag.spring.aop.demo;

import org.springframework.stereotype.Component;

/**
 * Calculate
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-23 23:28
 * @since 1.0
 */
@Component
public class SimpleCalculate implements Calculate{

	@Override
	public double add(double num1, double num2) {
		System.out.println("执行目标方法：add");
		return num1 + num2;
	}

	@Override
	public double sub(double num1, double num2) {
		System.out.println("执行目标方法：sub");
		return num1 - num2;
	}

	@Override
	public double multi(double num1, double num2) {
		System.out.println("执行目标方法：multi");
		return num1 * num2;
	}

	@Override
	public double div(double num1, double num2) {
		System.out.println("执行目标方法：div");
		return num1 / num2;
	}
}
