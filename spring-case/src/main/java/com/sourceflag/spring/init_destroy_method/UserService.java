package com.sourceflag.spring.init_destroy_method;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-19 23:52:53
 */
public class UserService implements InitializingBean, DisposableBean {

	public void init() {
		System.out.println("init method");
	}

	public void over() {
		System.out.println("over method");
	}

	public void externallyManagedInitMethod() {
		System.out.println("externallyManagedInitMethod");
	}

	@PostConstruct
	public void postConstruct1() {
		System.out.println("postConstruct1");
	}

	@PostConstruct
	public void postConstruct2() {
		System.out.println("postConstruct2");
	}


	@PreDestroy
	public void preDestroy1() {
		System.out.println("preDestroy1");
	}

	@PreDestroy
	public void preDestroy2() {
		System.out.println("preDestroy2");
	}

	@Override
	public void afterPropertiesSet() {
		System.out.println("afterPropertiesSet");
	}

	@Override
	public void destroy() {
		System.out.println("destroy method");
	}
}
