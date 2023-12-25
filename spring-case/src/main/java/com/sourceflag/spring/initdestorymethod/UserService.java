package com.sourceflag.spring.initdestorymethod;


import org.springframework.beans.factory.InitializingBean;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-19 23:52:53
 */
public class UserService implements InitializingBean {

	public void init() {
		System.out.println("init method");
	}

	public void destroy() {
		System.out.println("destroy method");
	}

	public void externallyManagedInitMethod() {
		System.out.println("externallyManagedInitMethod");
	}

	@Override
	public void afterPropertiesSet() {
		System.out.println("afterPropertiesSet");
	}
}
