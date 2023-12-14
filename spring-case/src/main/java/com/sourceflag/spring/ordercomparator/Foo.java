package com.sourceflag.spring.ordercomparator;

import org.springframework.core.annotation.Order;

/**
 * Foo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 19:21:28
 */
@Order(1)
public class Foo {
	@Override
	public String toString() {
		return 1 + ": " + this.getClass().getSimpleName();
	}
}
