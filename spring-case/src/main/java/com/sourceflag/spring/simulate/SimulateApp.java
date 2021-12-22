package com.sourceflag.spring.simulate;

import com.sourceflag.spring.simulate.app.service.UserService;
import com.sourceflag.spring.simulate.framework.SimulateApplicationContext;

public class SimulateApp {
	public static void main(String[] args) {
		SimulateApplicationContext context = new SimulateApplicationContext(SimulateAppConfig.class);
		UserService userService = (UserService) context.getBean("userService");
		userService.test();
	}
}
