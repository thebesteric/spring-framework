package com.sourceflag.spring.ioc;

import org.springframework.stereotype.Component;

/**
 * GenericTypeServiceImpl
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-19 20:52:45
 */
@Component
public class GenericTypeServiceImpl extends GenericTypeService<UserService, OrderService> {

	public void test() {
		System.out.println("GenericTypeServiceImpl A = " + a);
		System.out.println("GenericTypeServiceImpl B = " + b);
	}

}
