package com.base.basefeign.aic.service;

/**
 * @author ：sunrise
 * @description ：动态创建feign客户端实例
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/7/31 21:57
 */
public interface FeignService {
    /**
     * 通过url创建feign客户端实例
     * @param apiType feign接口类
     * @param url     动态url，包含协议、ip、端口、根目录，如:"http://192.168.153.1:5567/ems"
     * @return        feign客户端实例
     */
    <T> T createClientByUrl(Class<T> apiType, String url);

    /**
     * 通过服务名创建url
     * @param apiType feign接口类
     * @param name    动态名称，包含协议、名称、根目录，如:"http://ems-core/ems"
     * @return        feign客户端实例
     */
    <T> T createClientByName(Class<T> apiType, String name);
}
