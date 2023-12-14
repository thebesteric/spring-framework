package com.sourceflag.spring.simulate;

import com.sourceflag.spring.simulate.app.service.UserService;
import com.sourceflag.spring.simulate.framework.SimulateAnnotationConfigApplicationContext;

public class SimulateApp {
	public static void main(String[] args) {
		SimulateAnnotationConfigApplicationContext context = new SimulateAnnotationConfigApplicationContext(SimulateAppConfig.class);
		// UserService userService = (UserService) context.getBean("userService");
		UserService userService = context.getBean(UserService.class);
		userService.test();
	}
}
