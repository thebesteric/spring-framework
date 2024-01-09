package com.sourceflag.spring.mybatis;

import com.sourceflag.spring.mybatis.mapper.OrderMapper;
import com.sourceflag.spring.mybatis.mapper.UserMapper;
import com.sourceflag.spring.mybatis.service.UserService;
import com.sourceflag.spring.mybatis.spring.MyMapperScan;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 22:53:04
 */
public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);

		// 思考：利用 FactoryBean 和注册 BD 的形式，完成 Mapper 的注册
		// 问题：需要硬编码
		// 改进：移植到 BeanDefinitionRegistryPostProcessor，来隐藏实现细节
		// AbstractBeanDefinition beanDefinition1 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// beanDefinition1.setBeanClass(MyFactoryBean.class);
		// beanDefinition1.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
		// ctx.registerBeanDefinition("userMapper", beanDefinition1);
		//
		// AbstractBeanDefinition beanDefinition2 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// beanDefinition2.setBeanClass(MyFactoryBean.class);
		// beanDefinition2.getConstructorArgumentValues().addGenericArgumentValue(OrderMapper.class);
		// ctx.registerBeanDefinition("orderMapper", beanDefinition2);

		ctx.refresh();

		UserMapper userMapper = (UserMapper) ctx.getBean("userMapper");
		userMapper.getById(1);

		OrderMapper orderMapper = (OrderMapper) ctx.getBean("orderMapper");
		orderMapper.getById(1);

		UserService userService = ctx.getBean(UserService.class);
		userService.test();

	}

	@Configuration
	// @Import(MyBeanDefinitionRegistrar.class) // 加入到 @MyMapperScan 的注解上面更加合适，就解耦了
	@ComponentScan("com.sourceflag.spring.mybatis")
	// @MapperScan("com.sourceflag.spring.mybatis")
	@MyMapperScan("com.sourceflag.spring.mybatis.mapper")
	public static class Config {

		// 加上这个 bean，可以不用写 @MapperScan
		// @Bean
		public MapperScannerConfigurer mapperScannerConfigurer(){
			MapperScannerConfigurer configurer = new MapperScannerConfigurer();
			configurer.setBasePackage("com.sourceflag.spring.mybatis.mapper");
			return configurer;
		}

		// 整合 mybatis 的重要 bean
		@Bean
		public SqlSessionFactory sqlSessionFactory() throws Exception {
			// InputStream inputStream = Resources.getResourceAsStream("mybatis.xml");
			// return new SqlSessionFactoryBuilder().build(inputStream);

			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(dataSource());
			return sqlSessionFactoryBean.getObject();
		}

		@Bean
		public DataSource dataSource() {
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/test");
			dataSource.setUsername("root");
			dataSource.setPassword("root");
			return dataSource;
		}

	}
}
