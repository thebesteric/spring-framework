package com.sourceflag.spring.metadata;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class UserService implements ApplicationContextAware, InitializingBean {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	public static class UserInner {

	}
}
