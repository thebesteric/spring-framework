package com.sourceflag.spring.mybatis.mapper;

import org.apache.ibatis.annotations.Select;

// 通过添加一个 includeFilter 来忽略 Spring 判断 @Component 注解的条件
// @Component
public interface UserMapper {
	@Select("select 'user'")
	String getById(Integer id);
}
