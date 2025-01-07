package com.sourceflag.spring.constructor.lenient;

import org.springframework.util.MethodInvoker;

/**
 * Main
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-22 17:53:11
 */
public class Main {
	public static void main(String[] args) {
		Object[] arguments = new Object[] {new A()};
		System.out.println(MethodInvoker.getTypeDifferenceWeight(new Class<?>[]{A.class}, arguments));
		System.out.println(MethodInvoker.getTypeDifferenceWeight(new Class<?>[]{B.class}, arguments));
		System.out.println(MethodInvoker.getTypeDifferenceWeight(new Class<?>[]{C.class}, arguments));
		System.out.println(MethodInvoker.getTypeDifferenceWeight(new Class<?>[]{D.class}, arguments));
	}
}
