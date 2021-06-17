package com.sourceflag.spring.mvc.spi;

import javax.servlet.ServletContext;

public class TestWebApplicationInitializerImpl implements TestWebApplicationInitializer {
    @Override
    public void start(ServletContext servletContext) {
        System.out.println("This is my TestWebApplicationInitializer start " + servletContext);
    }
}
