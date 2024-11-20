package com.sourceflag.spring.autowired_mode;

/**
 * OrderService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-14 11:39:46
 */
public class OrderService {

	private String autowiredBy;

	public OrderService autowiredBy() {
		if (autowiredBy == null) {
			System.out.println("Spring 给 userService 添加 orderService");
		} else {
			System.out.println(autowiredBy);
		}
		return this;
	}

	public OrderService setAutowiredBy(String autowiredBy) {
		this.autowiredBy = autowiredBy;
		return this;
	}
}
