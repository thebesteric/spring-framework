package com.sourceflag.spring.mvc.controller;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TestInterfaceController
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-10 21:59
 * @since 1.0
 */
public class TestInterfaceController implements Controller {
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, @Nullable HttpServletResponse response) throws Exception {
		request.getServletContext().log("进入 TestInterfaceController 处理器..." + this);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("index");
		mv.addObject("hello", "Hello Controller");
		return mv;
	}
}
