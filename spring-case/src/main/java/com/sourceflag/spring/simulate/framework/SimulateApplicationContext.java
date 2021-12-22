package com.sourceflag.spring.simulate.framework;

import com.sourceflag.spring.simulate.framework.anno.Autowired;
import com.sourceflag.spring.simulate.framework.anno.Component;
import com.sourceflag.spring.simulate.framework.anno.ComponentScan;
import com.sourceflag.spring.simulate.framework.anno.Scope;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SimulateApplicationContext {

	private Class<?> configClass;

	private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
	private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	private List<BeanPostProcessor> beanPostProcessors = new LinkedList<>();

	public SimulateApplicationContext(Class<?> configClass) {
		this.configClass = configClass;
		// 扫描
		scan(configClass);

		for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
			String beanName = entry.getKey();
			BeanDefinition beanDefinition = entry.getValue();
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
		} else {
			bean = createBean(beanName, beanDefinition);
		}
		return bean;
	}

	private Object createBean(String beanName, BeanDefinition beanDefinition) {
		Object instance = null;
		try {
			Class<?> clazz = beanDefinition.getType();
			instance = clazz.getConstructor().newInstance();

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

	private void scan(Class<?> configClass) {
		if (configClass.isAnnotationPresent(ComponentScan.class)) {
			ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
			// com.sourceflag.spring.simulate.app
			String path = componentScanAnnotation.value();
			// com/sourceflag/spring/simulate/app
			path = path.replace(".", "/");

			ClassLoader classLoader = SimulateApplicationContext.class.getClassLoader();
			URL resource = classLoader.getResource(path);
			assert resource != null;
			File file = new File(resource.getFile());

			doScan(path, classLoader, file);

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

							Component componentAnnotation = clazz.getAnnotation(Component.class);

							BeanDefinition beanDefinition = new BeanDefinition();
							beanDefinition.setType(clazz);

							Scope scopeAnnotation = null;
							if (clazz.isAnnotationPresent(Scope.class)) {
								scopeAnnotation = clazz.getAnnotation(Scope.class);
							}
							beanDefinition.setScope(scopeAnnotation != null ? scopeAnnotation.value() : "singleton");

							String beanName = generateBeanName(clazz, componentAnnotation);
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
		if (beanName.equals("")) {
			beanName = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
		}
		return beanName;
	}

}
