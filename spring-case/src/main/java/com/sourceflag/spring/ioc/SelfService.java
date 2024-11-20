package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SelfService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-19 17:42:17
 */
@Component
public class SelfService implements BeanNameAware {

	public String beanName;

	@Autowired
	private SelfService selfService;

	public void print() {
		System.out.println(selfService);
	}


	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public String toString() {
		return "SelfService{" +
			   "beanName='" + beanName + '\'' +
			   '}';
	}
}
