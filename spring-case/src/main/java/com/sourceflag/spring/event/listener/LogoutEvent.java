package com.sourceflag.spring.event.listener;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class LogoutEvent extends ApplicationEvent {

	private static final long serialVersionUID = -1L;

	private long timestamp;

	public LogoutEvent(Object source, long timestamp) {
		super(source);
		this.timestamp = timestamp;
	}
}
