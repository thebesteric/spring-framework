package com.sourceflag.spring.autowired_mode;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-14 11:39:37
 */
public class UserService {

	// @Autowired
	private OrderService orderService;

	public void test() {
		System.out.println(orderService.autowiredBy());
	}

	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
}
