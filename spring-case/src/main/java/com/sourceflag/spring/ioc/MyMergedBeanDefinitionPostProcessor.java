package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * MyBeanPostProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-18 14:10:06
 */
@Component
public class MyMergedBeanDefinitionPostProcessor implements MergedBeanDefinitionPostProcessor {
	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if ("userService".equals(beanName)) {
			TestService testService = new TestService();
			testService.setInjectType("手动给 userService 添加 testService");
			beanDefinition.getPropertyValues().add("testService", testService);
		}
	}
}
