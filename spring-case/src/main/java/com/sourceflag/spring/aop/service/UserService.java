package com.sourceflag.spring.aop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 11:45
 * @since 1.0
 */
@Component
public class UserService implements UserServiceInterface {

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Override
	public String test() {
		System.out.println("user test");

		// if (new Random().nextBoolean()) {
		// 	throw new NullPointerException("something wrong");
		// }


		// UserService userServiceProxy = (UserService) AopContext.currentProxy();
		// userServiceProxy.other();

		// userService.other();

		// other();

		return "userService execute succeed";
	}

	public void other() {
		System.out.println("other");
	}
}
