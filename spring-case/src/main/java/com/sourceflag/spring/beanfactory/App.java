package com.sourceflag.spring.beanfactory;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-05 11:10:42
 */
public class App {
	public static void main(String[] args) {
		// 搞清楚 BeanFactory 和 ApplicationContext 的区别
		// ApplicationContext 实现了 BeanFactory，BeanFactory 规范了 ApplicationContext 的行为
		// 可以把 ApplicationContext 想象为 4S 点，BeanFactory 想象为工厂，AC 需要从 BF 中获取车（BF 只负责造车），车的装潢只能由 4S 点来做

		// AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
		// TestService testService = context.getBean(TestService.class);
		// System.out.println(testService);

		// 使用 BeanFactory 实现 ApplicationContext，来说明 AC 和 BF 的关系
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
		reader.register(Config.class);

		AnnotatedGenericBeanDefinition configBD = (AnnotatedGenericBeanDefinition) beanFactory.getBeanDefinition("app.Config");
		AnnotationMetadata metadata = configBD.getMetadata();
		if (metadata.hasAnnotation(ComponentScan.class.getName())) {
			MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(ComponentScan.class.getName());
			List<String> basePackages = attributes.get("basePackages").stream().flatMap(arr -> Arrays.stream((String[]) arr)).collect(Collectors.toList());
			if (basePackages == null) {
				String packageName = App.class.getPackage().getName();
				basePackages.add(packageName);
			}
			ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);
			scanner.scan(basePackages.toArray(new String[0]));
		}

		TestService testService = beanFactory.getBean(TestService.class);
		System.out.println(testService);
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.beanfactory")
	@EnableAspectJAutoProxy
	public static class Config {

	}
}
