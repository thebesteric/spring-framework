package com.sourceflag.spring.imported;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(MyImportSelector.class)
// @ComponentScan("com.sourceflag.spring.imported")
@Configuration
public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(App.class);
		context.refresh();

		// @Import 里导入的 bean 的名字，是类的全限定名
		UserService userService = (UserService) context.getBean("com.sourceflag.spring.imported.UserService");
		userService.test();
	}

}
