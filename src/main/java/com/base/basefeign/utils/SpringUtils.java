package com.base.basefeign.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Spring工具类
 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 */
@Service
@Lazy(false)
public class SpringUtils implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor, DisposableBean {

	private static ApplicationContext applicationContext = null;

	private static Logger logger = LoggerFactory.getLogger(SpringUtils.class);

	/**
	 * 实例化时自动执行,通常用反射包获取到需要动态创建的接口类，容器初始化时，此方法执行，创建bean
	 * 执行过程与registryBeanWithDymicEdit基本一致
	 * @param beanDefinitionRegistry
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
		logger.info("========");
//		List<Class<?>> beanClazzs = null;//反射获取需要代理的接口的clazz列表
//		for (Class beanClazz : beanClazzs) {
//			BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
//			GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
//			definition.getPropertyValues().add("interfaceClass", beanClazz);
//			definition.getPropertyValues().add("params", "注册传入工厂的参数，一般是properties配置的信息");
//			definition.setBeanClass(InterfaceFactoryBean.class);
//			definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
//			beanDefinitionRegistry.registerBeanDefinition(beanClazz.getSimpleName(), definition);
//		}


	}

	/**
	 * 实例化时自动执行
	 * @param configurableListableBeanFactory
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

	}

	/**
	 * 实现ApplicationContextAware接口, 注入Context到静态变量中.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		logger.debug("注入ApplicationContext到SpringContextHolder:" + applicationContext);

		if (SpringUtils.applicationContext != null) {
			logger.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:"
					+ SpringUtils.applicationContext);
		}
		SpringUtils.applicationContext = applicationContext; //NOSONAR
	}

	/**
	 * 实现DisposableBean接口,在Context关闭时清理静态变量.
	 */
	@Override
	public void destroy() throws Exception {
		SpringUtils.clear();
	}

	/**
	 * 取得存储在静态变量中的ApplicationContext.
	 */
	public static ApplicationContext getApplicationContext() {
		assertContextInjected();
		return applicationContext;
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		assertContextInjected();
		return (T) applicationContext.getBean(name);
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	public static <T> T getBean(Class<T> requiredType) {
		assertContextInjected();
		return applicationContext.getBean(requiredType);
	}

	/**
	 * 清除SpringContextHolder中的ApplicationContext为Null.
	 */
	public static void clear() {
		logger.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
		applicationContext = null;
	}

	/**
	 * 获取当前profiles
	 */
	public static List<String> getActiveProfiles(){
		assertContextInjected();
		String[] activeProfiles = getApplicationContext().getEnvironment().getActiveProfiles();
		return Arrays.asList(activeProfiles);
	}

	/**
	 * 获取配置文件数据
	 */
	public static String getProperty(String key) {
		assertContextInjected();
		return getApplicationContext().getEnvironment().getProperty(key);
	}

	/**
	 * 获取配置文件数据
	 * @param key
	 * @param defaultValue 默认值
	 * @return
	 */
	public static String getProperty(String key, String defaultValue) {
		assertContextInjected();
		return getApplicationContext().getEnvironment().getProperty(key, defaultValue);
	}

	/**
	 * 检查ApplicationContext不为空.
	 */
	protected static void assertContextInjected() {
		if (applicationContext == null) {
			throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		}
	}


	/**
	 * 直接创建bean，不设置属性
	 * @param beanId
	 * @param clazz
	 * @return
	 */
	public static boolean registryBean(String beanId, Class<?> clazz){
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		BeanDefinition definition = builder.getBeanDefinition();
		getRegistry().registerBeanDefinition(beanId, definition);
		return true;
	}
	/**
	 * 为已知的class创建bean，可以设置bean的属性，可以用作动态代理对象的bean扩展
	 * @param beanId
	 * @param
	 * @return
	 */
	public static boolean registryBeanWithEdit(String beanId, Class<?> factoryClazz, Class<?> beanClazz){
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
		GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
		definition.getPropertyValues().add("myClass", beanClazz);
		definition.setBeanClass(factoryClazz);
		definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		getRegistry().registerBeanDefinition(beanId, definition);
		return true;
	}

	/**
	 * 为已知的class创建bean，可以设置bean的属性，可以用作动态代理对象的bean扩展
	 * @param beanId
	 * @param
	 * @return
	 */
	public static boolean registryBeanWithDymicEdit(String beanId, Class<?> factoryClazz, Class<?> beanClazz, String params){
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
		GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
		definition.getPropertyValues().add("interfaceClass", beanClazz);
		definition.getPropertyValues().add("params", params);
		definition.setBeanClass(factoryClazz);
		definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		getRegistry().registerBeanDefinition(beanId, definition);
		return true;
	}

	/**
	 * 获取注册者
	 * context->beanfactory->registry
	 * @return
	 */
	public static BeanDefinitionRegistry getRegistry(){
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
		return (DefaultListableBeanFactory)configurableApplicationContext.getBeanFactory();
	}
}
