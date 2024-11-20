package com.sourceflag.spring.qualifier;

import com.sourceflag.spring.qualifier.loadbalance.LoadBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-20 14:39:11
 */
@Component
public class UserService {

	@Autowired
	// @Random
	@Sequence
	private LoadBalance loadBalance;

	public Integer next() {
		return loadBalance.select();
	}

}
