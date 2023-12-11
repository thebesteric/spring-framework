package com.sourceflag.spring.circular.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo
 * 增加：三级缓存，代码解耦，符合单一责任原则
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-09 23:57
 * @since 1.0
 */
public class Demo6 {

	public static Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
	// 一级缓存
	public static final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	// 二级缓存：解决避免读取到不完整的 bean
	public static final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
	// 三级缓存：存函数接口，解决判断循环依赖的问题，同时满足单一职责
	public static final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();
	// 循环依赖标识
	public static final Set<String> singletonCurrentlyInCreation = new HashSet<>();

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

		InstanceA instanceA = (InstanceA) singletonObjects.get("instanceA");
		System.out.println(instanceA.getInstanceB());

	}

	public static Object getBean(String beanName) throws Exception {

		// 查看一级缓存
		Object obj = getSingleton(beanName);
		if (obj != null) {
			return obj;
		}

		// 获取 beanDefinition
		RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionMap.get(beanName);

		// --- createBean ---
		Object instanceBean = getSingleton(beanName, () -> {
			return createBean(beanName, beanDefinition);
		});

		return instanceBean;
	}

	private static Object getSingleton(String beanName) throws Exception {
		// 先从一级缓存中获取
		Object bean = singletonObjects.get(beanName);
		if (bean == null && singletonCurrentlyInCreation.contains(beanName)) {
			synchronized (singletonObjects) {
				// 说明是循环依赖
				bean = earlySingletonObjects.get(beanName);
				// 如果二级缓存没有就从三级缓存中拿
				if (bean == null) {
					ObjectFactory<?> objectFactory = singletonFactories.get(beanName);
					if (objectFactory != null) {
						bean = objectFactory.getObject();
					}
					return bean;
				}
			}

		}
		return bean;
	}

	private static Object getSingleton(String beanName, ObjectFactory<?> objectFactory) throws Exception {
		synchronized (singletonObjects) {
			Object object = singletonObjects.get(beanName);
			if (object == null) {
				// 标记为正在创建
				singletonCurrentlyInCreation.add(beanName);

				// 调用 lambda
				object = objectFactory.getObject();

				// 创建完成
				singletonCurrentlyInCreation.remove(beanName);

				// 加入单例池
				singletonObjects.put(beanName, object);

				// 清除二、三级缓存
				singletonFactories.remove(beanName);
				earlySingletonObjects.remove(beanName);
			}
			return object;
		}
	}

	private static Object createBean(String beanName, RootBeanDefinition beanDefinition) throws Exception {

		// 实例化
		Class<?> beanClass = beanDefinition.getBeanClass();
		Object instanceBean = beanClass.getDeclaredConstructor().newInstance();

		singletonFactories.put(beanName, () -> {
			return new JdkProxyBeanPostProcessor().getEarlyBeanReference(instanceBean, beanName);
		});

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

		// 加入一级缓存（单例池）
		singletonObjects.put(beanName, instanceBean);

		// 删除二级缓存和三级缓存
		earlySingletonObjects.remove(beanName);
		singletonFactories.remove(beanName);

		return instanceBean;
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
