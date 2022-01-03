package com.sourceflag.spring.event;

import com.sourceflag.spring.event.listener.LoginEvent;
import com.sourceflag.spring.event.listener.LoginListener;
import com.sourceflag.spring.event.listener.LoginUser;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
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
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
		ac.register(AppConfig.class);
		ac.refresh();

		ac.addApplicationListener(new LoginListener());

		// SimpleApplicationEventMulticaster multicaster = ac.getBean(SimpleApplicationEventMulticaster.class);
		// multicaster.setTaskExecutor(ac.getBean(ThreadPoolTaskExecutor.class));

		// ApplicationEventPublisher publisher = ac.getBean(ApplicationEventPublisher.class);

		LoginUser loginUser = new LoginUser(1, "eric");

		LoginEvent loginEvent = new LoginEvent(loginUser, System.currentTimeMillis());
		ac.publishEvent(loginEvent);
		// publisher.publishEvent(loginEvent);
		System.out.println(Thread.currentThread().getName() + " publish OVER");

		// 发布 ContextStartedEvent
		ac.start();
		// 发布 ContextStoppedEvent
		ac.stop();
		// 发布 ContextClosedEvent
		ac.close();
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
