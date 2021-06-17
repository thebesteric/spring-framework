package com.sourceflag.spring.event.listener;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class LoginEvent extends ApplicationEvent {

	private static final long serialVersionUID = -1L;

	private long timestamp;

	public LoginEvent(Object source, long timestamp) {
		super(source);
		this.timestamp = timestamp;
	}
}
