package com.sourceflag.spring.mybatis.anno;

import com.sourceflag.spring.mybatis.factory.MyBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MyBeanDefinitionRegistrar.class)
public @interface MyMapperScan {
	String value();
}
