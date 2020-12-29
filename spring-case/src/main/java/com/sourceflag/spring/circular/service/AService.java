package com.sourceflag.spring.circular.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AService
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-25 23:16
 * @since 1.0
 */
@Component
public class AService {
	@Autowired
	private BService bService;

	public void test(){

	}

}
