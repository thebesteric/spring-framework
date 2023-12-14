package com.sourceflag.spring.simulate.framework;

import com.sourceflag.spring.simulate.framework.anno.Autowired;
import com.sourceflag.spring.simulate.framework.anno.Component;
import com.sourceflag.spring.simulate.framework.anno.ComponentScan;
import com.sourceflag.spring.simulate.framework.anno.Scope;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SimulateAnnotationConfigApplicationContext {

	// 配置类
	private Class<?> configClass;

	private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
	private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	private List<BeanPostProcessor> beanPostProcessors = new LinkedList<>();

	public SimulateAnnotationConfigApplicationContext(Class<?> configClass) {
		this.configClass = configClass;

		// 扫描
		scan(configClass);

		for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
			String beanName = entry.getKey();
			BeanDefinition beanDefinition = entry.getValue();
			// 获取所有单例 bean
			if ("singleton".equals(beanDefinition.getScope())) {
				// 创建 bean
				Object bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
		}
	}

	public Object getBean(String beanName) {
		Object bean;
		if (!beanDefinitionMap.containsKey(beanName)) {
			throw new RuntimeException("no such bean exception: " + beanName);
		}
		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		if ("singleton".equals(beanDefinition.getScope())) {
			bean = singletonObjects.get(beanName);
			if (bean == null) {
				bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
		} else {
			bean = createBean(beanName, beanDefinition);
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clazz) {
		String beanName = Introspector.decapitalize(clazz.getSimpleName());
		if (clazz.isAnnotationPresent(Component.class)) {
			Component annotation = clazz.getAnnotation(Component.class);
			beanName = generateBeanName(clazz, annotation);
		}
		return (T) getBean(beanName);
	}

	private Object createBean(String beanName, BeanDefinition beanDefinition) {
		Object instance = null;
		try {
			Class<?> clazz = beanDefinition.getType();
			// instance = clazz.getConstructor().newInstance();
			instance = determineConstructor(clazz);

			// 处理 @Autowired
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Autowired.class)) {
					field.setAccessible(true);
					String fieldName = field.getName();
					field.set(instance, getBean(fieldName));
				}
			}

			// aware
			if (instance instanceof BeanNameAware) {
				((BeanNameAware) instance).setBeanName(beanName);
			}

			// 初始化前
			for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
				instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
			}

			// 初始化
			if (instance instanceof InitializingBean) {
				((InitializingBean) instance).afterPropertiesSet();
			}

			// 初始化后
			for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
				instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	private Object determineConstructor(Class<?> clazz) throws Exception {

		// 过滤掉私有的构造函数，并且按参数数量升序
		List<Constructor<?>> sortedConstructors = Arrays.stream(clazz.getConstructors())
				.filter(c -> Modifier.isPublic(c.getModifiers()))
				.sorted(Comparator.comparingInt(Constructor::getParameterCount)).collect(Collectors.toList());

		for (Constructor<?> constructor : sortedConstructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 0) {
				return constructor.newInstance();
			}
			Object[] parameters = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				try {
					parameters[i] = getBean(parameterTypes[i]);
				} catch (Exception e) {
					break;
				}
			}
			return constructor.newInstance(parameters);
		}
		return null;
	}

	private void scan(Class<?> configClass) {
		if (configClass.isAnnotationPresent(ComponentScan.class)) {
			ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
			// com.sourceflag.spring.simulate.app
			String path = componentScanAnnotation.value();
			// com/sourceflag/spring/simulate/app
			path = path.replace(".", "/");

			ClassLoader appClassLoader = SimulateAnnotationConfigApplicationContext.class.getClassLoader();
			URL resource = appClassLoader.getResource(path);
			assert resource != null;
			File file = new File(resource.getFile());

			doScan(path, appClassLoader, file);

		}
	}

	private void doScan(String path, ClassLoader classLoader, File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {

				if (f.isDirectory()) {
					String newPath = f.getAbsolutePath().substring(f.getAbsolutePath().indexOf(path));
					doScan(newPath, classLoader, f);
				} else {
					// /Users/keisun/IdeaProjects/research/source/spring/spring-framework-5.3.2/spring-case/build/classes/java/main/com/sourceflag/spring/simulate/app/service/UserService.class
					String absolutePath = f.getAbsolutePath();
					int start = absolutePath.indexOf(path);
					int end = absolutePath.indexOf(".class");
					absolutePath = absolutePath.substring(start, end);
					String className = absolutePath.replace("/", ".");
					try {
						Class<?> clazz = classLoader.loadClass(className);
						if (clazz.isAnnotationPresent(Component.class)) {

							// clazz 是否实现了 BeanPostProcessor
							if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
								BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getConstructor().newInstance();
								beanPostProcessors.add(beanPostProcessor);
								continue;
							}

							// 解析 @Component 注解
							Component componentAnnotation = clazz.getAnnotation(Component.class);

							// 解析 @Scope 注解
							Scope scopeAnnotation = null;
							if (clazz.isAnnotationPresent(Scope.class)) {
								scopeAnnotation = clazz.getAnnotation(Scope.class);
							}

							// 创建 BD 来保存元信息
							BeanDefinition beanDefinition = new BeanDefinition();
							beanDefinition.setType(clazz);
							beanDefinition.setScope(scopeAnnotation != null ? scopeAnnotation.value() : "singleton");

							// 生成 beanName
							String beanName = generateBeanName(clazz, componentAnnotation);

							// 加入到 beanDefinitionMap 集合中
							beanDefinitionMap.put(beanName, beanDefinition);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private String generateBeanName(Class<?> clazz, Component componentAnnotation) {
		String beanName = componentAnnotation.value();
		if (beanName.isEmpty()) {
			// beanName = toLowercaseFirstLetter(clazz.getSimpleName());
			beanName = Introspector.decapitalize(clazz.getSimpleName());
		}
		return beanName;
	}

	private String toLowercaseFirstLetter(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		char firstChar = Character.toLowerCase(input.charAt(0));
		return firstChar + input.substring(1);
	}

}
