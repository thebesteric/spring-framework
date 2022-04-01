package com.sourceflag.spring.mybatis.service;

import com.sourceflag.spring.mybatis.anno.Select;

public interface OrderMapper {

	@Select("select * from Order where id = {0}")
	int getById(int id);

}
