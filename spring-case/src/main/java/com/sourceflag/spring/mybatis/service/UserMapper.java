package com.sourceflag.spring.mybatis.service;

import com.sourceflag.spring.mybatis.anno.Select;

/**
 * UserMapper
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-29 22:43
 * @since 1.0
 */
public interface UserMapper {

	@Select("select * from User where id = {0}")
	int getById(int id);

}
