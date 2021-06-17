package com.sourceflag.spring.circular.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InstanceA {
	@Autowired
	private InstanceB instanceB;

	public InstanceA() {
		System.out.println("InstanceA Constructor");
	}

	public InstanceB getInstanceB() {
		return instanceB;
	}

}