package com.sourceflag.spring.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-13 18:17:50
 */
@Component
public class UserService {

	@Autowired
	private static A a;

	@Autowired
	private B b;

	public void test() {
		System.out.println("a = " + a);
		System.out.println("b = " + getB());
	}

	@Lookup("b")
	public B getB() {
		return null;
	}

}
