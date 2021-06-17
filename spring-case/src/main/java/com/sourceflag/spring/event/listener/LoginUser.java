package com.sourceflag.spring.event.listener;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LoginUser
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-16 23:18
 * @since 1.0
 */
@Data
public class LoginUser {

	private int id;
	private String name;

	public LoginUser(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "LoginUser{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}

}
