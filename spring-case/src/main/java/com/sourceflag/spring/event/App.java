package com.sourceflag.spring.event;

import com.sourceflag.spring.event.listener.LoginEvent;
import com.sourceflag.spring.event.listener.LoginUser;
import com.sourceflag.spring.event.listener.LogoutEvent;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * TODO
 *
 * @author Eric Joe
 * @version Ver 1.0
 * @build 2020-05-14 23:41
 */
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		// context.registerShutdownHook();

		// context.addApplicationListener(new LoginListener());

		// SimpleApplicationEventMulticaster multicaster = context.getBean(SimpleApplicationEventMulticaster.class);
		// multicaster.setTaskExecutor(context.getBean(ThreadPoolTaskExecutor.class));

		// ApplicationEventPublisher publisher = context.getBean(ApplicationEventPublisher.class);

		LoginUser loginUser = new LoginUser(1, "eric");

		LoginEvent loginEvent = new LoginEvent(loginUser, System.currentTimeMillis());
		context.publishEvent(loginEvent); // 底层调用的就是 ApplicationEventPublisher 来发布
		// publisher.publishEvent(loginEvent);
		System.out.println(Thread.currentThread().getName() + " publish loginEvent OVER");


		LogoutEvent logoutEvent = new LogoutEvent(loginUser, System.currentTimeMillis());
		context.publishEvent(logoutEvent);
		System.out.println(Thread.currentThread().getName() + " publish logoutEvent OVER");

		// 自动发布 ContextStartedEvent 事件
		context.start();
		// 自动发布 ContextStoppedEvent 事件
		context.stop();
		// 自动发布 ContextClosedEvent 事件
		context.close();
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.event")
	@EnableAsync
	public static class AppConfig {

		// beanName 必须是 applicationEventMulticaster，才会被 Spring 所接收
		@Bean("applicationEventMulticaster")
		public ApplicationEventMulticaster applicationEventMulticaster(BeanFactory beanFactory, ThreadPoolTaskExecutor executor) {
			SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			// 设置异步
			applicationEventMulticaster.setTaskExecutor(executor);
			// applicationEventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
			return applicationEventMulticaster;
		}

		@Bean
		public ThreadPoolTaskExecutor executor() {
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			// 核心线程数
			taskExecutor.setCorePoolSize(5);
			// 最大线程数
			taskExecutor.setMaxPoolSize(10);
			// 队列大小
			taskExecutor.setQueueCapacity(100);
			// 线程池中的线程名称前缀
			taskExecutor.setThreadNamePrefix("async-task-");
			// 当 pool 已经达到 max size 的时候，如何处理新任务
			// CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
			// 初始化线程池
			taskExecutor.initialize();
			return taskExecutor;
		}

	}
}
