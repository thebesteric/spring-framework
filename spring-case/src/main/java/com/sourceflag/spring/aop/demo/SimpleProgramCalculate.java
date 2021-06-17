package com.sourceflag.spring.aop.demo;

/**
 * SimpleProgramCalculate
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-24 00:07
 * @since 1.0
 */
public class SimpleProgramCalculate implements ProgramCalculate {
	@Override
	public String toBinary(int value) {
		System.out.println("执行目标方法：toBinary");
		return Integer.toBinaryString(value);
	}
}
