package com.sourceflag.spring.simulate.app.processor;

import com.sourceflag.spring.simulate.framework.BeanPostProcessor;
import com.sourceflag.spring.simulate.framework.anno.Component;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Component
public class SimulateBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
		if (beanName.equals("userService")) {
			System.out.println(beanName + "======初始化后=====");
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(bean.getClass());
			enhancer.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
					System.out.println("前置通知 userService");
					Object invoke = method.invoke(bean, objects);
					System.out.println("后置通知 userService");
					return invoke;
				}
			});
			return enhancer.create();
		}

		return bean;
	}
}
