package com.sourceflag.spring.mybatis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 22:59
 * @since 1.0
 */
@Component
public class UserService {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private OrderMapper orderMapper;

	public void getById(int id){
		userMapper.getById(id);
		orderMapper.getById(id);
	}

}
