package com.sourceflag.spring.constructor;

import org.springframework.stereotype.Component;

@Component
public class UserService {

	// @Autowired(required = false)
	public UserService() {
		System.out.println(0);
	}

	// @Autowired(required = false)
	public UserService(OrderService orderService) {
		System.out.println(1);
	}
	// @Autowired(required = false)
	public UserService(OrderService orderService, OrderService orderService1) {
		System.out.println(2);
	}

	public UserService(OrderService orderService, OrderService orderService1, OrderService orderService2) {
		System.out.println(3);
	}

	public UserService(OrderService orderService, OrderService orderService1, OrderServiceInterface orderServiceInterface) {
		System.out.println(4);
	}
}