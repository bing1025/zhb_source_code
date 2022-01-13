/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.launchscript;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * zhb:就以这里作为启动类的入口开始分析吧
 * 参考资料： https://blog.csdn.net/woshilijiuyi/article/details/82388509
 */

// 前面 分析了springBoot启动流程，大体的轮廓只是冰山一角。
// 今天就来看一下springBoot的亮点功能：自动化装配功能。先从@SpringBootApplication开始
// 首先加载springBoot启动类注入到spring容器中bean map中，看下prepareContext方法中的load方法：
//load(context, sources.toArray(new Object[0]));
//跟进该方法最终会执行BeanDefinitionLoader的load方法
// 得到 我们的启动类就被包装成AnnotatedGenericBeanDefinition了，后续启动类的处理都基于该对象了
// 自动装配的入口：从刷新容器开始： refresh() -- invokeBeanFactoryPostProcessors(beanFactory);

/*首先我们要知道beanFactoryPostProcessor接口是spring的扩展接口，从名字也可以看出，是 beanFactory的扩展接口。
在刷新容器之前，该接口可用来修改bean元数据信息。具体实现方式，我们继续跟着上述执行逻辑便知。
		继续跟进上面invokeBeanFactoryPostProcessors方法，第一行很关键：
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
		一个比较核心的代理类出现了，AbstractApplicationContext委托执行post processors任务的工具类。
		而在项目启动时会委托什么任务呢？
		或许你还记得第一篇博客中介绍的SpringApplication类中applyInitializers(context);方法吧，
		它会将三个默认的内部类加入到 spring 容器DefaultListableBeanFactory中，如下：
		//设置配置警告
		ConfigurationWarningsApplicationContextInitializer$ConfigurationWarningsPostProcessor
		SharedMetadataReaderFactoryContextInitializer$CachingMetadataReaderFactoryPostProcessor
		ConfigFileApplicationListener$PropertySourceOrderingPostProcessor
		来看一下具体任务执行细节，跟进invokeBeanFactoryPostProcessors方法：
来分析一下核心代码：
String[] postProcessorNames =beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
这行代码通过类型BeanDefinitionRegistryPostProcessor获取的处理类名称为：
"org.springframework.context.annotation.internalConfigurationAnnotationProcessor"
而在源码中却搜不到internalConfigurationAnnotationProcessor类，为什么呢？最初看这块代码确实迷惑了半天。
在第一篇博客中，当启动springBoot，创建springBoot容器上下文AnnotationConfigEmbeddedWebApplicationContext时，会装配几个默认bean：
	public AnnotationConfigEmbeddedWebApplicationContext() {
		//在这里装配
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}
继续跟进会执行registerAnnotationConfigProcessors方法
继续跟进会执行registerAnnotationConfigProcessors方法：
public static final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalConfigurationAnnotationProcessor";

	//将 internalConfigurationAnnotationProcessor 对应的类包装成 RootBeanDefinition 加载到容器
	if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
		}
到这里，答案清晰浮现。internalConfigurationAnnotationProcessor为bean名称，容器中真正的类则是ConfigurationClassPostProcessor。
继续后面流程，获取ConfigurationClassPostProcessor后，开始执行BeanDefinitionRegistryPostProcessor:
//开始执行装配逻辑
invokeBeanDefinitionRegistryPostProcessors(priorityOrderedPostProcessors, registry);

开始执行 SpringBoot 默认配置逻辑
继续回到ConfigurationClassParser中的parse方法，回到该方法的最后一步：
public void parse(Set<BeanDefinitionHolder> configCandidates) {
		//...
		//开始执行默认配置
		processDeferredImportSelectors();
	}
getImportSelector()方法获取的 selector对象为EnableAutoConfigurationImportSelector，继续跟进该对象的selectImports方法
这里的处理方式，前面的博客中已经详细介绍过了，通过class类型来获取spring.factories中的指定类，class类型为：EnableAutoConfiguration
protected Class<?> getSpringFactoriesLoaderFactoryClass() {
		return EnableAutoConfiguration.class;
	}
在获取到springBoot提供的配置后，再次调用processImports方法进行递归解析，根据我们自定义的配置文件，进行选择性配置。
————————————————
版权声明：本文为CSDN博主「张书康」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/woshilijiuyi/article/details/82388509

*/
@SpringBootApplication
public class LaunchScriptTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaunchScriptTestApplication.class, args);
	}

}
