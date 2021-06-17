package com.sourceflag.spring.circular.demo;

@FunctionalInterface
public interface ObjectFactory<T> {
	T getObject() throws Exception;
}
