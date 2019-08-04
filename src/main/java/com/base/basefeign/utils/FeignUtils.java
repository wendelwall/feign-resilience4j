package com.base.basefeign.utils;

import com.base.basefeign.aic.service.FeignService;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/4 0:02
 */
public class FeignUtils {

    /**
     * 动态创建Feign调用client对象
     * @param url       远程调用url
     * @param apiType   feign接口类
     * @param <T>
     * @return          返回feign-client对象
     */
    public static <T> T getBean(String url, Class<T> apiType){
        FeignService feignService = SpringUtils.getBean(FeignService.class);
        return feignService.createClientByUrl(apiType, url);
    }

}
