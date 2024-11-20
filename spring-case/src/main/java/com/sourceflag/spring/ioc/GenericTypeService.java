package com.sourceflag.spring.ioc;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * GenericTypeService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-19 20:47:24
 */
@Getter
public class GenericTypeService<A, B> {

	@Autowired
	protected A a;

	@Autowired
	protected B b;

}
