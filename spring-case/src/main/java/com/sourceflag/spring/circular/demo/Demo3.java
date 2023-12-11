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
 * 增加：二级缓存，earlySingletonObjects，将完整的 bean 和 半成品 bean 分离开来，避免读取到半成品 bean
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-09 23:57
 * @since 1.0
 */
public class Demo3 {

	public static Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
	// 一级缓存
	public static Map<String, Object> singletonObjects = new HashMap<>();
	// 二级缓存：解决避免读取到不完整的 bean
	public static Map<String, Object> earlySingletonObjects = new HashMap<>();

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

		// 解决了循环依赖？
		InstanceA instanceA = (InstanceA) singletonObjects.get("instanceA");
		System.out.println(instanceA.getInstanceB());

	}

	public static Object getBean(String beanName) throws Exception {

		// 查看一级缓存
		Object obj = getSingleton(beanName);
		if (obj != null) {
			return obj;
		}

		RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionMap.get(beanName);


		// 实例化
		Class<?> beanClass = beanDefinition.getBeanClass();
		Object instanceBean = beanClass.getDeclaredConstructor().newInstance();

		// 加入二级缓存（半成品）
		earlySingletonObjects.put(beanName, instanceBean);

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

	private static Object getSingleton(String beanName) {
		if (singletonObjects.containsKey(beanName)) {
			return singletonObjects.get(beanName);
		}else if(earlySingletonObjects.containsKey(beanName)) {
			return earlySingletonObjects.get(beanName);
		}
		return null;
	}


	@Component
	public static class InstanceA {
		@Autowired
		private InstanceB instanceB;

		public InstanceA() {
			System.out.println("InstanceA Constructor");
		}

		public InstanceB getInstanceB() {
			return instanceB;
		}
	}

	@Component
	public static class InstanceB {
		@Autowired
		private InstanceA instanceA;

		public InstanceB() {
			System.out.println("InstanceB Constructor");
		}

		public InstanceA getInstanceA() {
			return instanceA;
		}
	}

}
