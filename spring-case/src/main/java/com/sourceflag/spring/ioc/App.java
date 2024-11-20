package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-18 10:01:53
 */

public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

		UserService userService1 = ctx.getBean(UserService.class);
		userService1.printOrderService("userService1");

		UserService userService2 = ctx.getBean(UserService.class);
		userService2.printOrderService("userService2");
		userService1.printOrderService("userService1");

		userService2.printMyName();


		TestService testService = ctx.getBean(TestService.class);
		testService.test();

		SelfService selfService = (SelfService) ctx.getBean("selfService");
		selfService.print(); // SelfService{beanName='selfService1'}

		SelfService selfService1 = (SelfService) ctx.getBean("selfService1");
		selfService1.print(); // SelfService{beanName='selfService'}

		GenericTypeServiceImpl genericTypeService = (GenericTypeServiceImpl) ctx.getBean("genericTypeServiceImpl");
		genericTypeService.test();

		QualifierService qualifierService = ctx.getBean(QualifierService.class);
		qualifierService.test();
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.ioc")
	public static class AppConfig {

		@Bean
		public String myName() {
			return "eric";
		}

		@Primary
		@Bean
		public MultiService multiService1() {
			return new MultiService();
		}

		@Bean
		public MultiService multiService2() {
			return new MultiService();
		}

		@Bean(autowireCandidate = true)
		public SelfService selfService1() {
			return new SelfService();
		}

		@Bean
		@Qualifier("address-1")
		public String address1() {
			return "address-1";
		}

		@Bean
		@Qualifier("address-2")
		public String address2() {
			return "address-2";
		}

	}
}
