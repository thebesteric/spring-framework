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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

import javax.inject.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Spring's default implementation of the {@link ConfigurableListableBeanFactory}
 * and {@link BeanDefinitionRegistry} interfaces: a full-fledged bean factory
 * based on bean definition metadata, extensible through post-processors.
 *
 * <p>Typical usage is registering all bean definitions first (possibly read
 * from a bean definition file), before accessing beans. Bean lookup by name
 * is therefore an inexpensive operation in a local bean definition table,
 * operating on pre-resolved bean definition metadata objects.
 *
 * <p>Note that readers for specific bean definition formats are typically
 * implemented separately rather than as bean factory subclasses: see for example
 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>For an alternative implementation of the
 * {@link org.springframework.beans.factory.ListableBeanFactory} interface,
 * have a look at {@link StaticListableBeanFactory}, which manages existing
 * bean instances rather than creating new ones based on bean definitions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 16 April 2001
 * @see #registerBeanDefinition
 * @see #addBeanPostProcessor
 * @see #getBean
 * @see #resolveDependency
 */
@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	@Nullable
	private static Class<?> javaxInjectProviderClass;

	static {
		try {
			javaxInjectProviderClass =
					ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - Provider interface simply not supported then.
			javaxInjectProviderClass = null;
		}
	}


	/** Map from serialized id to factory instance. */
	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories =
			new ConcurrentHashMap<>(8);

	/** Optional id for this factory, for serialization purposes. */
	@Nullable
	private String serializationId;

	/** Whether to allow re-registration of a different definition with the same name. */
	private boolean allowBeanDefinitionOverriding = true;

	/** Whether to allow eager class loading even for lazy-init beans. */
	private boolean allowEagerClassLoading = true;

	/** Optional OrderComparator for dependency Lists and arrays. */
	@Nullable
	private Comparator<Object> dependencyComparator;

	/** Resolver to use for checking if a bean definition is an autowire candidate. */
	private AutowireCandidateResolver autowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE;

	/** Map from dependency type to corresponding autowired value. */
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

	/** Map of bean definition objects, keyed by bean name. */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

	/** Map from bean name to merged BeanDefinitionHolder. */
	private final Map<String, BeanDefinitionHolder> mergedBeanDefinitionHolders = new ConcurrentHashMap<>(256);

	/** Map of singleton and non-singleton bean names, keyed by dependency type. */
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

	/** Map of singleton-only bean names, keyed by dependency type. */
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

	/** List of bean definition names, in registration order. */
	private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

	/** List of names of manually registered singletons, in registration order. */
	private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

	/** Cached array of bean definition names in case of frozen configuration. */
	@Nullable
	private volatile String[] frozenBeanDefinitionNames;

	/** Whether bean definition metadata may be cached for all beans. */
	private volatile boolean configurationFrozen;


	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		// 调用父类
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 * @param parentBeanFactory the parent BeanFactory
	 */
	public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}


	/**
	 * Specify an id for serialization purposes, allowing this BeanFactory to be
	 * deserialized from this id back into the BeanFactory object, if needed.
	 */
	public void setSerializationId(@Nullable String serializationId) {
		if (serializationId != null) {
			serializableFactories.put(serializationId, new WeakReference<>(this));
		}
		else if (this.serializationId != null) {
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}

	/**
	 * Return an id for serialization purposes, if specified, allowing this BeanFactory
	 * to be deserialized from this id back into the BeanFactory object, if needed.
	 * @since 4.1.2
	 */
	@Nullable
	public String getSerializationId() {
		return this.serializationId;
	}

	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. This also applies to overriding aliases.
	 * <p>Default is "true".
	 * @see #registerBeanDefinition
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Return whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * @since 4.1.2
	 */
	public boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	/**
	 * Set whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * <p>Default is "true". Turn this flag off to suppress class loading
	 * for lazy-init beans unless such a bean is explicitly requested.
	 * In particular, by-type lookups will then simply ignore bean definitions
	 * without resolved class name, instead of loading the bean classes on
	 * demand just to perform a type check.
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	/**
	 * Return whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * @since 4.1.2
	 */
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	/**
	 * Set a {@link java.util.Comparator} for dependency Lists and arrays.
	 * @since 4.0
	 * @see org.springframework.core.OrderComparator
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	/**
	 * Return the dependency comparator for this BeanFactory (may be {@code null}.
	 * @since 4.0
	 */
	@Nullable
	public Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	/**
	 * Set a custom autowire candidate resolver for this BeanFactory to use
	 * when deciding whether a bean definition should be considered as a
	 * candidate for autowiring.
	 */
	public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		if (autowireCandidateResolver instanceof BeanFactoryAware) {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
					return null;
				}, getAccessControlContext());
			}
			else {
				((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	/**
	 * Return the autowire candidate resolver for this BeanFactory (never {@code null}).
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}


	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			// A clone of the AutowireCandidateResolver since it is potentially BeanFactoryAware
			setAutowireCandidateResolver(otherListableFactory.getAutowireCandidateResolver().cloneIfNecessary());
			// Make resolvable dependencies (e.g. ResourceLoader) available here as well
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}


	//---------------------------------------------------------------------
	// Implementation of remaining BeanFactory methods
	//---------------------------------------------------------------------

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
		if (resolved == null) {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		Assert.notNull(requiredType, "Required type must not be null");
		return getBeanProvider(ResolvableType.forRawClass(requiredType), true);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		return getBeanProvider(requiredType, true);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return this.beanDefinitionMap.containsKey(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		String[] frozenNames = this.frozenBeanDefinitionNames;
		if (frozenNames != null) {
			return frozenNames.clone();
		}
		else {
			return StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		Assert.notNull(requiredType, "Required type must not be null");
		return getBeanProvider(ResolvableType.forRawClass(requiredType), allowEagerInit);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		return new BeanObjectProvider<T>() {
			@Override
			public T getObject() throws BeansException {
				T resolved = resolveBean(requiredType, null, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}
			@Override
			public T getObject(Object... args) throws BeansException {
				T resolved = resolveBean(requiredType, args, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}
			@Override
			@Nullable
			public T getIfAvailable() throws BeansException {
				try {
					return resolveBean(requiredType, null, false);
				}
				catch (ScopeNotActiveException ex) {
					// Ignore resolved bean in non-active scope
					return null;
				}
			}
			@Override
			public void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
				T dependency = getIfAvailable();
				if (dependency != null) {
					try {
						dependencyConsumer.accept(dependency);
					}
					catch (ScopeNotActiveException ex) {
						// Ignore resolved bean in non-active scope, even on scoped proxy invocation
					}
				}
			}
			@Override
			@Nullable
			public T getIfUnique() throws BeansException {
				try {
					return resolveBean(requiredType, null, true);
				}
				catch (ScopeNotActiveException ex) {
					// Ignore resolved bean in non-active scope
					return null;
				}
			}
			@Override
			public void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
				T dependency = getIfUnique();
				if (dependency != null) {
					try {
						dependencyConsumer.accept(dependency);
					}
					catch (ScopeNotActiveException ex) {
						// Ignore resolved bean in non-active scope, even on scoped proxy invocation
					}
				}
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> stream() {
				return Arrays.stream(getBeanNamesForTypedStream(requiredType, allowEagerInit))
						.map(name -> (T) getBean(name))
						.filter(bean -> !(bean instanceof NullBean));
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> orderedStream() {
				String[] beanNames = getBeanNamesForTypedStream(requiredType, allowEagerInit);
				if (beanNames.length == 0) {
					return Stream.empty();
				}
				Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
				for (String beanName : beanNames) {
					Object beanInstance = getBean(beanName);
					if (!(beanInstance instanceof NullBean)) {
						matchingBeans.put(beanName, (T) beanInstance);
					}
				}
				Stream<T> stream = matchingBeans.values().stream();
				return stream.sorted(adaptOrderComparator(matchingBeans));
			}
		};
	}

	@Nullable
	private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
		if (namedBean != null) {
			return namedBean.getBeanInstance();
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			return ((DefaultListableBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
		}
		else if (parent != null) {
			ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
			if (args != null) {
				return parentProvider.getObject(args);
			}
			else {
				return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
			}
		}
		return null;
	}

	private String[] getBeanNamesForTypedStream(ResolvableType requiredType, boolean allowEagerInit) {
		return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		return getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		Class<?> resolved = type.resolve();
		if (resolved != null && !type.hasGenerics()) {
			return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
		}
		else {
			return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		}
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
		}

		// includeNonSingletons = true 包含所有 bean，默认情况下是：allBeanNamesByType
		// includeNonSingletons = false 只包含单例 bean
		Map<Class<?>, String[]> cache =
				(includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
		String[] resolvedBeanNames = cache.get(type);
		if (resolvedBeanNames != null) {
			return resolvedBeanNames;
		}

		// ⭐️ 根据 bean 的类型找到符合条件的 beanNames
		resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);

		// 缓存逻辑
		if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
			// 加入缓存
			cache.put(type, resolvedBeanNames);
		}
		return resolvedBeanNames;
	}

	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();

		// Check all bean definitions.
		// ♻️ 遍历 BeanDefinition 集合
		for (String beanName : this.beanDefinitionNames) {
			// Only consider bean as eligible if the bean name is not defined as alias for some other bean.
			if (!isAlias(beanName)) {
				try {
					// 从 mergedBeanDefinitions 中获取 BD
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

					// Only check bean definition if it is complete.
					// mbd 不是抽象的 且 allowEagerInit 为 true，则直接去推测 mbd 的类型，并进行匹配
					// 如果 allowEagerInit 为 false：如果 mbd 还没有加载类，并且是懒加载的，并且不允许提前加载类，且不需要提前初始化
					// 如果 allowEagerInit 为 false：如果 mbd 已经加载类了，并且不是懒加载，并且允许提前加载类，且不需要提前初始化
					if (!mbd.isAbstract() && (allowEagerInit ||
							(mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) &&
									!requiresEagerInitForType(mbd.getFactoryBeanName()))) {

						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
						boolean matchFound = false;
						boolean allowFactoryBeanInit = (allowEagerInit || containsSingleton(beanName));
						boolean isNonLazyDecorated = (dbd != null && !mbd.isLazyInit());

						// 普通 Bean 的情况
						if (!isFactoryBean) {
							// 包含所有 bean 或 beanName 是一个单例 bean
							if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
								// ⭐️ 类型匹配
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
						// FactoryBean 的情况
						else {
							if (includeNonSingletons || isNonLazyDecorated ||
									(allowFactoryBeanInit && isSingleton(beanName, mbd, dbd))) {
								// 类型匹配
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
							if (!matchFound) {
								// In case of FactoryBean, try to match FactoryBean instance itself next.
								beanName = FACTORY_BEAN_PREFIX + beanName;
								// 类型匹配
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably a placeholder: let's ignore it for type matching purposes.
					LogMessage message = (ex instanceof CannotLoadBeanClassException ?
							LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
							LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName));
					logger.trace(message, ex);
					// Register exception, in case the bean was accidentally unresolvable.
					onSuppressedException(ex);
				}
				catch (NoSuchBeanDefinitionException ex) {
					// Bean definition got removed while we were iterating -> ignore.
				}
			}
		}

		// Check manually registered singletons too.
		for (String beanName : this.manualSingletonNames) {
			try {
				// In case of FactoryBean, match object created by FactoryBean.
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Shouldn't happen - probably a result of circular reference resolution...
				logger.trace(LogMessage.format(
						"Failed to check manually registered singleton with name '%s'", beanName), ex);
			}
		}

		return StringUtils.toStringArray(result);
	}

	private boolean isSingleton(String beanName, RootBeanDefinition mbd, @Nullable BeanDefinitionHolder dbd) {
		return (dbd != null ? mbd.isSingleton() : isSingleton(beanName));
	}

	/**
	 * Check whether the specified bean would need to be eagerly initialized
	 * in order to determine its type.
	 * @param factoryBeanName a factory-bean reference that the bean definition
	 * defines a factory method for
	 * @return whether eager initialization is necessary
	 */
	private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
		return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfType(
			@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {

		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			try {
				Object beanInstance = getBean(beanName);
				if (!(beanInstance instanceof NullBean)) {
					result.put(beanName, (T) beanInstance);
				}
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					String exBeanName = bce.getBeanName();
					if (exBeanName != null && isCurrentlyInCreation(exBeanName)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Ignoring match to currently created bean '" + exBeanName + "': " +
									ex.getMessage());
						}
						onSuppressedException(ex);
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		List<String> result = new ArrayList<>();
		for (String beanName : this.beanDefinitionNames) {
			BeanDefinition bd = this.beanDefinitionMap.get(beanName);
			if (bd != null && !bd.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		for (String beanName : this.manualSingletonNames) {
			if (!result.contains(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		return StringUtils.toStringArray(result);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		String[] beanNames = getBeanNamesForAnnotation(annotationType);
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			Object beanInstance = getBean(beanName);
			if (!(beanInstance instanceof NullBean)) {
				result.put(beanName, beanInstance);
			}
		}
		return result;
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {

		return findMergedAnnotationOnBean(beanName, annotationType)
				.synthesize(MergedAnnotation::isPresent).orElse(null);
	}

	private <A extends Annotation> MergedAnnotation<A> findMergedAnnotationOnBean(
			String beanName, Class<A> annotationType) {

		Class<?> beanType = getType(beanName);
		if (beanType != null) {
			MergedAnnotation<A> annotation =
					MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		if (containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			// Check raw bean class, e.g. in case of a proxy.
			if (bd.hasBeanClass()) {
				Class<?> beanClass = bd.getBeanClass();
				if (beanClass != beanType) {
					MergedAnnotation<A> annotation =
							MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
					if (annotation.isPresent()) {
						return annotation;
					}
				}
			}
			// Check annotations declared on factory method, if any.
			Method factoryMethod = bd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				MergedAnnotation<A> annotation =
						MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
				if (annotation.isPresent()) {
					return annotation;
				}
			}
		}
		return MergedAnnotation.missing();
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
		Assert.notNull(dependencyType, "Dependency type must not be null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
				throw new IllegalArgumentException("Value [" + autowiredValue +
						"] does not implement specified dependency type [" + dependencyType.getName() + "]");
			}
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}

	@Override
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {

		return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * @param beanName the name of the bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate(
			String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver)
			throws NoSuchBeanDefinitionException {

		String bdName = BeanFactoryUtils.transformedBeanName(beanName);
		if (containsBeanDefinition(bdName)) {
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(bdName), descriptor, resolver);
		}
		else if (containsSingleton(beanName)) {
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
		}

		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((DefaultListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor, resolver);
		}
		else if (parent instanceof ConfigurableListableBeanFactory) {
			// If no DefaultListableBeanFactory, can't pass the resolver along.
			return ((ConfigurableListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor);
		}
		else {
			return true;
		}
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * @param beanName the name of the bean definition to check
	 * @param mbd the merged bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {

		String bdName = BeanFactoryUtils.transformedBeanName(beanName);
		resolveBeanClass(mbd, bdName);
		if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		BeanDefinitionHolder holder = (beanName.equals(bdName) ?
				this.mergedBeanDefinitionHolders.computeIfAbsent(beanName,
						key -> new BeanDefinitionHolder(mbd, beanName, getAliases(bdName))) :
				new BeanDefinitionHolder(mbd, beanName, getAliases(bdName)));
		// ⭐️ 实际调用的是：QualifierAnnotationAutowireCandidateResolver 的 isAutowireCandidate
		// QualifierAnnotationAutowireCandidateResolver 是一个责任链模式，会依次调用父类的 isAutowireCandidate 方法
		return resolver.isAutowireCandidate(holder, descriptor);
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<>();
		iterator.add(this.beanDefinitionNames.iterator());
		iterator.add(this.manualSingletonNames.iterator());
		return iterator;
	}

	@Override
	protected void clearMergedBeanDefinition(String beanName) {
		super.clearMergedBeanDefinition(beanName);
		this.mergedBeanDefinitionHolders.remove(beanName);
	}

	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		this.mergedBeanDefinitionHolders.clear();
		clearByTypeCache();
	}

	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	/**
	 * Considers all beans as eligible for metadata caching
	 * if the factory's configuration has been marked as frozen.
	 * @see #freezeConfiguration()
	 */
	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
	}

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		// 从容器中获取所有 beanDefinition 的名称，这些名字都是通过 ClassPathBeanDefinitionScanner 的 scan 方法加入进来的
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		// 循环所有非懒加载的单例 bean
		for (String beanName : beanNames) {

			// ⭐️ 合并后的 beanDefinition，用于父子 bean 的关系
			// 此时的 BD 属性是最完整的，并且存放在 mergedBeanDefinitions 中
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);

			// beanDefinition 不能是抽象的，必须是单例与非懒加载的
			// bd.isAbstract()，并不是抽象类的概念，指的指抽象 BD
			// 抽象的 BD，通常在 xml 中配置，可用于其他 BD 的继承，如果子 BD 有同名属性，则不覆盖
			// <bean id="abstractPrototypeBD" scope="prototype" abstract="true" />
			// <bean id="userService" class="xx.xx.UserService", parent="abstractPrototypeBD" />
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {

				// 是工厂 bean
				if (isFactoryBean(beanName)) {
					// 如果是工厂 bean，则加 & 前缀，获取的时 FactoryBean 的对象
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						FactoryBean<?> factory = (FactoryBean<?>) bean;
						boolean isEagerInit;
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged(
									(PrivilegedAction<Boolean>) ((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							// 是否实现了 SmartFactoryBean 接口，并 isEagerInit 方法返回 true
							// isEagerInit 提前调用
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							// 真正的调用 factoryBean 的 getObject 方法
							getBean(beanName);
						}
					}
				}
				// 不是工厂 bean，即：非懒加载的单例 bean
				else {
					// ⭐️ 开始实例化 bean
					getBean(beanName);
				}
			}
		}

		// 💡 到这里所有的非懒加载的单例 bean 都已经创建完毕了
		// Trigger post-initialization callback for all applicable beans...
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			// 调用实现了 SmartInitializingSingleton 接口的 bean 的 afterSingletonsInstantiated 方法
			// 其中 EventListenerMethodProcessor 就实现了 SmartInitializingSingleton，会在这里进行调用
			// ⭐ 调用时机是：所有非懒加载的单例 bean 都已经创建完毕了
			if (singletonInstance instanceof SmartInitializingSingleton) {
				StartupStep smartInitialize = this.getApplicationStartup().start("spring.beans.smart-initialize")
						.tag("beanName", beanName);
				SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext());
				}
				else {
					// 调用：afterSingletonsInstantiated 方法
					smartSingleton.afterSingletonsInstantiated();
				}
				smartInitialize.end();
			}
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry interface
	//---------------------------------------------------------------------

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		// ⭐️ 出现重复
		if (existingDefinition != null) {
			// 是否允许覆盖，默认是允许的
			if (!isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			else if (existingDefinition.getRole() < beanDefinition.getRole()) {
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				if (logger.isInfoEnabled()) {
					logger.info("Overriding user-defined bean definition for bean '" + beanName +
							"' with a framework-generated bean definition: replacing [" +
							existingDefinition + "] with [" + beanDefinition + "]");
				}
			}
			else if (!beanDefinition.equals(existingDefinition)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Overriding bean definition for bean '" + beanName +
							"' with a different definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Overriding bean definition for bean '" + beanName +
							"' with an equivalent definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			// ⭐️ 后者覆盖前者
			// 因为 @Bean 是后解析的，而 @Component 是先解析的
			// 所以 @Bean 和 @Component 名称相同的话，@Bean 会覆盖 @Component 定义的 BD
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
		else {
			if (hasBeanCreationStarted()) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
				synchronized (this.beanDefinitionMap) {
					this.beanDefinitionMap.put(beanName, beanDefinition);
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					removeManualSingletonName(beanName);
				}
			}
			else {
				// Still in startup registration phase
				// ⭐️ 加入到 beanDefinitionMap 中
				this.beanDefinitionMap.put(beanName, beanDefinition);
				// 同时给 beanDefinitionNames 赋值
				this.beanDefinitionNames.add(beanName);
				removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}

		if (existingDefinition != null || containsSingleton(beanName)) {
			resetBeanDefinition(beanName);
		}
		else if (isConfigurationFrozen()) {
			clearByTypeCache();
		}
	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}

		if (hasBeanCreationStarted()) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
				updatedDefinitions.remove(beanName);
				this.beanDefinitionNames = updatedDefinitions;
			}
		}
		else {
			// Still in startup registration phase
			this.beanDefinitionNames.remove(beanName);
		}
		this.frozenBeanDefinitionNames = null;

		resetBeanDefinition(beanName);
	}

	/**
	 * Reset all bean definition caches for the given bean,
	 * including the caches of beans that are derived from it.
	 * <p>Called after an existing bean definition has been replaced or removed,
	 * triggering {@link #clearMergedBeanDefinition}, {@link #destroySingleton}
	 * and {@link MergedBeanDefinitionPostProcessor#resetBeanDefinition} on the
	 * given bean and on all bean definitions that have the given bean as parent.
	 * @param beanName the name of the bean to reset
	 * @see #registerBeanDefinition
	 * @see #removeBeanDefinition
	 */
	protected void resetBeanDefinition(String beanName) {
		// Remove the merged bean definition for the given bean, if already created.
		clearMergedBeanDefinition(beanName);

		// Remove corresponding bean from singleton cache, if any. Shouldn't usually
		// be necessary, rather just meant for overriding a context's default beans
		// (e.g. the default StaticMessageSource in a StaticApplicationContext).
		destroySingleton(beanName);

		// Notify all post-processors that the specified bean definition has been reset.
		for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
			processor.resetBeanDefinition(beanName);
		}

		// Reset all bean definitions that have the given bean as parent (recursively).
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				// Ensure bd is non-null due to potential concurrent modification of beanDefinitionMap.
				if (bd != null && beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}

	/**
	 * Only allows alias overriding if bean definition overriding is allowed.
	 */
	@Override
	protected boolean allowAliasOverriding() {
		return isAllowBeanDefinitionOverriding();
	}

	/**
	 * Also checks for an alias overriding a bean definition of the same name.
	 */
	@Override
	protected void checkForAliasCircle(String name, String alias) {
		super.checkForAliasCircle(name, alias);
		if (!isAllowBeanDefinitionOverriding() && containsBeanDefinition(alias)) {
			throw new IllegalStateException("Cannot register alias '" + alias +
					"' for name '" + name + "': Alias would override bean definition '" + alias + "'");
		}
	}

	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);
		updateManualSingletonNames(set -> set.add(beanName), set -> !this.beanDefinitionMap.containsKey(beanName));
		clearByTypeCache();
	}

	@Override
	public void destroySingletons() {
		super.destroySingletons();
		// 清空 manualSingletonNames 集合
		updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
		clearByTypeCache();
	}

	@Override
	public void destroySingleton(String beanName) {
		super.destroySingleton(beanName);
		removeManualSingletonName(beanName);
		clearByTypeCache();
	}

	private void removeManualSingletonName(String beanName) {
		updateManualSingletonNames(set -> set.remove(beanName), set -> set.contains(beanName));
	}

	/**
	 * Update the factory's internal set of manual singleton names.
	 * @param action the modification action
	 * @param condition a precondition for the modification action
	 * (if this condition does not apply, the action can be skipped)
	 */
	private void updateManualSingletonNames(Consumer<Set<String>> action, Predicate<Set<String>> condition) {
		if (hasBeanCreationStarted()) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized (this.beanDefinitionMap) {
				if (condition.test(this.manualSingletonNames)) {
					Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
					action.accept(updatedSingletons);
					this.manualSingletonNames = updatedSingletons;
				}
			}
		}
		else {
			// Still in startup registration phase
			if (condition.test(this.manualSingletonNames)) {
				action.accept(this.manualSingletonNames);
			}
		}
	}

	/**
	 * Remove any assumptions about by-type mappings.
	 */
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}


	//---------------------------------------------------------------------
	// Dependency resolution functionality
	//---------------------------------------------------------------------

	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		NamedBeanHolder<T> namedBean = resolveNamedBean(ResolvableType.forRawClass(requiredType), null, false);
		if (namedBean != null) {
			return namedBean;
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof AutowireCapableBeanFactory) {
			return ((AutowireCapableBeanFactory) parent).resolveNamedBean(requiredType);
		}
		throw new NoSuchBeanDefinitionException(requiredType);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(
			ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {

		Assert.notNull(requiredType, "Required type must not be null");
		String[] candidateNames = getBeanNamesForType(requiredType);

		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = StringUtils.toStringArray(autowireCandidates);
			}
		}

		if (candidateNames.length == 1) {
			return resolveNamedBean(candidateNames[0], requiredType, args);
		}
		else if (candidateNames.length > 1) {
			Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.length);
			for (String beanName : candidateNames) {
				if (containsSingleton(beanName) && args == null) {
					Object beanInstance = getBean(beanName);
					candidates.put(beanName, (beanInstance instanceof NullBean ? null : beanInstance));
				}
				else {
					candidates.put(beanName, getType(beanName));
				}
			}
			String candidateName = determinePrimaryCandidate(candidates, requiredType.toClass());
			if (candidateName == null) {
				candidateName = determineHighestPriorityCandidate(candidates, requiredType.toClass());
			}
			if (candidateName != null) {
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance == null) {
					return null;
				}
				if (beanInstance instanceof Class) {
					return resolveNamedBean(candidateName, requiredType, args);
				}
				return new NamedBeanHolder<>(candidateName, (T) beanInstance);
			}
			if (!nonUniqueAsNull) {
				throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
			}
		}

		return null;
	}

	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(
			String beanName, ResolvableType requiredType, @Nullable Object[] args) throws BeansException {

		Object bean = getBean(beanName, null, args);
		if (bean instanceof NullBean) {
			return null;
		}
		return new NamedBeanHolder<T>(beanName, adaptBeanInstance(beanName, bean, requiredType.toClass()));
	}

	@Override
	@Nullable
	public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		// ⭐️ 初始化参数名称发现器，用来获取字段的名字或方法入惨的名字
		// 因为 JDK 1.7 不支持反射获取方法的参数名称，JDK 1.8 虽然支持，但是需要加 -parameters 参数
		// 所以 Spring 使用了两个参数名称发现器
		// 1. 基于反射的参数名称发现器：StandardReflectionParameterNameDiscoverer
		// 2. 基于本地变量表的参数名称发现器：LocalVariableTableParameterNameDiscoverer
		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());

		// 所需要的类型是 Optional
		if (Optional.class == descriptor.getDependencyType()) {
			return createOptionalDependency(descriptor, requestingBeanName);
		}
		// 所需要的类型是 ObjectFactory 或 ObjectProvider
		else if (ObjectFactory.class == descriptor.getDependencyType() ||
				ObjectProvider.class == descriptor.getDependencyType()) {
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		}
		// 所需要的类型是 Provider
		else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
			return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
		}
		// 其他类型
		else {
			// ⭐️ 在属性上或者 setter 方法的参数前面是否使用了 @Lazy 注解
			// 如果有，那么构造一个 result 代理对象并返回，真正使用的时候才会使用代理对象进行类型筛选 bean
			// 实现类：ContextAnnotationAutowireCandidateResolver
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor, requestingBeanName);
			// ⭐️ 没有 @Lazy 注解
			if (result == null) {
				// ⭐ 开始解析依赖
				// descriptor 表示一个属性或者一个 setter 方法
				// requestingBeanName 表示正在依赖注入的 bean
				result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
			}
			return result;
		}
	}

	@Nullable
	public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			// 缓存：如果当前的 descriptor 做过依赖注入了，则可以直接取 shortcut 了，相当于缓存
			// ShortcutDependencyDescriptor#resolveShortcut
			// 即从 beanFactory.getBean(this.shortcut, this.requiredType);
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}

			// 获取类型，可能是字段的类型，也可能是方法参数的类型
			Class<?> type = descriptor.getDependencyType();

			// ⭐️ 处理 @Value 注解
			// 实现类：QualifierAnnotationAutowireCandidateResolver
			Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				if (value instanceof String) {
					// 占位符的填充：${xxx}
					// 其实就是去 Environment 环境变量中寻找值
					String strVal = resolveEmbeddedValue((String) value);
					BeanDefinition bd = (beanName != null && containsBean(beanName) ?
							getMergedBeanDefinition(beanName) : null);
					// SpEl 解析：#{xxx}
					value = evaluateBeanDefinitionString(strVal, bd);
				}

				// 利用类型转换器，将 value 转化为 descriptor 所对应的类型
				TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
				try {
					return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
				}
				catch (UnsupportedOperationException ex) {
					// A custom TypeConverter which does not support TypeDescriptor resolution...
					return (descriptor.getField() != null ?
							converter.convertIfNecessary(value, type, descriptor.getField()) :
							converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
				}
			}

			// ⭐️ 是否是需要注入多个 bean 到某个字段或方法参数中
			// 如果 descriptor 所对应的类型是数组、List、Map 这些类型，则直接将 descriptor 对应的类型所匹配的所有 bean 加入
			// ⌨️ @Autowired
			// ⌨️ Map<String, OrderService> orderServices; key 就是 beanName，value 就是 bean 对象
			// 或
			// ⌨️ @Autowired
			// ⌨️ List<OrderService> orderServices;
			Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
			if (multipleBeans != null) {
				return multipleBeans;
			}

			// ⭐️⭐️⭐️ 【筛选阶段一】：表示就是一个普通的注入点，找到所有匹配上的 beans
			// 找到所有的 bean，key 是 beanName，value 有可能是 bean 对象，也有可能是 beanClass
			// 因为：如果单例池已经存在该 bean 对象，则 Object 就是 bean 的对象，如果还不存在，则可能并不需要去创建对象，或者说创建了也不一定会用到
			// 因为不一定需要 bean 才能判断，beanClass 也可以判断出某个 bean 是否符合要求
			// 所以：Object 可能是 bean 对象，也可能是 beanClass
			// 何谓符合条件：
			// 1. BD 的 autowireCandidate 属性为 ture：@Bean(autowireCandidate = true)
			// 2. 如果存在范型，那么能找到合适的实际类型
			// 3. 如果存在@Qualifier注解，取注解的值，然后在符合条件的 bean 找对应的@Qualifier注解
			// 4. 如果存在多个符合条件的 bean，判断@Primary注解
			// 5. 如果存在多个符合条件的 bean，对@Priority进行排序，最小的值，优先级最高
			// 6. 比较 beanName 是否和属性名、或方法参数名称一致
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				// 没有找到，并且 required 为 true，抛异常
				if (isRequired(descriptor)) {
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				return null;
			}

			// 需要注入的 beanName
			String autowiredBeanName;
			// 候选 bean 实例
			Object instanceCandidate;

			// ⭐️ 根据类型找到了多个符合的 bean
			if (matchingBeans.size() > 1) {
				// ⭐️ byName 的实现
				// ⭐️ 【筛选阶段二】：此时需要根据 name 继续查找，经过三个判断：@Primary => @Priority => beanName
				// @Primary：如果找到多个 bean，如果没有 @Primary，则返回 null；如果只有一个 @Primary，则直接返回，否则抛异常，
				// @Priority：如果找到多个 bean，如果没有 @Priority，则返回 null；如果只有一个 @Priority，则直接返回，如果相同，则抛异常，否则返回数值最小的
				// beanName：比较 beanName 是否和候选 bean 的名称一致，没有返回 null
				autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (autowiredBeanName == null) {
					// required 为 true，则抛异常：NoUniqueBeanDefinitionException
					if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
						return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
					}
					// required 为 false，则注入 null 值
					else {
						// In case of an optional Collection/Map, silently ignore a non-unique case:
						// possibly it was meant to be an empty collection of multiple regular beans
						// (before 4.3 in particular when we didn't even look for collection beans).
						return null;
					}
				}
				// 找到了符合条件的 bean
				instanceCandidate = matchingBeans.get(autowiredBeanName);
			}
			// ⭐️ 只有一个符合条件的 bean 的情况
			else {
				// We have exactly one match.
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				autowiredBeanName = entry.getKey();
				instanceCandidate = entry.getValue();
			}

			// 加入缓存
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(autowiredBeanName);
			}

			// ⭐️ 此处拿到的 instanceCandidate 可能是 class，那么就需要进行实例化，调用 getBean 方法
			if (instanceCandidate instanceof Class) {
				instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
			}

			Object result = instanceCandidate;
			// NullBean 的情况，就是 @Bean 返回的是 null，Spring 会将 null 封装为 NullBean 对象
			if (result instanceof NullBean) {
				if (isRequired(descriptor)) {
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				result = null;
			}
			if (!ClassUtils.isAssignableValue(type, result)) {
				throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
			}

			// 返回 bean 对象
			return result;
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	@Nullable
	private Object resolveMultipleBeans(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		Class<?> type = descriptor.getDependencyType();

		if (descriptor instanceof StreamDependencyDescriptor) {
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			Stream<Object> stream = matchingBeans.keySet().stream()
					.map(name -> descriptor.resolveCandidate(name, type, this))
					.filter(bean -> !(bean instanceof NullBean));
			if (((StreamDependencyDescriptor) descriptor).isOrdered()) {
				stream = stream.sorted(adaptOrderComparator(matchingBeans));
			}
			return stream;
		}
		// ⭐️ 数组类型
		else if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			ResolvableType resolvableType = descriptor.getResolvableType();
			Class<?> resolvedArrayType = resolvableType.resolve(type);
			if (resolvedArrayType != type) {
				componentType = resolvableType.getComponentType().resolve();
			}
			// 解析 componentType 的类型
			if (componentType == null) {
				return null;
			}
			// ⭐️ 核心方法：根据 componentType 类型，去容器中找 bean
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 是否需要使用类型转换器进行转换
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			// matchingBeans.values() 获取所有的 beans
			Object result = converter.convertIfNecessary(matchingBeans.values(), resolvedArrayType);
			if (result instanceof Object[]) {
				Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					// 排序
					Arrays.sort((Object[]) result, comparator);
				}
			}
			return result;
		}
		// ⭐️ 集合类型
		else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			// 解析 elementType 的类型
			Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
			if (elementType == null) {
				return null;
			}
			// ⭐️ 核心方法：根据 elementType 类型，去容器中找 bean
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 是否需要使用类型转换器进行转换
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			// matchingBeans.values() 获取所有的 beans
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			if (result instanceof List) {
				if (((List<?>) result).size() > 1) {
					Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
					if (comparator != null) {
						// 排序
						((List<?>) result).sort(comparator);
					}
				}
			}
			return result;
		}
		// ⭐️ Map 类型
		else if (Map.class == type) {
			ResolvableType mapType = descriptor.getResolvableType().asMap();
			Class<?> keyType = mapType.resolveGeneric(0);
			// key 的类型必须是 String
			if (String.class != keyType) {
				return null;
			}
			// 解析 valueType 的类型
			Class<?> valueType = mapType.resolveGeneric(1);
			if (valueType == null) {
				return null;
			}
			// ⭐️ 核心方法：根据 valueType 类型，去容器中找 bean
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		}
		else {
			return null;
		}
	}

	private boolean isRequired(DependencyDescriptor descriptor) {
		return getAutowireCandidateResolver().isRequired(descriptor);
	}

	private boolean indicatesMultipleBeans(Class<?> type) {
		return (type.isArray() || (type.isInterface() &&
				(Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))));
	}

	@Nullable
	private Comparator<Object> adaptDependencyComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator).withSourceProvider(
					createFactoryAwareOrderSourceProvider(matchingBeans));
		}
		else {
			return comparator;
		}
	}

	private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> dependencyComparator = getDependencyComparator();
		OrderComparator comparator = (dependencyComparator instanceof OrderComparator ?
				(OrderComparator) dependencyComparator : OrderComparator.INSTANCE);
		return comparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
	}

	private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
		beans.forEach((beanName, instance) -> instancesToBeanNames.put(instance, beanName));
		return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
	}

	/**
	 * Find bean instances that match the required type.
	 * Called during autowiring for the specified bean.
	 * @param beanName the name of the bean that is about to be wired
	 * @param requiredType the actual type of bean to look for
	 * (may be an array component type or collection element type)
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return a Map of candidate names and candidate instances that match
	 * the required type (never {@code null})
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected Map<String, Object> findAutowireCandidates(
			@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

		// ⭐️⭐️⭐️ 根据类型找到所有的 beanNames
		// 从 BeanFactory 中找出和 requiredType 所匹配的 beanNames
		// 📝 仅仅返回的是 beanName，这些 bean 不一定经过了实例化，只有到最终确认了某个 bean 就是候选 bean 的时候，才会进行实例化
		// 其中：includeNonSingletons 表示是否包含非单例的 bean，这里传入的是 true，表示包含所有的 bean
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager());

		// 创建一个 Map，用于存储匹配的 bean
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(candidateNames.length);

		// ⭐️ 根据类型从 resolvableDependencies 中匹配 bean
		// resolvableDependencies 的 key 是类型，value 是对象，如：[{BeanFactory.class: beanFactory}, {ApplicationContext.class: applicationContext}]
		// resolvableDependencies 是在 beanFactory 初始化的时候添加的：AbstractApplicationContext#prepareBeanFactory
		// Spring 启动的时候，就会在容器中，默认添加几个对象，程序员也可以手动获取 beanFactory，通过 registerResolvableDependency 进行注册
		for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
			Class<?> autowiringType = classObjectEntry.getKey();
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = classObjectEntry.getValue();
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}

		// ⭐️ 不是自己注入自己，先考虑别人
		for (String candidate : candidateNames) {
			// 这种情况，通常是自己注入自己，然后找到了多个符合条件的 bean，只是名称不同
			// 此时优先使用另一个，打破了 byName 根据名称注入的逻辑
			// ⭐️ 核心方法：isAutowireCandidate 表示是否可以进行依赖注入
			// @Bean(autowireCandidate = true)：表示可以进行依赖注入，默认就是 true
			// 如果有两个 bean，一个是 true，一个是 false，那么肯定是用 autowireCandidate = true 的哪一个
			if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
				addCandidateEntry(result, candidate, descriptor, requiredType);
			}
		}

		// ⭐️ result 为空的情况：
		// 1. 要么是真的没有找到匹配的
		// 2. 要么是匹配的自己（没有找到别人）UserService 注入了 @Autowired UserService userService;
		if (result.isEmpty()) {
			// 需要匹配的类似是否是 Map、List 或 Array
			boolean multiple = indicatesMultipleBeans(requiredType);
			// Consider fallback matches if the first pass failed to find anything...
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for (String candidate : candidateNames) {
				if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor) &&
						(!multiple || getAutowireCandidateResolver().hasQualifier(descriptor))) {
					addCandidateEntry(result, candidate, descriptor, requiredType);
				}
			}
			if (result.isEmpty() && !multiple) {
				// Consider self references as a final pass...
				// but in the case of a dependency collection, not the very same bean itself.
				for (String candidate : candidateNames) {
					// 是否是自己 && 可以进行依赖注入
					// ⭐️ 核心方法：isAutowireCandidate 表示是否可以进行依赖注入
					if (isSelfReference(beanName, candidate) &&
							(!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) &&
							isAutowireCandidate(candidate, fallbackDescriptor)) {
						addCandidateEntry(result, candidate, descriptor, requiredType);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Add an entry to the candidate map: a bean instance if available or just the resolved
	 * type, preventing early bean initialization ahead of primary candidate selection.
	 */
	private void addCandidateEntry(Map<String, Object> candidates, String candidateName,
			DependencyDescriptor descriptor, Class<?> requiredType) {

		if (descriptor instanceof MultiElementDescriptor) {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			if (!(beanInstance instanceof NullBean)) {
				candidates.put(candidateName, beanInstance);
			}
		}
		else if (containsSingleton(candidateName) || (descriptor instanceof StreamDependencyDescriptor &&
				((StreamDependencyDescriptor) descriptor).isOrdered())) {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			candidates.put(candidateName, (beanInstance instanceof NullBean ? null : beanInstance));
		}
		else {
			candidates.put(candidateName, getType(candidateName));
		}
	}

	/**
	 * Determine the autowire candidate in the given set of beans.
	 * <p>Looks for {@code @Primary} and {@code @Priority} (in that order).
	 * @param candidates a Map of candidate names and candidate instances
	 * that match the required type, as returned by {@link #findAutowireCandidates}
	 * @param descriptor the target dependency to match against
	 * @return the name of the autowire candidate, or {@code null} if none found
	 */
	@Nullable
	protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		Class<?> requiredType = descriptor.getDependencyType();

		// ⭐️ 检查是否有 @Primary 注解的 bean
		// 如果存在多个 @Primary 注解，则抛出异常
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			// 直接返回，不会在进行下面的判断
			return primaryCandidate;
		}

		// ⭐️ 取 @Priority 优先级最高的 bean
		// @Priority 是 javax 提供的，只能作用在类上，看是类上否有 @Priority 注解
		// 通常是某个 bean 实现了相同的接口，可以用 @Priority 表示优先级，越小越高
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			// 直接返回，不会在进行下面的判断
			return priorityCandidate;
		}

		// Fallback
		// ⭐️ 兜底：byName 的实现
		// 将 descriptor 的名字 与 candidates 的名字进行匹配，要么是字段的名字，要么是 setter 方法入参的名字
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			// resolvableDependencies 记录了某个类型对应的某个 bean
			// matchesBeanName：匹配当前查找到的 bean 的名字和 descriptor 中属性的名字是否相同
			if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) ||
					matchesBeanName(candidateName, descriptor.getDependencyName())) {
				return candidateName;
			}
		}

		// 没有名字符合
		return null;
	}

	/**
	 * Determine the primary candidate in the given set of beans.
	 * @param candidates a Map of candidate names and candidate instances
	 * (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the primary candidate, or {@code null} if none found
	 * @see #isPrimary(String, Object)
	 */
	@Nullable
	protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		String primaryBeanName = null;
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			// 检查是否有 @Primary 注解
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					// 同一类型存在多个 @Primary 注解，抛异常
					if (candidateLocal && primaryLocal) {
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
								"more than one 'primary' bean found among candidates: " + candidates.keySet());
					}
					else if (candidateLocal) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					// 第一次会记录候选 bean 为 primaryBean
					primaryBeanName = candidateBeanName;
				}
			}
		}
		return primaryBeanName;
	}

	/**
	 * Determine the candidate with the highest priority in the given set of beans.
	 * <p>Based on {@code @javax.annotation.Priority}. As defined by the related
	 * {@link org.springframework.core.Ordered} interface, the lowest value has
	 * the highest priority.
	 * @param candidates a Map of candidate names and candidate instances
	 * (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the candidate with the highest priority,
	 * or {@code null} if none found
	 * @see #getPriority(Object)
	 */
	@Nullable
	protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		// 最高优先级的 beanName
		String highestPriorityBeanName = null;
		// 最高优先级的 priority（越小优先级越高）
		Integer highestPriority = null;

		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (beanInstance != null) {
				Integer candidatePriority = getPriority(beanInstance);
				if (candidatePriority != null) {
					if (highestPriorityBeanName != null) {
						// 优先级相同，则抛异常
						if (candidatePriority.equals(highestPriority)) {
							throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
									"Multiple beans found with the same priority ('" + highestPriority +
									"') among candidates: " + candidates.keySet());
						}
						// 如果当前 priority 小于最高优先级，则更新
						else if (candidatePriority < highestPriority) {
							highestPriorityBeanName = candidateBeanName;
							highestPriority = candidatePriority;
						}
					}
					else {
						// 第一次循环，会将当前 bean 和 priority 最高最高优先级
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				}
			}
		}
		return highestPriorityBeanName;
	}

	/**
	 * Return whether the bean definition for the given bean name has been
	 * marked as a primary bean.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance (can be null)
	 * @return whether the given bean qualifies as primary
	 */
	protected boolean isPrimary(String beanName, Object beanInstance) {
		String transformedBeanName = transformedBeanName(beanName);
		if (containsBeanDefinition(transformedBeanName)) {
			return getMergedLocalBeanDefinition(transformedBeanName).isPrimary();
		}
		BeanFactory parent = getParentBeanFactory();
		return (parent instanceof DefaultListableBeanFactory &&
				((DefaultListableBeanFactory) parent).isPrimary(transformedBeanName, beanInstance));
	}

	/**
	 * Return the priority assigned for the given bean instance by
	 * the {@code javax.annotation.Priority} annotation.
	 * <p>The default implementation delegates to the specified
	 * {@link #setDependencyComparator dependency comparator}, checking its
	 * {@link OrderComparator#getPriority method} if it is an extension of
	 * Spring's common {@link OrderComparator} - typically, an
	 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}.
	 * If no such comparator is present, this implementation returns {@code null}.
	 * @param beanInstance the bean instance to check (can be {@code null})
	 * @return the priority assigned to that bean or {@code null} if none is set
	 */
	@Nullable
	protected Integer getPriority(Object beanInstance) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator).getPriority(beanInstance);
		}
		return null;
	}

	/**
	 * Determine whether the given candidate name matches the bean name or the aliases
	 * stored in this bean definition.
	 */
	protected boolean matchesBeanName(String beanName, @Nullable String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), candidateName)));
	}

	/**
	 * Determine whether the given beanName/candidateName pair indicates a self reference,
	 * i.e. whether the candidate points back to the original bean or to a factory method
	 * on the original bean.
	 */
	private boolean isSelfReference(@Nullable String beanName, @Nullable String candidateName) {
		return (beanName != null && candidateName != null &&
				(beanName.equals(candidateName) || (containsBeanDefinition(candidateName) &&
						beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
	}

	/**
	 * Raise a NoSuchBeanDefinitionException or BeanNotOfRequiredTypeException
	 * for an unresolvable dependency.
	 */
	private void raiseNoMatchingBeanFound(
			Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor) throws BeansException {

		checkBeanNotOfRequiredType(type, descriptor);

		throw new NoSuchBeanDefinitionException(resolvableType,
				"expected at least 1 bean which qualifies as autowire candidate. " +
				"Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	/**
	 * Raise a BeanNotOfRequiredTypeException for an unresolvable dependency, if applicable,
	 * i.e. if the target type of the bean would match but an exposed proxy doesn't.
	 */
	private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
		for (String beanName : this.beanDefinitionNames) {
			try {
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				Class<?> targetType = mbd.getTargetType();
				if (targetType != null && type.isAssignableFrom(targetType) &&
						isAutowireCandidate(beanName, mbd, descriptor, getAutowireCandidateResolver())) {
					// Probably a proxy interfering with target type match -> throw meaningful exception.
					Object beanInstance = getSingleton(beanName, false);
					Class<?> beanType = (beanInstance != null && beanInstance.getClass() != NullBean.class ?
							beanInstance.getClass() : predictBeanType(beanName, mbd));
					if (beanType != null && !type.isAssignableFrom(beanType)) {
						throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Bean definition got removed while we were iterating -> ignore.
			}
		}

		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) parent).checkBeanNotOfRequiredType(type, descriptor);
		}
	}

	/**
	 * Create an {@link Optional} wrapper for the specified dependency.
	 */
	private Optional<?> createOptionalDependency(
			DependencyDescriptor descriptor, @Nullable String beanName, final Object... args) {

		DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {
			@Override
			public boolean isRequired() {
				return false;
			}
			@Override
			public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
				return (!ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, args) :
						super.resolveCandidate(beanName, requiredType, beanFactory));
			}
		};
		Object result = doResolveDependency(descriptorToUse, beanName, null, null);
		return (result instanceof Optional ? (Optional<?>) result : Optional.ofNullable(result));
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		}
		else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - " +
				"just a SerializedBeanFactoryReference is");
	}

	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		}
		else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}


	/**
	 * Minimal id reference to the factory.
	 * Resolved to the actual factory instance on deserialization.
	 */
	private static class SerializedBeanFactoryReference implements Serializable {

		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get(this.id);
			if (ref != null) {
				Object result = ref.get();
				if (result != null) {
					return result;
				}
			}
			// Lenient fallback: dummy factory in case of original factory not found...
			DefaultListableBeanFactory dummyFactory = new DefaultListableBeanFactory();
			dummyFactory.serializationId = this.id;
			return dummyFactory;
		}
	}


	/**
	 * A dependency descriptor marker for nested elements.
	 */
	private static class NestedDependencyDescriptor extends DependencyDescriptor {

		public NestedDependencyDescriptor(DependencyDescriptor original) {
			super(original);
			increaseNestingLevel();
		}
	}


	/**
	 * A dependency descriptor for a multi-element declaration with nested elements.
	 */
	private static class MultiElementDescriptor extends NestedDependencyDescriptor {

		public MultiElementDescriptor(DependencyDescriptor original) {
			super(original);
		}
	}


	/**
	 * A dependency descriptor marker for stream access to multiple elements.
	 */
	private static class StreamDependencyDescriptor extends DependencyDescriptor {

		private final boolean ordered;

		public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
			super(original);
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}


	private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
	}


	/**
	 * Serializable ObjectFactory/ObjectProvider for lazy resolution of a dependency.
	 */
	private class DependencyObjectProvider implements BeanObjectProvider<Object> {

		private final DependencyDescriptor descriptor;

		private final boolean optional;

		@Nullable
		private final String beanName;

		public DependencyObjectProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			this.descriptor = new NestedDependencyDescriptor(descriptor);
			this.optional = (this.descriptor.getDependencyType() == Optional.class);
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName);
			}
			else {
				Object result = doResolveDependency(this.descriptor, this.beanName, null, null);
				if (result == null) {
					throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
				}
				return result;
			}
		}

		@Override
		public Object getObject(final Object... args) throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName, args);
			}
			else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
					@Override
					public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
						return beanFactory.getBean(beanName, args);
					}
				};
				Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
				if (result == null) {
					throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
				}
				return result;
			}
		}

		@Override
		@Nullable
		public Object getIfAvailable() throws BeansException {
			try {
				if (this.optional) {
					return createOptionalDependency(this.descriptor, this.beanName);
				}
				else {
					DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
						@Override
						public boolean isRequired() {
							return false;
						}
					};
					return doResolveDependency(descriptorToUse, this.beanName, null, null);
				}
			}
			catch (ScopeNotActiveException ex) {
				// Ignore resolved bean in non-active scope
				return null;
			}
		}

		@Override
		public void ifAvailable(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = getIfAvailable();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				}
				catch (ScopeNotActiveException ex) {
					// Ignore resolved bean in non-active scope, even on scoped proxy invocation
				}
			}
		}

		@Override
		@Nullable
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}

				@Override
				@Nullable
				public Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) {
					return null;
				}
			};
			try {
				if (this.optional) {
					return createOptionalDependency(descriptorToUse, this.beanName);
				}
				else {
					return doResolveDependency(descriptorToUse, this.beanName, null, null);
				}
			}
			catch (ScopeNotActiveException ex) {
				// Ignore resolved bean in non-active scope
				return null;
			}
		}

		@Override
		public void ifUnique(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = getIfUnique();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				}
				catch (ScopeNotActiveException ex) {
					// Ignore resolved bean in non-active scope, even on scoped proxy invocation
				}
			}
		}

		@Nullable
		protected Object getValue() throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName);
			}
			else {
				return doResolveDependency(this.descriptor, this.beanName, null, null);
			}
		}

		@Override
		public Stream<Object> stream() {
			return resolveStream(false);
		}

		@Override
		public Stream<Object> orderedStream() {
			return resolveStream(true);
		}

		@SuppressWarnings("unchecked")
		private Stream<Object> resolveStream(boolean ordered) {
			DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
			Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
			return (result instanceof Stream ? (Stream<Object>) result : Stream.of(result));
		}
	}


	/**
	 * Separate inner class for avoiding a hard dependency on the {@code javax.inject} API.
	 * Actual {@code javax.inject.Provider} implementation is nested here in order to make it
	 * invisible for Graal's introspection of DefaultListableBeanFactory's nested classes.
	 */
	private class Jsr330Factory implements Serializable {

		public Object createDependencyProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			return new Jsr330Provider(descriptor, beanName);
		}

		private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {

			public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
				super(descriptor, beanName);
			}

			@Override
			@Nullable
			public Object get() throws BeansException {
				return getValue();
			}
		}
	}


	/**
	 * An {@link org.springframework.core.OrderComparator.OrderSourceProvider} implementation
	 * that is aware of the bean metadata of the instances to sort.
	 * <p>Lookup for the method factory of an instance to sort, if any, and let the
	 * comparator retrieve the {@link org.springframework.core.annotation.Order}
	 * value defined on it. This essentially allows for the following construct:
	 */
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {

		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		@Nullable
		public Object getOrderSource(Object obj) {
			String beanName = this.instancesToBeanNames.get(obj);
			if (beanName == null || !containsBeanDefinition(beanName)) {
				return null;
			}
			RootBeanDefinition beanDefinition = getMergedLocalBeanDefinition(beanName);
			List<Object> sources = new ArrayList<>(2);
			Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				sources.add(factoryMethod);
			}
			Class<?> targetType = beanDefinition.getTargetType();
			if (targetType != null && targetType != obj.getClass()) {
				sources.add(targetType);
			}
			return sources.toArray();
		}
	}

}
