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
public class Demo5 {

	public static Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
	// 一级缓存
	public static Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	// 二级缓存：解决避免读取到不完整的 bean
	public static Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
	// 三级缓存：存函数接口，解决判断循环依赖的问题，同时满足单一职责
	public static Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();
	// 循环依赖标识
	public static Set<String> singletonCurrentlyInCreation = new HashSet<>();

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

		// 标记为正在创建
		if (!singletonCurrentlyInCreation.contains(beanName)) {
			singletonCurrentlyInCreation.add(beanName);
		}

		// --- createBean ---

		// 实例化
		Class<?> beanClass = beanDefinition.getBeanClass();
		Object instanceBean = beanClass.getDeclaredConstructor().newInstance();

		// 创建动态代理（BeanPostProcessor），只有出现循环依赖的时候，才会在实例化之后，提前创建
		// 判断当前是不是循环依赖
		Object finalInstanceBean = instanceBean;
		singletonFactories.put(beanName, () -> {
			return new JdkProxyBeanPostProcessor().getEarlyBeanReference(finalInstanceBean, beanName);
		});


		// 加入二级缓存（半成品）
		// 没有必要放到二级缓存了，因为不需要二级缓存放 AOP 对象了
		// earlySingletonObjects.put(beanName, instanceBean);

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
		// 正常情况下会在初始化之后创建动态代理
		if (earlySingletonObjects.containsKey(beanName)) {
			instanceBean = earlySingletonObjects.get(beanName);

			// 属性赋值
			for (Field declaredField : beanClass.getDeclaredFields()) {
				Autowired annotation = declaredField.getAnnotation(Autowired.class);
				if (annotation != null) {
					declaredField.setAccessible(true);
					String name = declaredField.getName();
					Object fieldObject = getBean(name);
					declaredField.set(instanceBean, fieldObject);
				}
			}


		}

		// 加入一级缓存（单例池）
		singletonObjects.put(beanName, instanceBean);

		// 删除二级缓存和三级缓存
		earlySingletonObjects.remove(beanName);
		singletonFactories.remove(beanName);

		return instanceBean;
	}

	private static Object getSingleton(String beanName) throws Exception {
		// 先从一级缓存中获取
		Object bean = singletonObjects.get(beanName);
		if (bean == null && singletonCurrentlyInCreation.contains(beanName)) {
			// 说明是循环依赖
			bean = earlySingletonObjects.get(beanName);
			// 如果二级缓存没有就从三级缓存中拿
			if (bean == null) {
				ObjectFactory<?> objectFactory = singletonFactories.get(beanName);
				if (objectFactory != null) {
					bean = objectFactory.getObject();
					// 加入二级缓存的意义是，存放已经代理过的对象
					// 比如 A <--> B 并且 A <--> C，这样就不用重复创建代理两次 A 对象了
					earlySingletonObjects.put(beanName, bean);
					singletonFactories.remove(beanName);
				}
				return bean;
			}
		}
		return bean;
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
