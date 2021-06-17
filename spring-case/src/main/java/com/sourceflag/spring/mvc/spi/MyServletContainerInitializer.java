package com.sourceflag.spring.mvc.spi;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

// SPI 机制：javax.servlet.ServletContainerInitializer 中会定义这个实现类
// @HandlesTypes 会找到指定接口的实现类，封装到 webAppInitializerClasses 入参中
@HandlesTypes(TestWebApplicationInitializer.class)
public class MyServletContainerInitializer implements ServletContainerInitializer {
    public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext) throws ServletException {
        for (Class<?> webAppInitializerClass : webAppInitializerClasses) {
            try {
            	// 调用接口方法
                TestWebApplicationInitializer obj = (TestWebApplicationInitializer) webAppInitializerClass.getDeclaredConstructor().newInstance();
                obj.start(servletContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
