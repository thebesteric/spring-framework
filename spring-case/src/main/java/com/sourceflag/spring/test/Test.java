package com.sourceflag.spring.test;

import com.sourceflag.spring.test.service.C;
import com.sourceflag.spring.test.service.TestService;
import com.sourceflag.spring.test.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Test
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-13 13:24:07
 */
public class Test {

	public static void main(String[] args) throws IOException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		context.registerShutdownHook();

		// DefaultListableBeanFactory context = new DefaultListableBeanFactory();

		// AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// beanDefinition.setBeanClass(UserService.class);

		// context.registerBeanDefinition("userService", beanDefinition);

		// AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(context);
		// reader.register(UserService.class);

		// ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
		// int num = scanner.scan("com.sourceflag.spring.test");
		// System.out.println("num = " + num);

		// Resource resource = context.getResource("mybatis.xml");
		// Resource resource = context.getResource("file:///Users/wangweijun/IdeaProjects/spring-framework/spring-case/src/main/resources/mybatis.xml");
		// Resource resource = context.getResource("classpath:mybatis.xml");
		// System.out.println(resource.getFile());

		// Resource httpResource = context.getResource("https://www.baidu.com");
		// System.out.println(httpResource.getURL());


		UserService userService = (UserService) context.getBean("userService");
		System.out.println(userService);
		userService.test();
		userService.test();

		C c = context.getBean(C.class);
		System.out.println("c = " + c);
		System.out.println(c.getB());


		@SuppressWarnings({"rawtypes"})
		TestService testService = context.getBean(TestService.class);
		testService.test();
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.test")
	public static class Config {

	}

}
