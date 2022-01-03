package com.sourceflag.spring.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * MyListenerA
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-06-22 16:20
 * @since 1.0
 */
@Component
public class LoginListener implements ApplicationListener<LoginEvent> {
	@Override
	public void onApplicationEvent(LoginEvent event) {
		LoginUser loginUser = (LoginUser) event.getSource();
		System.out.println(Thread.currentThread().getName() + " LoginListener: " + loginUser + " = " + event.getTimestamp());
	}
}
