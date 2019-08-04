package com.base.basefeign.aic.service.impl;

import com.base.basefeign.aic.service.FeignService;
import com.base.basefeign.config.FeignClientsConfiguration;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

/**
 * @author ：sunrise
 * @description ：动态创建feign客户端实例
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/7/31 21:59
 */
@Service
@Import(FeignClientsConfiguration.class)
public class FeignServiceImpl implements FeignService {
    private final Feign.Builder urlBuilder;
    private final Feign.Builder nameBuilder;

    @Autowired
    public FeignServiceImpl(Decoder decoder, Encoder encoder, Client client, Contract contract,
                            Request.Options options, Retryer retryer, FeignLoggerFactory loggerFactory) {
        Logger logger = loggerFactory.create(this.getClass());//必须构建，否则日志中无法看到重试的次数
//        Contract contract = new Contract.Default();
        // nameBuilder直接使用client，它会使用负载均衡
        nameBuilder = Feign.builder()
                .options(options)
                .retryer(retryer)
                .logger(logger)
                .logLevel(Logger.Level.FULL)
                .client(client)
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract);

        if (client instanceof LoadBalancerFeignClient) { // 无需均衡负载
            client = ((LoadBalancerFeignClient)client).getDelegate();
        }
        urlBuilder = Feign.builder()
                .options(options)
                .retryer(retryer)
                .logger(logger)
                .logLevel(Logger.Level.FULL)
                .client(client)
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract);
    }

    /**
     * 通过url创建feign客户端实例
     * @param apiType feign接口类
     * @param url     动态url，包含协议、ip、端口、根目录，如:"http://192.168.153.1:5567/ems"
     * @return        feign客户端实例
     */
    @Override
    public <T> T createClientByUrl(Class<T> apiType, String url) {
        return urlBuilder.target(apiType, url);
    }

    /**
     * 通过服务名创建url
     * @param apiType feign接口类
     * @param name    动态名称，包含协议、名称、根目录，如:"http://ems-core/ems"
     * @return        feign客户端实例
     */
    @Override
    public <T> T createClientByName(Class<T> apiType, String name) {
        return nameBuilder.target(apiType, name);
    }
}
