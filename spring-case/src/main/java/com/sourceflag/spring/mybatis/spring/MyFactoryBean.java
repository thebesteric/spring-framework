package com.sourceflag.spring.mybatis.spring;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * MyFactoryBean
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-01-03 22:59:41
 */
public class MyFactoryBean implements FactoryBean<Object> {

	private Class<?> mapperInterface;

	private SqlSession sqlSession;

	public MyFactoryBean(Class<?> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}


	@Override
	public Object getObject() throws Exception {
		// 通过 JDK 生成代理对象
		// return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{mapperInterface}, new InvocationHandler() {
		// 	@Override
		// 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 		String[] select = method.getAnnotation(Select.class).value();
		// 		String result = Arrays.toString(select);
		// 		System.out.println(method.getName() + " => " + result);
		// 		return result;
		// 	}
		// });

		// 通过 sqlSession 生成代理对象
		return sqlSession.getMapper(mapperInterface);
	}

	@Override
	public Class<?> getObjectType() {
		return mapperInterface;
	}

	public void setSqlSession(SqlSessionFactory sqlSessionFactory) {
		sqlSessionFactory.getConfiguration().addMapper(mapperInterface);
		this.sqlSession = sqlSessionFactory.openSession();
	}
}
