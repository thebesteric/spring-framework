package com.sourceflag.spring.mvc.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * TestServlet
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-10 21:59
 * @since 1.0
 */
// @Component
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * 这里会先调用父类的 service(req, resp); 方法，通过 req.getMethod(); 判断方法
	 * 从而对应去调用 doGet(req, resp); 或 doPost(req, resp);
	 */

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		writer.write("Enter TestServlet");
		writer.flush();
	}
}
