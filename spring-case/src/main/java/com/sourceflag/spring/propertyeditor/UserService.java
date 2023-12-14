package com.sourceflag.spring.propertyeditor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserService {

	@Value("admin")
	private User adminUser;

	public void test() {
		System.out.println(adminUser.getName());
	}

	public UserService() {

	}

	public void setAdminUser(User adminUser) {
		this.adminUser = adminUser;
	}
}
