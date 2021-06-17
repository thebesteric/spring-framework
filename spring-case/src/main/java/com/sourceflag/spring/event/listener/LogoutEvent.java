package com.sourceflag.spring.event.listener;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class LogoutEvent extends ApplicationEvent {

	private static final long serialVersionUID = -1L;

	private String name;

	public LogoutEvent(Object source) {
		super(source);
	}
}
