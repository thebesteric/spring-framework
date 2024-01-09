package com.sourceflag.spring.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * E
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-27 00:18:23
 */
@Component
public class E {


	@Autowired
	// @Resource
	public void setXXX(A a, B b) {
		System.out.println("====" + a);
		System.out.println("====" + b);
	}
}
