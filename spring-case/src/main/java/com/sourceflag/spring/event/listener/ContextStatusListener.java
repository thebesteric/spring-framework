package com.sourceflag.spring.event.listener;

import org.springframework.context.event.*;
import org.springframework.stereotype.Component;

/**
 * ContextStatusListener
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-16 23:57
 * @since 1.0
 */
@Component
public class ContextStatusListener {


	@EventListener(ContextStartedEvent.class)
	public void onStarted(ContextStartedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			System.out.println("spring started...");
		}
	}

	@EventListener(ContextRefreshedEvent.class)
	public void onRefreshed(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			System.out.println("spring refreshed...");
		}
	}

	@EventListener(ContextStoppedEvent.class)
	public void onStopped(ContextStoppedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			System.out.println("spring stopped...");
		}
	}

	@EventListener(ContextClosedEvent.class)
	public void onClosed(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			System.out.println("spring closed...");
		}
	}

}
