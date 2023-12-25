package com.sourceflag.spring.parentbeanfactory;

import com.sourceflag.spring.parentbeanfactory.child.InChild;
import com.sourceflag.spring.parentbeanfactory.parent.InParent;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-15 19:05:36
 */
public class App {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext(ParentConfig.class);
		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext(ChildConfig.class);
		child.setParent(parent);

		System.out.println("父容器 = " + parent.getBean(InParent.class));
		try {
			System.out.println("父容器 = " + parent.getBean(InChild.class));
		} catch (BeansException e) {
			System.out.println("父容器 = 没有 InChild");
		}

		try {
			System.out.println("子容器 = " + child.getBean(InParent.class));
		} catch (BeansException e) {
			System.out.println("子容器 = 没有 InParent");
		}
		System.out.println("子容器 = " + child.getBean(InChild.class));
	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.parentbeanfactory.parent")
	public static class ParentConfig {

	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.parentbeanfactory.child")
	public static class ChildConfig {

	}
}
