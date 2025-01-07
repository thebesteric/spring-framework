package com.sourceflag.spring.constructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MyService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-22 16:14:07
 */
@Component
public class MyService {

	@Autowired(required = false)
	public MyService(OrderService orderService) {
		System.out.println(1);
	}

	@Autowired(required = false)
	public MyService(OrderService orderService1, OrderService orderService2) {
		System.out.println(2);
	}

	// @Autowired(required = false)
	// public MyService(UserService userService1, UserService userService2) {
	// 	System.out.println(2);
	// }

}
