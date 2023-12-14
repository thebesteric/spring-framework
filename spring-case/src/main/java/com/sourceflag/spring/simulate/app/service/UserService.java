package com.sourceflag.spring.simulate.app.service;

import com.sourceflag.spring.simulate.framework.BeanNameAware;
import com.sourceflag.spring.simulate.framework.InitializingBean;
import com.sourceflag.spring.simulate.framework.anno.Autowired;
import com.sourceflag.spring.simulate.framework.anno.Component;
import com.sourceflag.spring.simulate.framework.anno.Scope;
import com.sourceflag.spring.simulate.framework.anno.Value;

@Component
@Scope("singleton")
// @Scope("prototype")
public class UserService implements InitializingBean, BeanNameAware {

	@Autowired
	private OrderService orderService;

	private OtherService otherService;

	@Value("server.port")
	private int port;

	private String beanName;

	public UserService() {
	}

	public UserService(OtherService otherService) {
		this.otherService = otherService;
	}

	public void test() {
		System.out.println("orderService = " + orderService);
		System.out.println("otherService = " + otherService);
		System.out.println("port = " + port);
		System.out.println("beanName = " + beanName);
	}

	@Override
	public void afterPropertiesSet() {
		System.out.println("=====初始化=====");
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
}
