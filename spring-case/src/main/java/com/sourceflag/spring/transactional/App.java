package com.sourceflag.spring.transactional;

import com.alibaba.druid.pool.DruidDataSource;
import com.sourceflag.spring.transactional.service.UserService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * App
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-02 22:19
 * @since 1.0
 */
public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();


		UserService userService = ctx.getBean(UserService.class);
		userService.test();
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.transactional")
	@MapperScan("com.sourceflag.spring.transactional.mapper")
	@EnableTransactionManagement // 开启事务
	public static class Config {

		@Bean
		public DataSource dataSource() {
			// DriverManagerDataSource dataSource = new DriverManagerDataSource();
			DruidDataSource dataSource = new DruidDataSource();
			dataSource.setUsername("root");
			dataSource.setPassword("root");
			dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&userSSL=false&serverTimezone=Asia/Shanghai");
			dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			return dataSource;
		}

		@Bean
		public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(dataSource);
			try {
				return sqlSessionFactoryBean.getObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		// 事务管理器
		@Bean
		public PlatformTransactionManager transactionManager(DataSource dataSource) {
			DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
			dataSourceTransactionManager.setDataSource(dataSource);
			// 设置要不要把事务信息同步到线程中
			// dataSourceTransactionManager.setTransactionSynchronization(1);
			return dataSourceTransactionManager;
		}


	}

}
