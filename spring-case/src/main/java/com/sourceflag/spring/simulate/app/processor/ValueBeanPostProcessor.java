package com.sourceflag.spring.simulate.app.processor;

import com.sourceflag.spring.simulate.framework.BeanPostProcessor;
import com.sourceflag.spring.simulate.framework.anno.Component;
import com.sourceflag.spring.simulate.framework.anno.Value;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

@Component
public class ValueBeanPostProcessor implements BeanPostProcessor {

	private Properties properties;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {

		if (properties == null) {
			properties = loadProps();
		}

		for (Field field : bean.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Value.class)) {
				Value valueAnnotation = field.getAnnotation(Value.class);
				field.setAccessible(true);
				Class<?> type = field.getType();
				String value = properties.getProperty(valueAnnotation.value());
				if (type.getCanonicalName().equals("int")) {
					field.setInt(bean, Integer.parseInt(value));
				} else {
					field.set(bean, value);
				}
			}
		}

		return bean;
	}

	private Properties loadProps() {
		try (InputStream in = ValueBeanPostProcessor.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties = new Properties();
			properties.load(in);
			return properties;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
