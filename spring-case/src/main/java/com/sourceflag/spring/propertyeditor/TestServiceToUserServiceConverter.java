package com.sourceflag.spring.propertyeditor;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.Collections;
import java.util.Set;

/**
 * TestServiceToUserServiceConverter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 18:13:42
 */
public class TestServiceToUserServiceConverter implements ConditionalGenericConverter {
	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return sourceType.getType().equals(UserService.class) && targetType.getType().equals(TestService.class);
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(UserService.class, TestService.class));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		return new TestService();
	}
}
