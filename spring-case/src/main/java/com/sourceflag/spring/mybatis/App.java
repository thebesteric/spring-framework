package com.sourceflag.spring.mybatis;

import com.sourceflag.spring.mybatis.anno.MyMapperScan;
import com.sourceflag.spring.mybatis.service.UserService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

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
	// @MapperScan("com.sourceflag.spring.mybatis")
	@MyMapperScan("com.sourceflag.spring.mybatis")
	public static class Config {

		// 加上这个 bean，可以不用写 @MapperScan
		// @Bean
		public MapperScannerConfigurer mapperScannerConfigurer(){
			MapperScannerConfigurer configurer = new MapperScannerConfigurer();
			configurer.setBasePackage("com.sourceflag.spring.mybatis");
			return configurer;
		}

		// 整合 mybatis 的重要 bean
		// @Bean
		public SqlSessionFactory sqlSessionFactory() throws IOException {
			InputStream inputStream = Resources.getResourceAsStream("mybatis.xml");
			return new SqlSessionFactoryBuilder().build(inputStream);
		}

	}

}
