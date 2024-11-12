package com.sourceflag.spring.imported;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 15:52:22
 */
@Configuration
public class MyConfiguration {

	@Bean
	public UserService userService() {
		return new UserService();
	}

}
