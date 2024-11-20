package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TestService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-18 14:13:33
 */
@Component
public class TestService {

	@Autowired
	private MultiService multi;


	public String injectType = "Spring Inject";

	public String getInjectType() {
		return injectType;
	}

	public void setInjectType(String injectType) {
		this.injectType = injectType;
	}

	public void test() {
		System.out.println("multi = " + multi);
	}
}
