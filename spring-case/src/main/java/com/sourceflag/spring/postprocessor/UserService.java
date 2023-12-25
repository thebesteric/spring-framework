package com.sourceflag.spring.postprocessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 23:03:51
 */
@Component
public class UserService {

	@MyInject("this is my inject field")
	private String value;

	@Autowired
	private OrderService orderService;

	public void test() {
		System.out.println("orderService 是通过 MergedBeanDefinitionPostProcessor 注入的 = " + orderService);
		System.out.println("value = @MyInject " +  value);
	}

	public void call() {
		System.out.println("call");
	}

	public void myInitMethod() {
		System.out.println("myInitMethod is running...");
	}

	public OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
}
