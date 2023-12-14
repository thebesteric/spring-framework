package com.sourceflag.spring.propertyeditor;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.support.DefaultConversionService;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class App {
	public static void main(String[] args) throws IOException {

		// jdk 的方式
		StringToUserPropertyEditor propertyEditor = new StringToUserPropertyEditor();
		propertyEditor.setAsText("admin");
		User value = (User) propertyEditor.getValue();
		System.out.println(value + ": name = " + value.getName());

		// spring 的方式
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new StringToUserConverter());
		value = conversionService.convert("admin", User.class);
		System.out.println(value + ": name = " + value.getName());

		// 通用方式
		SimpleTypeConverter typeConverter = new SimpleTypeConverter();
		// jdk 的方式
		// typeConverter.registerCustomEditor(User.class, new StringToUserPropertyEditor());
		// spring 的方式
		typeConverter.setConversionService(conversionService);
		value = typeConverter.convertIfNecessary("admin", User.class);
		System.out.println(value + ": name = " + value.getName());

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		UserService userService = context.getBean("userService", UserService.class);
		userService.test();

		// 模拟传入 userService 字符串，但是类型时 TestService.class，尝试获取到 UserService
		System.out.println(context.getBean("userService", TestService.class));
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.propertyeditor")
	public static class AppConfig {

		@Bean
		public CustomEditorConfigurer customEditorConfigurer() {
			CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
			Map<Class<?>, Class<? extends PropertyEditor>> propertyEditorMap = new HashMap<>();

			// 表示 StringToUserPropertyEditor 可以将 String 转化成 User 类型，
			// 在 Spring 源码中，如果发现当前 value 的值是 String，而需要的类型是 User，就会使用该 PropertyEditor 来做类型转化
			propertyEditorMap.put(User.class, StringToUserPropertyEditor.class);

			customEditorConfigurer.setCustomEditors(propertyEditorMap);
			return customEditorConfigurer;
		}

		@Bean
		public ConversionServiceFactoryBean conversionService() {
			ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
			Set<Object> sets = new HashSet<>();
			sets.add(new StringToUserConverter());
			sets.add(new TestServiceToUserServiceConverter());
			conversionServiceFactoryBean.setConverters(sets);

			return conversionServiceFactoryBean;
		}
	}
}
