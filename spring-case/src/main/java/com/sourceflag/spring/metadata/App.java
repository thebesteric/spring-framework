package com.sourceflag.spring.metadata;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class App {
	public static void main(String[] args) throws IOException {
		SimpleMetadataReaderFactory simpleMetadataReaderFactory = new SimpleMetadataReaderFactory();

		// 构造一个MetadataReader
		MetadataReader metadataReader = simpleMetadataReaderFactory.getMetadataReader("com.sourceflag.spring.metadata.UserService");

		// 得到一个ClassMetadata，并获取了类名
		ClassMetadata classMetadata = metadataReader.getClassMetadata();

		// 获取所有接口
		for (String interfaceName : classMetadata.getInterfaceNames()) {
			System.out.println(interfaceName);
		}
		System.out.println(classMetadata.getClassName());

		// 获取一个AnnotationMetadata，并获取类上的注解信息
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		for (String annotationType : annotationMetadata.getAnnotationTypes()) {
			System.out.println(annotationType);
		}

		System.out.println(annotationMetadata.hasMetaAnnotation(Component.class.getName()));
		System.out.println(annotationMetadata.hasAnnotation(Component.class.getName()));

	}

	@Configuration
	@ComponentScan("com.sourceflag.spring.metadata")
	@EnableAsync
	public static class AppConfig {

	}
}
