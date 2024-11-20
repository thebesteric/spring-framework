package com.sourceflag.spring.qualifier.loadbalance;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * RandomLoadBalance
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-20 14:19:40
 */
@Component
@com.sourceflag.spring.qualifier.Random
public class RandomLoadBalance implements LoadBalance {

	private static final Random RANDOM = new Random();

	@Override
	public Integer select() {
		return RANDOM.nextInt(5);
	}
}
