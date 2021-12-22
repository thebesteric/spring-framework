package com.sourceflag.spring.simulate.framework;

import org.springframework.lang.Nullable;

public interface BeanPostProcessor {

	default Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
		return bean;
	}

	default Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
		return bean;
	}

}
