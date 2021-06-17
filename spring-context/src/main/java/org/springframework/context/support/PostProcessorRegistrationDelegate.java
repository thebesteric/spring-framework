/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// ★★★ 1、会首先处理内置的 BeanDefinitionRegistryPostProcessor 类
		// ★★★ 2、再会处理 BeanFactoryPostProcessor 类
		// 为什么会执行处理这两种类？
		// 因为 ConfigurationClassPostProcessor 继承了 BeanDefinitionRegistryPostProcessor
		// 而 BeanDefinitionRegistryPostProcessor 又继承了 BeanFactoryPostProcessor


		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		// 如果是 BeanDefinitionRegistry 才会执行下面的代码（保证了先执行内置的 BeanDefinitionRegistry 类）
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			// regularPostProcessors 里面存放的是 BeanFactoryPostProcessor
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();

			// 这个集合其实有几层含义
			// 1、首先它是全部的 BeanDefinitionRegistryPostProcessor 的集合
			// 2、其次它也是全部的 Spring 内置的 BeanFactoryPostProcessor 的集合
			// 这个集合的意义在哪里呢？其实就是因为 BeanDefinitionRegistryPostProcessor 继承了 BeanFactoryPostProcessor
			// 所以 全部的 BeanDefinitionRegistryPostProcessor 的集合 = 全部的 BeanFactoryPostProcessor 的集合
			// 当后面 spring 执行 BeanFactoryPostProcessor 的时候只需要遍历换个集合（regularPostProcessors）即可
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// 通常这里是不会执行的
			// 这里会直接执行 通过 ctx.addBeanFactoryPostProcessor 手动添加进来的，实现了 BeanDefinitionRegistryPostProcessor 接口的 postProcessBeanDefinitionRegistry 方法
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// ★★★ 此时会调用 ConfigurationClassPostProcessor 的 postProcessBeanDefinitionRegistry 方法：解析配置类，并判断配置类是否是 full 或者 lite
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					// 执行完之后，放入 registryProcessors 集合中
					// 现在我们可以认为 registryProcessors 放的是 BeanDefinitionRegistryPostProcessor 的集合
					registryProcessors.add(registryProcessor);
				}
				else {
					// 如果不是 BeanDefinitionRegistryPostProcessor，那么一定是 BeanFactoryPostProcessor
					// 就会放到 regularPostProcessors 集合中
					// 现在我们可以认为 regularPostProcessors 放的是 BeanFactoryPostProcessor 的集合
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			// 当前需要执行的 BeanDefinitionRegistryPostProcessor 集合
			// 什么叫当前？因为 spring 使用了策略模式，不同的策略执行的时机不同
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			// ★★★ 【首先】，先调用实现了 PriorityOrdered 接口的 BeanDefinitionRegistryPostProcessors 所有子类
			// 根据类型从 beanDefinitionMap 中找到名字
			// 因为此时未进行扫描，所以只有 spring 初始的 6 个 BD，所以后面还需要再进行 getBeanNamesForType
			// 同时此时不能拿原始的 BD 去比较，必须先进行合并，应为可能有父类
			// 这里地方只能找到一个 internalConfigurationAnnotationProcessor
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				// 是否实现了 PriorityOrdered 接口（PriorityOrdered 接口，同时又继承了 PriorityOrdered 接口）
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// 加入当前需要执行的集合中，执行完会清空该集合
					// 为什么要放到集合中？是防止程序员也会提供一个实现
					// beanFactory.getBean()，1、从容器中直接拿 bean，2、如果拿不到会实例化该 bean
					// ★★★ 关键代码：beanFactory.getBean() 会将 ConfigurationClassPostProcessor 类进行实例化，放如 singletonMap 中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 找到的 bean 将名字放入 processedBeans 集合中，后面会用这个集合进行判断，是否执行过，就不再进行执行
					processedBeans.add(ppName);
				}
			}
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 将所有的 bean 放入 registryProcessors 集合
			registryProcessors.addAll(currentRegistryProcessors);
			// ★★★ 执行 ConfigurationClassPostProcessor 的 postProcessBeanDefinitionRegistry 方法
			// 因为只有 ConfigurationClassPostProcessor 继承了 BeanDefinitionRegistryPostProcessor
			// 会解析配置类，@Bean，@Import，@ComponentScan 注解的 Bean 都会扫描出来
			// 该方法完成以后，所有的 bean 都会被扫描出来放入 beanDefinitionMap 中
			// ★★★ Mybatis 也是通过这里入手，完成的扫描
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 清空当前需要执行的集合清空，也表示这一种策略执行完成了
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			// ★★★ 此时已经通过上一步完成了扫描
			// 此时第二次调用 getBeanNamesForType 可以拿到所有的 BD，同时完成了第一次的 BD 合并
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			// ★★★ 【接下来】，调用实现了 Ordered 接口的 BeanDefinitionRegistryPostProcessors
			for (String ppName : postProcessorNames) {
				// 执行过的不会再执行了，并且 实现了 Ordered 接口
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			// ★★★ postProcessBeanDefinitionRegistry 方法
			// ConfigurationClassPostProcessor 由于在上面执行过了，所以这里不会在执行
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			// ★★★ 【最终】调用所有其他的 BeanDefinitionRegistryPostProcessors
			// 即既没有实现 PriorityOrdered 也没有实现 Ordered 接口的 BeanDefinitionRegistryPostProcessors
			// 这里就会找到 mybatis
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					// 执行过的不会再执行了
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						// 以防止我们会进行注册新的 BeanDefinitionRegistryPostProcessor 的 BD，所以还需要循环一次
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				// 因为 registry 还可能注册其他的 bd，所以这个类可能还会扫描出来其他的 BeanFactoryPostProcessor 类
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			// 现在，已经执行完所有的 BeanDefinitionRegistryPostProcessor 调的 postProcessBeanFactory回调
			// 1、利用 API new 的
			// 2、Spring 自己内置的
			// ★★★ 现在开始执行所有 实现了 BeanDefinitionRegistryPostProcessor 接口的父类的 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
			// 也就是 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
			// ★★★ 同时也完成了对 AppConfig 的 CGLIB 动态代理
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			// ★ 执行所有通过 applicationContext 手动添加的，只实现了 BeanFactoryPostProcessor 接口的 postProcessBeanFactory 方法
			// regularPostProcessors 中存放的是只实现了 BeanFactoryPostProcessor 接口的 bean
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}
		// 非 BeanDefinitionRegistryPostProcessor 的实现会执行下面的代码
		// 也就是 直接实现 BeanFactoryPostProcessor 的类
		else {
			// Invoke factory processors registered with the context instance.
			// 如果 beanFactory 不是 BeanDefinitionRegistry 类型的话，会在这里执行
			// 比如：EventListenerMethodProcessor 会在这里执行
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// ★★★ 因为上面的代码通过执行子类，已经扫描出来了程序员提供的 BeanFactoryPostProcessor 类
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
				// 已经在第一阶段执行过了，此处就不执行了
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		// 【首先】执行实现了 PriorityOrdered 接口的 BeanFactoryPostProcessor 接口的 bean
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		// 【然后】执行实现了 Ordered 接口的 BeanFactoryPostProcessor 接口的 bean
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		// 【最后】执行普通的实现了 BeanFactoryPostProcessor 接口的 bean
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 实例化 BeanPostProcessor
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry, ApplicationStartup applicationStartup) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanDefRegistry = applicationStartup.start("spring.context.beandef-registry.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanDefinitionRegistry(registry);
			postProcessBeanDefRegistry.end();
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		if (beanFactory instanceof AbstractBeanFactory) {
			// Bulk addition is more efficient against our CopyOnWriteArrayList there
			((AbstractBeanFactory) beanFactory).addBeanPostProcessors(postProcessors);
		}
		else {
			for (BeanPostProcessor postProcessor : postProcessors) {
				beanFactory.addBeanPostProcessor(postProcessor);
			}
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
