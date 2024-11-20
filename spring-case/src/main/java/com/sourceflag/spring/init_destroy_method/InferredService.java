package com.sourceflag.spring.init_destroy_method;

/**
 * InferredService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-14 20:57:24
 */
public class InferredService {

	public void shutdown() {
		System.out.println("InferredService shutodwn method");
	}

	public void close() {
		System.out.println("InferredService close method");
	}

}
