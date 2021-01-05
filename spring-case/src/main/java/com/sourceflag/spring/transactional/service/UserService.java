package com.sourceflag.spring.transactional.service;

import com.sourceflag.spring.transactional.mapper.UserMapper;
import com.sourceflag.spring.transactional.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * UserService
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-02 22:54
 * @since 1.0
 */
@Component
public class UserService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	@Transactional
	public void test() {
		userMapper.insert1(new User("张三"));
	}

	@Transactional
	public void test1() {
		userMapper.insert1(new User("张三"));

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void suspend() {
				System.out.println("被挂起了");
			}
		});

		userService.test3();
	}

	@Transactional
	public void test2() {
		userMapper.insert2(new User("李四"));
		userService.test3();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void test3() {
		userMapper.insert2(new User("李四"));
	}

	@Transactional(propagation = Propagation.NESTED)
	public void test4() {
		userMapper.insert2(new User("李四"));
	}

	@Transactional(propagation = Propagation.NEVER)
	public void test5() {
		userMapper.insert2(new User("李四"));
	}

}
