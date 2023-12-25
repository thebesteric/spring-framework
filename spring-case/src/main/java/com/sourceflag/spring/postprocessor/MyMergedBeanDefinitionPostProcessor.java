package com.sourceflag.spring.postprocessor;

import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * MyBeanPostProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 23:00:00
 */
@Component
public class MyMergedBeanDefinitionPostProcessor implements MergedBeanDefinitionPostProcessor {

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if ("userService".equals(beanName)) {
			OrderService orderService = new OrderService();
			System.out.println("orderService by pvs = " + orderService);
			beanDefinition.getPropertyValues().add("orderService", orderService);
			beanDefinition.setInitMethodName("myInitMethod");
		}
	}
}
