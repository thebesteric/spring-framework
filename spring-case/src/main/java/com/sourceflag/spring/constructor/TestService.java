package com.sourceflag.spring.constructor;

/**
 * TestService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-27 23:01:50
 */
public class TestService {

	// public TestService() {
	// 	System.out.println("TestService construct: " + 0);
	// }

	// @Autowired(required = false)
	public TestService(OrderService orderService) {
		System.out.println("TestService construct: " + 1);
	}

	// @Autowired(required = false)
	public TestService(OrderService orderService, OrderService orderService1) {
		System.out.println("TestService construct: " + 2);
	}

	// @Autowired(required = false)
	// @ConstructorProperties({"orderService", "orderService"})
	public TestService(OrderService orderService, UserService userService) {
		System.out.println("TestService construct: " + 2.1);
	}

	// @Autowired(required = false)
	public TestService(OrderService orderService, OrderServiceInterface orderServiceInterface) {
		System.out.println("TestService construct: " + 2.2);
	}

}
