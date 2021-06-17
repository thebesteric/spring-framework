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

        tomcat.addWebapp("/", "D:\\Documents\\Java\\Spring\\spring\\spring-framework-5.3.2\\spring-case\\src\\main\\webapp");

        tomcat.start();
        tomcat.getServer().await();
    }

}
