package com.sourceflag.spring.mybatis.factory;

import com.sourceflag.spring.mybatis.anno.Select;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * MyFactoryBean
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 22:28
 * @since 1.0
 */
// @Component
public class MyFactoryBean implements FactoryBean<Object> {

	private Class<?> mapperInterface;



	public MyFactoryBean(Class<?> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	// private SqlSession sqlSession;
	//
	// public void setSqlSession(SqlSessionFactory sqlSessionFactory) {
	// 	this.sqlSession = sqlSessionFactory.openSession();
	// }

	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(MyFactoryBean.class.getClassLoader(), new Class<?>[]{mapperInterface}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				String select = method.getAnnotation(Select.class).value();
				System.out.println(method.getName() + " => " + select);
				return args;
			}
		});
		// sqlSession.getConfiguration().addMapper(mapperInterface);
		// return sqlSession.getMapper(mapperInterface);
	}

	@Override
	public Class<?> getObjectType() {
		return mapperInterface;
	}
}
