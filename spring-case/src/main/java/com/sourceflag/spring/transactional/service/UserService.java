package com.sourceflag.spring.transactional.service;

import com.sourceflag.spring.transactional.mapper.UserMapper;
import com.sourceflag.spring.transactional.model.User;
import org.springframework.aop.framework.AopContext;
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
// @Transactional
public class UserService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	@Transactional(rollbackFor = Exception.class)
	public void test() {
		// userMapper.insert1(new User("test"));
		// userService.test1();
		System.out.println("completed");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void test1() {
		userMapper.insert1(new User("test1"));

		// 获取当前事务的名字：com.sourceflag.spring.transactional.service.UserService.test1
		System.out.println(TransactionSynchronizationManager.getCurrentTransactionName());

		// 可以通过这个 api 实现监听事务执行过程
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void suspend() {
				System.out.println("被挂起了");
			}

			@Override
			public void beforeCommit(boolean readOnly) {
				System.out.println("提交前...");
			}
		});

		// // 直接调用方法是不会触发事物的，比如使用 userService.xxx();
		// // test3();
		// userService.test3();
		//
		// try {
		// 	throw new RuntimeException("发生后续业务异常");
		// } catch (Exception ex) {
		// 	// 由于异常被抓了，所以需要设置回滚
		// 	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		// }
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void test2() {
		userMapper.insert2(new User("李四1"));
		userMapper.insert2(new User("李四2"));
		// userService.test3(); // 需要使用 CGLIB 动态代理
		((UserService)AopContext.currentProxy()).test3(); // 需要暴露代理
		throw new RuntimeException("some error in it");
	}

	// 挂起外部事务，开启一个新的事务
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void test3() {
		userMapper.insert2(new User("李四3"));
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
