package com.sourceflag.spring.circular.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * JdkProxyBeanPostProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-11 23:15
 * @since 1.0
 */
// @Component
public class JdkProxyBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {

		// 假设：A 被切点命中，则需要创建动态代理 @PointCut("execution(*.*..InstanceA.*.(..))")
		if(bean instanceof InstanceA) {
			JdkDynamicProxy jdkDynamicProxy = new JdkDynamicProxy(bean);
			return jdkDynamicProxy.getProxy();
		}

		return SmartInstantiationAwareBeanPostProcessor.super.getEarlyBeanReference(bean, beanName);
	}
}
