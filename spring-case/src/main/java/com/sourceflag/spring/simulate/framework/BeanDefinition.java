package com.sourceflag.spring.simulate.framework;

import lombok.Data;

@Data
public class BeanDefinition {
	private Class<?> type;
	private String scope;
	private boolean lazy;

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}
}
