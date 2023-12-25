package com.sourceflag.spring.postprocessor;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyInject {
	String value();
}
