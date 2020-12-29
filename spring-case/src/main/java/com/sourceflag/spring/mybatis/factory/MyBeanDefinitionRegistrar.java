package com.sourceflag.spring.mybatis.factory;

import com.sourceflag.spring.mybatis.service.OrderMapper;
import com.sourceflag.spring.mybatis.service.UserMapper;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBeanDefinitonRegistar
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 23:29
 * @since 1.0
 */
public class MyBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		// 这里可以用 扫描 来实现
		List<Class<?>> mapperClasses = new ArrayList<>();
		mapperClasses.add(UserMapper.class);
		mapperClasses.add(OrderMapper.class);

		for (Class<?> mapperClass : mapperClasses) {
			AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
			beanDefinition.setBeanClass(MyFactoryBean.class);
			beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
			// 注册到容器
			registry.registerBeanDefinition(mapperClass.getSimpleName(), beanDefinition);
		}
	}
}
