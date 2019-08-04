package com.base.basefeign.config;

import com.base.basefeign.aic.model.Subject;
import com.base.basefeign.utils.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/3 23:35
 */
@Component
public class InitObject implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init(){
//        FeignService feignService = applicationContext.getBean(FeignService.class);
        SpringUtils.registryBean("subject", Subject.class);
        System.out.println("==============");
    }

}
