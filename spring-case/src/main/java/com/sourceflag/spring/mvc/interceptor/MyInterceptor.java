package com.sourceflag.spring.mvc.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MyInterceptor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-06-17 00:38
 * @since 1.0
 */
public class MyInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		System.out.println("=========== MyInterceptor preHandle =============");
		// true，表示继续往下执行
		// false，请求将停止，不会在执行了
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		System.out.println("=========== MyInterceptor postHandle =============");
	}
}
