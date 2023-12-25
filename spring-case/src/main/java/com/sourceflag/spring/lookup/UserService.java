package com.sourceflag.spring.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-15 14:23:41
 */
@Component
public abstract class UserService {

	@Autowired
	private User user;

	public void test() {
		System.out.println(getUser());
	}

	@Lookup("user")
	public abstract User getUser();

}
