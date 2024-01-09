package com.sourceflag.spring.mybatis.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * MyBeanDefinitonRegistrar
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 23:17:11
 */
public class MyBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<String, Object> myMapperScanAnno = importingClassMetadata.getAnnotationAttributes(MyMapperScan.class.getName());
		String path = (String) myMapperScanAnno.get("value");

		MyMapperScanner scanner = new MyMapperScanner(registry);
		// 这一步可以不用在 interface 上添加 @Component 注解了
		scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
		scanner.scan(path);
	}
}
