package com.sourceflag.spring.mvc.spi;

import javax.servlet.ServletContext;

public interface TestWebApplicationInitializer {
    void start(ServletContext servletContext);
}
