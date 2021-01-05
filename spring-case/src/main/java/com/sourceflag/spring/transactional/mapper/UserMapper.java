package com.sourceflag.spring.transactional.mapper;

import com.sourceflag.spring.transactional.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface UserMapper {

	@Insert("insert into t_user(name) values(#{name})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert1(User user);

	@Insert("insert into t_user(name) values(#{name})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert2(User user);

}
