package com.sourceflag.spring.ioc;

/**
 * InterfaceService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-18 10:31:41
 */
public interface InterfaceService<T> {
	void setService(T service);
}
