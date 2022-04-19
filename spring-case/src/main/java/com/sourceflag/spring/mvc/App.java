package com.sourceflag.spring.mvc;

// import org.apache.catalina.LifecycleException;
// import org.apache.catalina.startup.Tomcat;

import org.apache.catalina.startup.Tomcat;

public class App {

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        // Context context = tomcat.addContext("/", System.getProperty("java.io.tmpdir"));
        // context.addLifecycleListener((LifecycleListener) Class.forName(tomcat.getHost().getConfigClass()).newInstance());

		// String docBase = "D:\\Documents\\Java\\Spring\\spring\\spring-framework-5.3.2\\spring-case\\src\\main\\webapp";
		String docBase = "/Users/keisun/IdeaProjects/research/source/spring/spring-framework-5.3.2/spring-case/src/main/webapp";

		/*
		 * / 表示所有的请求都会被 DispatcherServlet 所匹配
		 * DispatcherServlet: 称为前端控制值，可以理解为是请求的调度中心
		 */
        tomcat.addWebapp("/", docBase);

        tomcat.start();
        tomcat.getServer().await();
    }

}
