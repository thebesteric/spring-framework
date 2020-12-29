package com.sourceflag.spring.circular.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BService
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-25 23:16
 * @since 1.0
 */
@Component
public class BService {
	@Autowired
	private AService aService;
}
