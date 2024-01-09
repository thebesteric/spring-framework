package com.sourceflag.spring.mybatis.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

/**
 * MyMapperScanner
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 23:22:29
 */
public class MyMapperScanner extends ClassPathBeanDefinitionScanner {
	public MyMapperScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		// 调用父类的方法找到所有符合条件的 BD
		Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);

		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
			GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
			// 设置注入模式，这样可以自动找 setter 方法
			beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
			// 指定构造方法参数（这里的顺序不能错）
			beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
			// 设置 beanClass（这里的顺序不能错）
			beanDefinition.setBeanClassName(MyFactoryBean.class.getName());
		}
		return beanDefinitionHolders;
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		// 表示只有接口才符合条件
		return beanDefinition.getMetadata().isInterface();
	}
}
