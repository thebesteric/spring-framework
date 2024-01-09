package com.sourceflag.spring.mybatis.service;

import com.sourceflag.spring.mybatis.mapper.OrderMapper;
import com.sourceflag.spring.mybatis.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 22:54:11
 */
@Component
public class UserService {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private OrderMapper orderMapper;

	public void test() {
		System.out.println(userMapper.getById(1));
		System.out.println(orderMapper.getById(1));
	}
}
