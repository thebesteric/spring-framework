package com.sourceflag.spring.circular.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InstanceB {
	@Autowired
	private InstanceA instanceA;

	public InstanceB() {
		System.out.println("InstanceB Constructor");
	}

	public InstanceA getInstanceA() {
		return instanceA;
	}
}