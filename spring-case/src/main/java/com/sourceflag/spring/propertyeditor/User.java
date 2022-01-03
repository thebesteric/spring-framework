package com.sourceflag.spring.propertyeditor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class User {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
