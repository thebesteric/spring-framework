package com.sourceflag.spring.init_destroy_method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * MyDestructionAwareBeanPostProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-15 09:23:15
 */
@Component
public class MyDestructionAwareBeanPostProcessor implements DestructionAwareBeanPostProcessor {
	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		System.out.println(beanName + ": 即将被销毁");
	}

	@Override
	public boolean requiresDestruction(Object bean) {
		boolean requiresDestruction = true;
		System.out.println(bean.getClass().getSimpleName() + ": " + (requiresDestruction ? "需要被销毁" : "不需要被销毁"));
		return requiresDestruction;
	}
}
