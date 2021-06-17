package com.sourceflag.spring.mvc;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.sourceflag.spring.mvc.controller.TestHttpRequestHandlerController;
import com.sourceflag.spring.mvc.controller.TestInterfaceController;
import com.sourceflag.spring.mvc.controller.TestServlet;
import com.sourceflag.spring.mvc.interceptor.MyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.SimpleServletHandlerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Config 的方式配置 Web Mvc：
 *
 * @see WebMvcConfigurer 接口
 * @see WebMvcConfigurerAdapter 实现类（过时）
 * @see WebMvcConfigurationSupport 实现类
 * @see EnableWebMvc 注解
 */
@Configuration
@ComponentScan("com.sourceflag.spring.mvc")
@EnableWebMvc // <mvc:annotation-driven/>
public class AppConfig implements WebMvcConfigurer {

	/**
	 * 视图处理
	 */
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/page/", ".html");
	}

	/**
	 * 解析器添加
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
		converters.add(fastJsonHttpMessageConverter);
	}

	/**
	 * 拦截器添加
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		InterceptorRegistration interceptorRegistration = registry.addInterceptor(new MyInterceptor());
		interceptorRegistration.addPathPatterns("/test.do");
		WebMvcConfigurer.super.addInterceptors(registry);
	}

	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		Map<String, Object> urlMap = new HashMap<>();
		urlMap.put("/testServlet.do", new TestServlet());
		urlMap.put("/testInterfaceController.do", testInterfaceController());
		urlMap.put("/testHttpRequestHandlerController.do", testHttpRequestHandlerController());
		return new SimpleUrlHandlerMapping(urlMap);
	}

	// 声明一个 Controller
	@Bean
	public TestInterfaceController testInterfaceController() {
		return new TestInterfaceController();
	}

	@Bean
	public TestHttpRequestHandlerController testHttpRequestHandlerController() {
		return new TestHttpRequestHandlerController();
	}


	// 处理普通的 Servlet 的 Adapter
	@Bean
	public SimpleServletHandlerAdapter simpleServletHandlerAdapter() {
		return new SimpleServletHandlerAdapter();
	}

	// 处理 Controller 接口的 Adapter
	@Bean
	public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
		return new SimpleControllerHandlerAdapter();
	}

	// 处理 HttpRequestHandler 接口的 Adapter
	@Bean
	public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
		return new HttpRequestHandlerAdapter();
	}


}
