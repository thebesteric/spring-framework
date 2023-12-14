package com.sourceflag.spring.ordercomparator;

import org.springframework.core.Ordered;

/**
 * A
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 19:01:41
 */

public class C  implements Ordered {
	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public String toString() {
		return 1 + ": " + this.getClass().getSimpleName();
	}
}
