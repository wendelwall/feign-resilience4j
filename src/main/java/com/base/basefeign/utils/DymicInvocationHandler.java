package com.base.basefeign.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/3 23:23
 */
@Slf4j
public class DymicInvocationHandler implements InvocationHandler {
    private String params;

    public DymicInvocationHandler(String params){
        this.params = params;
    }

    /**
     * 可扩展处理点invoke
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args.length > 0){
            log.info("代理对象\n->方法"+method.getName()+
                    "\n->方法调用参数："+args[0].toString()+
                    "\n->bean注册时读取到参数："+params);
        }
        return method.invoke(proxy, args);
    }
}
