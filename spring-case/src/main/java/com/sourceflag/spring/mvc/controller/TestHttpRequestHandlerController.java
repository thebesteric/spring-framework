package com.sourceflag.spring.mvc.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TestHttpRequestHandler
 *
 * 由 BeanNameUrlHandlerMapping 来解析
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-10 21:57
 * @since 1.0
 */
@Component("/testHttpRequestHandlerController.do")
public class TestHttpRequestHandlerController implements HttpRequestHandler {
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getServletContext().log("进入 TestHttpRequestHandlerController 处理器..." + this);
		request.getRequestDispatcher("/page/index.html").forward(request, response);
	}
}
