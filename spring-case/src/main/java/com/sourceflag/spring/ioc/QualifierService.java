package com.sourceflag.spring.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * QualiferService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-20 11:29:01
 */
@Component
public class QualifierService {

	@Autowired
	@Qualifier("address-1")
	private String address2;

	public void test() {
		System.out.println(address2);
	}


}
