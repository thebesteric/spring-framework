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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

import java.util.*;

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

		// 📝 这里列举一下 Spring 启动过程中，在构建 reader 的时候，会加入一些 BD
		// new RootBeanDefinition(ConfigurationClassPostProcessor.class); => BeanDefinitionRegistryPostProcessor => BeanFactoryPostProcessor
		// new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class); => BeanPostProcessor
		// new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class); => BeanPostProcessor

		// 📝 beanFactoryPostProcessors 有两种类型：
		// 🏷️ 1、会首先处理内置的 BeanDefinitionRegistryPostProcessor 类，可以去注册 BD
		// 🏷️ 2、再会处理 BeanFactoryPostProcessor 类
		// 为什么会执行处理这两种类？
		// 因为 ConfigurationClassPostProcessor 继承了 BeanDefinitionRegistryPostProcessor
		// 而 BeanDefinitionRegistryPostProcessor 又继承了 BeanFactoryPostProcessor

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		// ⭐️ 记录已经处理过的 bean
		Set<String> processedBeans = new HashSet<>();

		// ⭐️ 如果是 BeanDefinitionRegistry 才会执行下面的代码（保证了先执行内置的 BeanDefinitionRegistry 类）
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			// ⭐️ regularPostProcessors 里面存放的是 BeanFactoryPostProcessor
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();

			// 这个集合其实有几层含义
			// 1、首先它是全部的 BeanDefinitionRegistryPostProcessor 的集合
			// 2、其次它也是全部的 Spring 内置的 BeanFactoryPostProcessor 的集合
			// 这个集合的意义在哪里呢？其实就是因为 BeanDefinitionRegistryPostProcessor 继承了 BeanFactoryPostProcessor
			// 所以 全部的 BeanDefinitionRegistryPostProcessor 的集合 = 全部的 BeanFactoryPostProcessor 的集合
			// 当后面 Spring 执行 BeanFactoryPostProcessor 的时候只需要遍历换个集合（regularPostProcessors）即可
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// ⚠️ 通常这里是不会执行的，因为此时没有任何一个 bean 在 beanFactoryPostProcessors 中
			// ⚠️ 这里是处理手动通过 ctx.addBeanFactoryPostProcessor 手动添加进来的 BeanFactoryPostProcessor
			// 实现了 BeanDefinitionRegistryPostProcessor 接口的 postProcessBeanDefinitionRegistry 方法
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// ⭐️ 执行 ConfigurationClassPostProcessor 的 postProcessBeanDefinitionRegistry 方法
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					// 执行完之后，放入 registryProcessors 集合中
					// 同时将 BeanDefinitionRegistryPostProcessor 加入 registryProcessors 集合
					// 因为 BeanDefinitionRegistryPostProcessor 继承了 BeanFactoryPostProcessor
					registryProcessors.add(registryProcessor);
				}
				else {
					// ⚠️ 如果不是 BeanDefinitionRegistryPostProcessor，那么一定是 BeanFactoryPostProcessor
					// 就会放到 regularPostProcessors 集合中，等待后续步骤执行，要保证先执行 BeanDefinitionRegistryPostProcessor 的接口的 postProcessBeanDefinitionRegistry
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

			// ======================================= 第一阶段 =======================================
			// ==== 处理：实现了 BeanDefinitionRegistryPostProcessors 和 PriorityOrdered 接口的 bean ====
			// =======================================================================================

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			// ⭐️ 将 ConfigurationClassPostProcessor 注册成 bean 对象
			// 【首先】，先调用实现了 PriorityOrdered 接口的 BeanDefinitionRegistryPostProcessors 所有子类
			// 根据类型从 beanDefinitionMap 中找到名字
			// 因为此时未进行扫描，所以只有 spring 初始的 6 个 BD，所以后面还需要再进行 getBeanNamesForType
			// 同时此时不能拿原始的 BD 去比较，必须先进行合并，应为可能有父类
			// 这里地方只能找到一个 internalConfigurationAnnotationProcessor
			// ⭐️ 也就是在初始化 reader 的时候，注册的 ConfigurationClassPostProcessor，同时实现了 PriorityOrdered 接口
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				// 是否实现了 PriorityOrdered 接口（PriorityOrdered 接口，同时又继承了 PriorityOrdered 接口）
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// 加入当前需要执行的集合中，执行完会清空该集合
					// 为什么要放到集合中？是防止程序员也会提供一个实现
					// beanFactory.getBean()，1、从容器中直接拿 bean，2、如果拿不到会实例化该 bean
					// ⭐️ 关键代码：beanFactory.getBean() 会将 ConfigurationClassPostProcessor 类进行实例化，放如 singletonMap 中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 找到的 bean 将名字放入 processedBeans 集合中，后面会用这个集合进行判断，是否执行过，就不再进行执行
					processedBeans.add(ppName);
				}
			}
			// 升序排序，所以值越低，优先级越高
			sortPostProcessors(currentRegistryProcessors, beanFactory);

			// 将所有的 实现了 BeanDefinitionRegistryPostProcessor 和 PriorityOrdered 接口的 bean 放入 registryProcessors 集合
			// 其实就是 ConfigurationClassPostProcessor 的 bean
			registryProcessors.addAll(currentRegistryProcessors);

			// ⭐️ 此时会调用 ConfigurationClassPostProcessor 的 postProcessBeanDefinitionRegistry 方法：解析配置类，并判断配置类是否是 full 或者 lite
			// 因为只有 ConfigurationClassPostProcessor 继承了 BeanDefinitionRegistryPostProcessor
			// 会解析配置类，@Bean，@Import，@ComponentScan 注解的 Bean 都会扫描出来
			// ⭐️ 该方法完成以后，所有的 bean 都会被扫描出来放入 beanDefinitionMap 中
			// ⭐️ Mybatis 也是通过这里入手，完成的扫描
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());

			// 清空当前需要执行的集合清空，也表示这一种策略执行完成了
			currentRegistryProcessors.clear();

			// ======================================= 第二阶段 =======================================
			// ======== 处理：实现了 BeanDefinitionRegistryPostProcessors 和 Ordered 接口的 bean ========
			// ===================== 同时会排除掉实现了 PriorityOrdered 接口的 bean ======================
			// ============ 同时已经执行过的 bean 会存放在 processedBeans，所以执行过的不会再执行了 ==========
			// =======================================================================================

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			// ⭐️ 此时已经通过上一步完成了扫描
			// 【下一步】此时第二次调用 getBeanNamesForType 可以拿到所有的 BD，同时完成了第一次的 BD 合并
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			// ⭐️ 【接下来】，调用实现了 Ordered 接口的 BeanDefinitionRegistryPostProcessors
			for (String ppName : postProcessorNames) {
				// ⭐️ 执行过的不会再执行了，并且 实现了 Ordered 接口
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 记录一下已经执行过的 bean
					processedBeans.add(ppName);
				}
			}

			// 升序排序，所以值越低，优先级越高
			sortPostProcessors(currentRegistryProcessors, beanFactory);

			// 将所有的 实现了 BeanDefinitionRegistryPostProcessor 和 Ordered 接口的 bean 放入 registryProcessors 集合
			registryProcessors.addAll(currentRegistryProcessors);

			// ⭐️ 执行 postProcessBeanDefinitionRegistry 方法
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// ======================================= 第三阶段 =======================================
			// ============== 处理：实现了 BeanDefinitionRegistryPostProcessors 接口的 bean =============
			// ============ 同时已经执行过的 bean 会存放在 processedBeans，所以执行过的不会再执行了 ==========
			// =======================================================================================

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			// ⭐️ 【最终】调用所有其他的 BeanDefinitionRegistryPostProcessors
			// 即既没有实现 PriorityOrdered 也没有实现 Ordered 接口的 BeanDefinitionRegistryPostProcessors
			// 在这个过程中，可能会想 beanFactory 注册另外的 BeanDefinitionRegistryPostProcessors，所以需要循环
			// 🌰 比如 A 注册了 B 和 C，B 又注册了 D 和 E，那么 B 和 C 会按顺序执行，D 和 E 也会按顺序执行，但是 B、C、D、E 整体不能保证顺序
			// 🧩 这里就会找到 mybatis
			// ⭐️ 这个阶段执行结束后，那么意味着所有的 BeanDefinitionRegistryPostProcessors 的 postProcessBeanDefinitionRegistry 就都执行完了
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					// 执行过的不会再执行了
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						// 以防止我们在利用 BeanDefinitionRegistryPostProcessor 注册的时候
						// 又注册了新的 BeanDefinitionRegistryPostProcessor 的 BD 所以还需要循环一次
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
			// ⭐️ 现在，已经执行完目前所有的 BeanDefinitionRegistryPostProcessor 接口的 postProcessBeanDefinitionRegistry 方法
			// 1、利用 API new 的
			// 2、Spring 自己内置的
			// 3、通过实现 BeanDefinitionRegistryPostProcessor 接口注册的 BD
			// ⭐️ 开始执行所有实现了 BeanDefinitionRegistryPostProcessor 接口的父类的 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
			// 也就是 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
			// ⭐️ 同时也完成了对 AppConfig 的 CGLIB 动态代理
			// ⭐️ 这里就和配置类是 full 还是 lite 有关系了
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			// ⭐️ 执行所有通过 applicationContext 手动添加的，只实现了 BeanFactoryPostProcessor 接口的 postProcessBeanFactory 方法
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
		// ⭐️ 因为上面的代码通过执行子类，已经扫描出来了程序员提供的 BeanFactoryPostProcessor 类
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
			// 第一类：实现了 PriorityOrdered 接口
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			// 第二类：实现了 Ordered 接口
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 第三类：普通的 BeanFactoryPostProcessor
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// ======================================= 第一阶段 =======================================
		// ==== 处理：实现了 BeanFactoryPostProcessor 和 PriorityOrdered 接口的 bean ====

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		// 【首先】执行实现了 PriorityOrdered 接口的 BeanFactoryPostProcessor 接口的 bean
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// ======================================= 第二阶段 =======================================
		// ============== 处理：实现了 BeanFactoryPostProcessor 和 Ordered 接口的 bean ==============
		// =======================================================================================

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		// 【然后】执行实现了 Ordered 接口的 BeanFactoryPostProcessor 接口的 bean
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// ======================================= 第三阶段 =======================================
		// ==================== 处理：实现了 BeanFactoryPostProcessor 接口的 bean ===================
		// =======================================================================================

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

		// 📝 这里列举一下 Spring 启动过程中，在构建 reader 的时候，会加入一些 BD
		// new RootBeanDefinition(ConfigurationClassPostProcessor.class); => BeanDefinitionRegistryPostProcessor => BeanFactoryPostProcessor
		// new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class); => BeanPostProcessor
		// new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class); => BeanPostProcessor

		// ⭐️ 找到所有实现了 BeanPostProcessor 类型的 bean 的名字
		// ⚠️ 这里并不是 bean，只是 name，因为此时还没有 bean，因为这个方法就是将 name 转换成 bean 的
		// 这里会获取到 AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		// ⭐️ beanProcessorTargetCount 表示所有 BeanPostProcessor 的数量，+1 表示 BeanPostProcessorChecker
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		// BeanPostProcessorChecker 是做 bean 角色检查的，可以忽略
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// 📝 实现了 PriorityOrdered 接口的 BeanPostProcessor 集合
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 📝 实现了 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor 集合
		// 由于可以多实现，所以任何 BeanPostProcessor 都可能会实现 MergedBeanDefinitionPostProcessor
		// 所以这个集合可能会重复
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// 📝 实现了 Ordered 接口的 BeanPostProcessor 集合
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 📝 普通的 BeanPostProcessor 集合
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		// ♻️ 循环所有的 beanPostProcessor
		for (String ppName : postProcessorNames) {
			// 🏷️ 实现了 PriorityOrdered 接口的 BeanPostProcessor
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 直接会实例化 BeanPostProcessor
				// ⭐️ 默认情况下：这里会实例化 AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				// 检查是否实现了 MergedBeanDefinitionPostProcessor
				// 由于 AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor 也实现了 MergedBeanDefinitionPostProcessor
				// 所以也会加入 internalPostProcessors 集合中
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			// 🏷️ 实现了 Ordered 接口的 BeanPostProcessor
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 🏷️ 普通的 BeanPostProcessor
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// ======================================= 第一阶段 =======================================
		// ============= 处理：实现了 BeanPostProcessors 和 PriorityOrdered 接口的 bean =============
		// =======================================================================================

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		// 升序排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// ⭐️ 注册：实现了 PriorityOrdered 接口的 BeanPostProcessor，加入到 beanPostProcessors 集合
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// ======================================= 第二阶段 =======================================
		// ================= 处理：实现了 BeanPostProcessors 和 Ordered 接口的 bean =================
		// =======================================================================================

		// Next, register the BeanPostProcessors that implement Ordered.
		// ⭐️ 查找所有实现了 Ordered 接口的 BeanPostProcessor
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			// 检查是否实现了 MergedBeanDefinitionPostProcessor
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 升序排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// ⭐️ 注册：实现了 Ordered 接口的 BeanPostProcessor，加入到 beanPostProcessors 集合
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// ======================================= 第三阶段 =======================================
		// ======================= 处理：实现了 BeanPostProcessors 接口的 bean ======================
		// =======================================================================================

		// Now, register all regular BeanPostProcessors.
		// ⭐️ 查找普通的 BeanPostProcessor
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			// 检查是否实现了 MergedBeanDefinitionPostProcessor
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// ⭐️ 注册：普通的 BeanPostProcessor，加入到 beanPostProcessors 集合
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// ======================================= 第四阶段 =======================================
		// ================ 处理：实现了 MergedBeanDefinitionPostProcessor 接口的 bean ==============
		// =======================================================================================

		// Finally, re-register all internal BeanPostProcessors.
		// 升序排序
		sortPostProcessors(internalPostProcessors, beanFactory);
		// ⭐️ 注册：实现了 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor
		// ⚠️这里要注意一下，AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor 虽然实现了 PriorityOrdered
		// 但是也实现了 MergedBeanDefinitionPostProcessor 接口，所以这两个在 beanPostProcessors 的排序是比较靠后的
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// ======================================= 第五阶段 =======================================
		// ============================ 处理：ApplicationListenerDetector =========================
		// =======================================================================================

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// ⭐️ 注册一个 ApplicationListenerDetector 的 BeanPostProcessor，放在 beanPostProcessors 的最后
		// ApplicationListenerDetector 其实在 prepareBeanFactory 阶段已经加入到 beanPostProcessors 中了，这里会重复添加一遍
		// 原因就是：期望 ApplicationListenerDetector 在所有的 beanPostProcessor 的最后，因为 addBeanPostProcessor 方法会先删除，在添加
		// ApplicationListenerDetector 是处理实现了 ApplicationListener 接口的 bean
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
			// ⭐️ 执行 postProcessBeanDefinitionRegistry 方法
			// ⭐️ ConfigurationClassPostProcessor 会来解析配置类
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
			// ⭐️ 执行 BeanFactory 的 postProcessBeanFactory 方法
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
