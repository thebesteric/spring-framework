package com.sourceflag.spring.qualifier.loadbalance;

import com.sourceflag.spring.qualifier.Sequence;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SequanceLoadBalance
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-20 14:23:33
 */
@Component
@Sequence
public class SequenceLoadBalance implements LoadBalance {

	private static final AtomicInteger SEQ = new AtomicInteger(1);

	@Override
	public Integer select() {
		int next = SEQ.getAndIncrement();
		if (next == 5) {
			SEQ.set(1);
		}
		return next;
	}

}
