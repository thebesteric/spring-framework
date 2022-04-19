package com.sourceflag.spring.mvc;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * MyWebApplicationInitializer
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-07 20:12
 * @since 1.0
 */
// 加了 abstract 就不会被 servlet 3.0 的规范所调用
public abstract class MyWebApplicationInitializer implements WebApplicationInitializer {

	/**
	 * 因为 servlet 3.0 的一个新规范（ServletContainerInitializer），
	 * 而 tomcat 也遵守了了这个规范，所以会调用 实现了 ServletContainerInitializer 接口的 onStartup 方法
	 *
	 * Spring 定义了一个 org.springframework.web.SpringServletContainerInitializer 类，实现了 servlet 3.0 的这个新规范
	 *
	 * Spring 实现方式
	 * spring-web\META-INF\services\javax.servlet.ServletContainerInitializer，是指需要实现的接口
	 * javax.servlet.ServletContainerInitializer 文件里面的 org.springframework.web.SpringServletContainerInitializer 是实现类
	 *
	 * 注解 @HandlesTypes 是 javax.servlet.annotation.HandlesTypes 定义的注解
	 * 注解 @HandlesTypes(WebApplicationInitializer.class) 会由容器进行解析里面定义的接口，找出所有实现类，并回调给 Set<Class<?>> webAppInitializerClasses
	 * Spring 再对 webAppInitializerClasses 的子类进行遍历，分别调用其 onStartup 的方法
	 *
	 * @param servletContext Web上下文对象
	 * @throws ServletException
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		System.out.println("=== MyWebApplicationInitializer 被调用了 ===");

		// Load Spring web application configuration
		AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
		ac.register(AppConfig.class);
		// ac.refresh();

		// Create and register the DispatcherServlet
		DispatcherServlet servlet = new DispatcherServlet(ac);
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("*.do");
	}
}
