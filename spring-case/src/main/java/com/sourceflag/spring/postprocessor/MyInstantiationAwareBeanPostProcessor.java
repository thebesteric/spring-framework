package com.sourceflag.spring.postprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * MyInstantiationAwareBeanPostProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-18 18:32:15
 */
@Component
public class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		if ("userService".equals(beanName)) {
			System.out.println("实例化前");
		}
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		if ("userService".equals(beanName)) {
			UserService userService = (UserService) bean;
			userService.call();
			System.out.println("实例化后");
		}
		return true;
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		if ("userService".equals(beanName)) {
			for (Field field : bean.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(MyInject.class)) {
					field.setAccessible(true);
					MyInject myInject = field.getAnnotation(MyInject.class);
                    try {
                        field.set(bean, myInject.value());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
			}
		}
		return pvs;
	}
}
