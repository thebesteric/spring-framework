package com.sourceflag.spring.transactional.invalid;

import com.sourceflag.spring.transactional.invalid.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * InvalidApp
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-11 15:10:57
 */

public class InvalidApp {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);

		UserService userService = (UserService) ctx.getBean("userService");

		// 为什么能正常插入？
		userService.test1();

		// 不会抛出异常
		// userService.test2();

		// 抛出异常
		// userService.test3();

		// 抛出异常
		// userService.test4();
	}

}
