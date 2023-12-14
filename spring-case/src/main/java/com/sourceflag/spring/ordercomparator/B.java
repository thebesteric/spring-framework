package com.sourceflag.spring.ordercomparator;

import org.springframework.core.Ordered;

/**
 * A
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 19:01:41
 */
public class B implements Ordered {
	@Override
	public int getOrder() {
		return 2;
	}

	@Override
	public String toString() {
		return 2 + ": " + this.getClass().getSimpleName();
	}
}
