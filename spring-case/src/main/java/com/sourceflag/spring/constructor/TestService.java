package com.sourceflag.spring.constructor;

import org.springframework.beans.factory.annotation.Autowired;

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

	// public TestService(NotBeanService notBeanService) {
	// 	System.out.println("TestService construct: " + notBeanService);
	// }

	@Autowired(required = true)
	public TestService(OrderService orderService) {
		System.out.println("TestService construct: " + 1);
	}

	// @Autowired(required = false)
	// public TestService(OrderService orderService, OrderService orderService1) {
	// 	System.out.println("TestService construct: " + 2);
	// }

	// @Autowired(required = false)
	// public TestService(OrderService orderService, OrderService orderService1, OrderService orderService2) {
	// 	System.out.println("TestService construct: " + 3);
	// }

	// @Autowired(required = false)
	// public TestService(OrderService orderService, OrderService orderService1, OrderService orderService2, NotBeanService notBeanService) {
	// 	System.out.println("TestService construct: " + 4);
	// }

	// @Autowired(required = false)
	// @ConstructorProperties({"orderService", "orderService"})
	// public TestService(OrderService orderService, UserService userService) {
	// 	System.out.println("TestService construct: " + 2.1);
	// }

	// @Autowired(required = false)
	// public TestService(OrderService orderService, OrderServiceInterface orderServiceInterface) {
	// 	System.out.println("TestService construct: " + 2.2);
	// }

}
