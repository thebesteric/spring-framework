package com.sourceflag.spring.circular;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * MyAspect
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-26 23:10
 * @since 1.0
 */
@Aspect
@Component
public class MyAspect {

	@Before("execution(public void com.sourceflag.spring.circular.service.AService.test())")
	public void before() {

	}

}
