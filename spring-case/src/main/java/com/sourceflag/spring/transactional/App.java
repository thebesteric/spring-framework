package com.sourceflag.spring.transactional;

import com.alibaba.druid.pool.DruidDataSource;
import com.sourceflag.spring.transactional.service.UserService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

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
	@ComponentScan({"com.sourceflag.spring.transactional.service"})
	@MapperScan("com.sourceflag.spring.transactional.mapper")
	// @EnableAspectJAutoProxy(proxyTargetClass = true)
	@EnableAspectJAutoProxy(exposeProxy = true)
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
			dataSourceTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);

			// 部分失败时是否全局回滚，如果设置 false 的话，则不会
			// 如果有 tryCatch 则还是会全部提交，否则全部回滚
			// dataSourceTransactionManager.setGlobalRollbackOnParticipationFailure(true);

			return dataSourceTransactionManager;
		}


	}

}
