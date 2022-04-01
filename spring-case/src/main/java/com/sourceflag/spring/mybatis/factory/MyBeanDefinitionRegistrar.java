package com.sourceflag.spring.mybatis.factory;

import com.sourceflag.spring.mybatis.anno.MyMapperScan;
import com.sourceflag.spring.mybatis.scanner.MyMapperScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * MyBeanDefinitonRegistar
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 23:29
 * @since 1.0
 */
public class MyBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		// AbstractBeanDefinition beanDefinition1 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// beanDefinition1.setBeanClass(MyFactoryBean.class);
		// beanDefinition1.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
		// registry.registerBeanDefinition("userMapper", beanDefinition1);
		//
		// AbstractBeanDefinition beanDefinition2 = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// beanDefinition2.setBeanClass(MyFactoryBean.class);
		// beanDefinition2.getConstructorArgumentValues().addGenericArgumentValue(OrderMapper.class);
		// registry.registerBeanDefinition("orderMapper", beanDefinition1);

		// 这里可以用 扫描 来实现
		// List<Class<?>> mapperClasses = new ArrayList<>();
		// mapperClasses.add(UserMapper.class);
		// mapperClasses.add(OrderMapper.class);

		// for (Class<?> mapperClass : mapperClasses) {
		// 	AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// 	beanDefinition.setBeanClass(MyFactoryBean.class);
		// 	// 指定构造方法的入参
		// 	beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
		// 	// 注册到容器
		// 	registry.registerBeanDefinition(mapperClass.getSimpleName(), beanDefinition);
		// }

		Map<String, Object> myMapperScanAnno = importingClassMetadata.getAnnotationAttributes(MyMapperScan.class.getName());
		String path = (String) myMapperScanAnno.get("value");

		MyMapperScanner scanner = new MyMapperScanner(registry);
		// 这一步可以不用在 interface 上添加 @Component 注解了
		scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
		scanner.scan(path);
	}
}
