package com.sourceflag.spring.ordercomparator;

import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * App
 *
 * @author wangweijun
 * @version v1.0
 * @since 2023-12-14 19:05:50
 */
public class App {

	public static void main(String[] args) {
		A a = new A(); // order = 3
		B b = new B(); // order = 2
		C c = new C(); // order = 1

		OrderComparator orderComparator = new OrderComparator();
		// 1：代表前面比后面大
		// 0：代表前面和后面一样大
		// -1：代表前面比后面小
		System.out.println(orderComparator.compare(a, b)); // 1

		List<Object> list = new ArrayList<>();
		list.add(a);
		list.add(b);
		list.add(c);
		// 按照升序进行排序
		list.sort(orderComparator);
		System.out.println(list); // C、B、A

		System.out.println("==========================================");

		Foo foo = new Foo(); // order = 1
		Bar bar = new Bar(); // order = 2

		AnnotationAwareOrderComparator annotationAwareOrderComparator = new AnnotationAwareOrderComparator();
		// 1：代表前面比后面大
		// 0：代表前面和后面一样大
		// -1：代表前面比后面小
		System.out.println(annotationAwareOrderComparator.compare(foo, bar)); // -1

		List<Object> list1 = new ArrayList<>();
		list1.add(foo);
		list1.add(bar);
		list1.sort(annotationAwareOrderComparator);
		System.out.println(list1); // foo、bar

	}

}
