package com.sourceflag.spring.transactional.invalid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private OtherService otherService;

	@Autowired
	private UserService userService;

	@Transactional
	public void test1() {
		jdbcTemplate.execute("insert into test values (1, 1, 1)");
		throw new NullPointerException("异常了");
	}

	@Transactional
	public void test2() {
		jdbcTemplate.execute("insert into test values (1, 1, 1)");
		// 是由普通对象调用的 other 方法
		other();
	}

	@Transactional
	public void test3() {
		jdbcTemplate.execute("insert into test values (1, 1, 1)");
		// 是由代理对象调用的 other 方法
		otherService.other();
	}

	@Transactional
	public void test4() {
		jdbcTemplate.execute("insert into test values (1, 1, 1)");
		// 是由代理对象调用的 other 方法
		userService.other();
	}

	@Transactional(propagation = Propagation.NEVER)
	public void other() {
		System.out.println("other method executed");
	}

}