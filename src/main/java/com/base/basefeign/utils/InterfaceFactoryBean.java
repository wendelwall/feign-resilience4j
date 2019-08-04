package com.base.basefeign.utils;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/3 23:21
 */
/**
 * 1.支持动态代理类创建bean
 * 2.动态代理逻辑需要我们自己实现invocationHandler
 * 3.用途：dubbo、rmi等rpc框架都需要在客户端，根据访问的服务接口，
 *   进行创建一个动态代理对象，然后注册到spring容器中，客户端通过注解引用这个代理对象进行一系列我们封装的操作，如网络io等。
 * @author garine
 * @date 2018年07月10日
 **/
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

@Data
public class InterfaceFactoryBean<T> implements FactoryBean<T> {
    private Class<T> interfaceClass;

    /**
     * 在bean注册时设置
     */
    private String params;

    /**
     * 新建bean
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        //利用反射具体的bean新建实现，不支持T为接口。
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new DymicInvocationHandler(params));
    }

    /**
     * 获取bean
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
