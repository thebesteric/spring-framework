package com.sourceflag.spring.simulate.framework;

public interface InitializingBean {

	// 初始化：属性设置后
	void afterPropertiesSet();
}
