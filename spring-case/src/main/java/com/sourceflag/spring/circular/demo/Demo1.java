package com.sourceflag.spring.circular.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-09 23:57
 * @since 1.0
 */
public class Demo1 {

	public static Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
	// 一级缓存
	public static Map<String, Object> singletonObjects = new HashMap<>();

	public static void loadBeanDefinitions() {
		RootBeanDefinition aBd = new RootBeanDefinition(InstanceA.class);
		RootBeanDefinition bBd = new RootBeanDefinition(InstanceB.class);
		beanDefinitionMap.put("instanceA", aBd);
		beanDefinitionMap.put("instanceB", bBd);
	}

	public static void main(String[] args) throws Exception {
		// 加载 beanDefinition
		loadBeanDefinitions();

		// 循环创建 bean
		for (String beanName : beanDefinitionMap.keySet()) {
			getBean(beanName);
		}

	}

	public static Object getBean(String beanName) throws Exception {

		RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionMap.get(beanName);

		// 实例化
		Class<?> beanClass = beanDefinition.getBeanClass();
		Object instanceBean = beanClass.newInstance();

		// 属性赋值
		Field[] declaredFields = beanClass.getDeclaredFields();
		for (Field declaredField : declaredFields) {
			Autowired annotation = declaredField.getAnnotation(Autowired.class);
			if (annotation != null) {
				declaredField.setAccessible(true);
				String name = declaredField.getName();
				Object fieldObject = getBean(name);
				declaredField.set(instanceBean, fieldObject);
			}
		}
		// 初始化：InitializingBean，init-method

		// 加入一级缓存（单例池）
		singletonObjects.put(beanName, instanceBean);

		return instanceBean;
	}






}
