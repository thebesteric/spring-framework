package com.sourceflag.spring.transactional.model;

import lombok.Data;

/**
 * User
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-02 22:49
 * @since 1.0
 */
@Data
public class User {
	private long id;
	private String name;

	public User(String name) {
		this.name = name;
	}

}
