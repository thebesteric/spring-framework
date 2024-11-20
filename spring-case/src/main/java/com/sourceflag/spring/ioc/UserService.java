package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-18 10:03:35
 */
@Component
@Scope("prototype")
public class UserService implements InterfaceService<OrderService> {

	// 【静态属性测试】：不会注入
	@Autowired
	private static OrderService orderService;

	// 【代理对象】由于使用了 @Lazy 注解，此时这个对象就会是一个代理对象
	// 当使用该对象的时候，会调用代理对象的 getTarget 方法，返回被代理的对象
	@Autowired
	@Lazy
	private OrderService proxyOrderService;

	@Autowired
	private String myName;

	@Autowired
	private TestService testService;

	public void printOrderService(String name) {
		System.out.println(name + "：" + orderService);
	}

	public void useProxyOrderService() {
		System.out.println("proxyOrderService = " + proxyOrderService);
	}

	public void printMyName() {
		System.out.println("myName = " + myName);
	}

	@Autowired
	public void xxx(TestService testService, OrderService orderService) {
		System.out.println("xxx testService = " + testService);
		System.out.println("xxx orderService = " + orderService);
	}

	// 测试利用【pvs】设置属性值，Spring 就不会去单例池去寻找，直接使用程序员创建的对象
	// @Autowired
	public void setTestService(TestService testService) {
		this.testService = testService;
		System.out.println("testService injectType = " + testService.getInjectType());
	}

	// 【桥接测试】
	// 这是一个桥接方法，字节码会有两个同名方法，一个入参是 Object，一个是 OrderService
	// 桥接方法：某个类实现类一个接口，接口中有一个含有范型参数的方法，然后实现类实现类改方法，就会出现桥接方法
	// public synthetic bridge setService(Ljava/lang/Object;)V
	// public setService(Lcom/sourceflag/spring/ioc/OrderService;)V
	// Spring 会忽略有 synthetic bridge 修饰的方法
	@Autowired
	@Override
	public void setService(OrderService service) {
		System.out.println("InterfaceService = " + service);
	}
}
