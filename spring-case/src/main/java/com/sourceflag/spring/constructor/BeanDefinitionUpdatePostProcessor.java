package com.sourceflag.spring.constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

// @Component
public class BeanDefinitionUpdatePostProcessor implements BeanFactoryPostProcessor {
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		GenericBeanDefinition bd = (GenericBeanDefinition) beanFactory.getBeanDefinition("userService");
		// bd.setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		bd.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
	}
}
