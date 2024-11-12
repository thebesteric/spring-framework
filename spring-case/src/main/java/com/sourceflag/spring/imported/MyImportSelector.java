package com.sourceflag.spring.imported;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyImportSelector implements ImportSelector {
	@Override
	public String[] selectImports(AnnotationMetadata metadata) {

		for (Map.Entry<String, Object> entry : metadata.getAnnotationAttributes(TestAnno.class.getName()).entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}


		for (MergedAnnotation<Annotation> annotation : metadata.getAnnotations()) {
			System.out.println(annotation);
		}


		List<String> classes = new ArrayList<>();
		classes.add(UserService.class.getName());

        try {
            Class<?> aClass = Class.forName("com.sourceflag.spring.rootbd.User");
			classes.add(aClass.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

		return classes.toArray(new String[0]);

        // return new String[]{UserService.class.getName()};
		//
		// return new String[]{MyConfiguration.class.getName()};
	}
}
