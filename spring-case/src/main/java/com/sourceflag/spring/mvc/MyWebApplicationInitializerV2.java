package com.sourceflag.spring.mvc;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * MyWebApplicationInitializer
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-07 20:12
 * @since 1.0
 */
public class MyWebApplicationInitializerV2 extends AbstractAnnotationConfigDispatcherServletInitializer {

	// 传入 spring 的配置类
	@Override
	protected Class<?>[] getRootConfigClasses() {
		// 通过这里的配置类很简单，会排出掉 @Controller 注解，也就是所谓的父容器
		return new Class<?>[]{AppConfig.class};
	}

	// 传入 spring-mvc 的配置类
	@Override
	protected Class<?>[] getServletConfigClasses() {
		// 这里的配置类会去扫描 @Controller 注解，同时会开启 @EnableWebMvc，也就是所谓的子容器
		return new Class<?>[]{AppConfig.class};
	}

	// 配置映射路径
	@Override
	protected String[] getServletMappings() {
		// 拦截请求的路径
		return new String[]{"/"};
	}

	@Override
	protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
		ApplicationContextInitializer<?> applicationContextInitializer = applicationContext -> {
			System.out.println("==== spring is starting ====");
		};
		return new ApplicationContextInitializer<?>[]{applicationContextInitializer};
	}
}
