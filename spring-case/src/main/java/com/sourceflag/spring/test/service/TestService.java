package com.sourceflag.spring.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TestService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-25 14:51:59
 */
@Component
public class TestService<T> {

	@Autowired
	private List<T> list;

	public void test() {
		System.out.println(list);
	}

}
