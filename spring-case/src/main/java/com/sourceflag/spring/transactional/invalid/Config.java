package com.sourceflag.spring.transactional.invalid;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.sourceflag.spring.transactional.invalid")
@EnableTransactionManagement
public class Config {

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		// return new JdbcTemplate(dataSource());
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public DataSource dataSource() {
		// DruidDataSource dataSource = new DruidDataSource();
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&userSSL=false&serverTimezone=Asia/Shanghai");
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		return dataSource;
	}

	// 事务管理器
	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		// transactionManager.setDataSource(dataSource());
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

}