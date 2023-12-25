package com.sourceflag.spring.factorybean;

import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class MyFactoryBean implements SmartFactoryBean<UserService> {

	@Override
	public boolean isEagerInit() {
		// 是否要提前创建
		return true;
	}

	@Override
	public boolean isSingleton() {
		// 是否单例
		return true;
	}

	@Override
	public UserService getObject() throws Exception {
		return new UserService();
	}

	@Override
	public Class<?> getObjectType() {
		return UserService.class;
	}
}
