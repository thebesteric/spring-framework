package com.sourceflag.spring.ioc;

import lombok.Getter;
import org.springframework.beans.factory.BeanNameAware;

/**
 * MultiService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-19 11:55:09
 */
@Getter
public class MultiService implements BeanNameAware {

	// 测试 @Primary 注解
	private String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MultiService{" +
			   "name='" + name + '\'' +
			   '}';
	}
}
