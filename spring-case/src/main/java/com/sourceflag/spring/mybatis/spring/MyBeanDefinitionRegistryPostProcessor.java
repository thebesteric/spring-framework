package com.sourceflag.spring.mybatis.spring;

import com.sourceflag.spring.mybatis.mapper.OrderMapper;
import com.sourceflag.spring.mybatis.mapper.UserMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * MyBeanDefinitionRegistry
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 23:10:44
 */
// @Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		// 思考：利用 BeanDefinitionRegistryPostProcessor 和注册 BD 的形式，完成 Mapper 的注册
		// 问题：需要硬编码
		// 改进：移植到 BeanDefinitionRegistrar，我们期望可以通过扫描完成 bean 的注册
		AbstractBeanDefinition beanDefinition1 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		beanDefinition1.setBeanClass(MyFactoryBean.class);
		beanDefinition1.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
		registry.registerBeanDefinition("userMapper", beanDefinition1);

		AbstractBeanDefinition beanDefinition2 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		beanDefinition2.setBeanClass(MyFactoryBean.class);
		beanDefinition2.getConstructorArgumentValues().addGenericArgumentValue(OrderMapper.class);
		registry.registerBeanDefinition("orderMapper", beanDefinition2);
	}
}
