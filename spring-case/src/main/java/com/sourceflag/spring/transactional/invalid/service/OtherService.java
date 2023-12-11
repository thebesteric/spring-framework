package com.sourceflag.spring.transactional.invalid.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserServiceBase
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-11 15:59:04
 */
@Component
public class OtherService {


	@Transactional(propagation = Propagation.NEVER)
	public void other() {
		System.out.println("a executed");
	}

}
