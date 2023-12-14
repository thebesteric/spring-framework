package com.sourceflag.spring.filter;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-15 00:25:18
 */
public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(context.getBean(TestService.class));
		System.out.println(context.getBean(UserService.class));
	}

	// FilterType.ASSIGNABLE_TYPE：某个类
	// FilterType.ANNOTATION：某个注解
	// FilterType.ASPECTJ：某个 AspectJ 表达式
	// FilterType.REGEX：某个正则表达式
	// FilterType.CUSTOM：自定义
	@Configuration
	@ComponentScan(value = "com.sourceflag.spring.filter",
			excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserService.class)},
			includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TestService.class)}
	)
	public static class AppConfig {

	}

}
