package com.sourceflag.spring.imported;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestAnno {
	boolean start() default true;
}
