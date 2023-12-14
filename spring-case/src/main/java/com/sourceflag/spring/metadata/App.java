package com.sourceflag.spring.metadata;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class App {
	public static void main(String[] args) throws IOException {
		// 构造一个元数据读取器工厂
		SimpleMetadataReaderFactory simpleMetadataReaderFactory = new SimpleMetadataReaderFactory();

		// 通过工厂，构造一个 MetadataReader，并解析指定类
		MetadataReader metadataReader = simpleMetadataReaderFactory.getMetadataReader("com.sourceflag.spring.metadata.UserService");

		// 得到一个 ClassMetadata，并获取了类名
		ClassMetadata classMetadata = metadataReader.getClassMetadata();
		System.out.println(classMetadata.getClassName()); // com.sourceflag.spring.metadata.UserService

		// 获取所有接口
		for (String interfaceName : classMetadata.getInterfaceNames()) {
			// org.springframework.context.ApplicationContextAware
			// org.springframework.beans.factory.InitializingBean
			System.out.println(interfaceName);
		}

		// 获取内部类
		for (String memberClassName : classMetadata.getMemberClassNames()) {
			System.out.println(memberClassName); // com.sourceflag.spring.metadata.UserService$UserInner
		}

		// 获取一个 AnnotationMetadata，并获取类上的注解信息
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		for (String annotationType : annotationMetadata.getAnnotationTypes()) {
			System.out.println(annotationType); // org.springframework.stereotype.Service
		}

		// 是否有 @Component 注解
		System.out.println(annotationMetadata.hasMetaAnnotation(Component.class.getName())); // true
		// 是否直接有 @Component 注解
		System.out.println(annotationMetadata.hasAnnotation(Component.class.getName())); // false

	}
}
