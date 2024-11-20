package com.sourceflag.spring.autowired_mode;

import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * MyMergedBeanDefinitionPostProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-15 16:47:07
 */
@Component
public class MyMergedBeanDefinitionPostProcessor implements MergedBeanDefinitionPostProcessor {
	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if ("userService".equals(beanName)) {
			// 手动给 userService 添加 orderService
			beanDefinition.getPropertyValues().add("orderService", new OrderService().setAutowiredBy("手动给 userService 添加 orderService"));
		}
	}
}
