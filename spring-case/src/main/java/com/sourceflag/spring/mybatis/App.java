package com.sourceflag.spring.mybatis;

import com.sourceflag.spring.mybatis.factory.MyBeanDefinitionRegistrar;
import com.sourceflag.spring.mybatis.service.OrderMapper;
import com.sourceflag.spring.mybatis.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.*;

/**
 * App
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-25 22:48
 * @since 1.0
 */

public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();

		UserService userService = ctx.getBean(UserService.class);
		userService.getById(1);
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.mybatis")
	@Import(MyBeanDefinitionRegistrar.class)
	@MapperScan("com.sourceflag.spring.mybatis")
	public static class Config {

		@Bean
		public MapperScannerConfigurer mapperScannerConfigurer(){
			MapperScannerConfigurer configurer = new MapperScannerConfigurer();
			configurer.setBasePackage("com.sourceflag.spring.mybatis");
			return configurer;
		}

	}

}
