package com.base.basefeign.config;

import com.fasterxml.jackson.databind.Module;
import com.netflix.hystrix.HystrixCommand;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.hystrix.HystrixFeign;
import feign.optionals.OptionalDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.DefaultFeignLoggerFactory;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/2 22:20
 */
@Slf4j
@Configuration
public class FeignClientsConfiguration {
    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;
    @Autowired(
            required = false
    )
    private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList();
    @Autowired(
            required = false
    )
    private List<FeignFormatterRegistrar> feignFormatterRegistrars = new ArrayList();
    @Autowired(
            required = false
    )
    private Logger logger;

    public FeignClientsConfiguration() {
    }

    @Bean
    public Request.Options options(ConfigurableEnvironment env) {
        int ribbonReadTimeout = Integer.valueOf(env.getProperty("ribbon.ReadTimeout"));
        int ribbonConnectionTimeout = env.getProperty("ribbon.ConnectTimeout", int.class, 500000000);
        log.info("ribbonReadTimeout:" + ribbonReadTimeout + "    ribbonConnectionTimeout:" + ribbonConnectionTimeout);
        return new Request.Options(ribbonConnectionTimeout, ribbonReadTimeout);
    }

    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder() {
//        return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
        return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverter())));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass({"org.springframework.data.domain.Pageable"})
    public Encoder feignEncoder() {
        return new SpringEncoder(this.messageConverters);
    }

    @Bean
    @ConditionalOnClass(
            name = {"org.springframework.data.domain.Pageable"}
    )
    @ConditionalOnMissingBean
    public Encoder feignEncoderPageable() {
        return new PageableSpringEncoder(new SpringEncoder(this.messageConverters));
    }

    @Bean
    @ConditionalOnMissingBean
    public Contract feignContract(ConversionService feignConversionService) {
        return new SpringMvcContract(this.parameterProcessors, feignConversionService);
//        return new Contract.Default();
    }

    @Bean
    public FormattingConversionService feignConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        Iterator var2 = this.feignFormatterRegistrars.iterator();

        while(var2.hasNext()) {
            FeignFormatterRegistrar feignFormatterRegistrar = (FeignFormatterRegistrar)var2.next();
            feignFormatterRegistrar.registerFormatters(conversionService);
        }

        return conversionService;
    }

    @Bean
    @ConditionalOnMissingBean
    public Retryer feignRetryer() {
        //重试的间隔时间是动态变化的,越往后间隔时间越长，但最长不会超过设置的最大间隔（maxPeriod）
//        return new Retryer.Default(1000, 10000, 3);
        return Retryer.NEVER_RETRY; //Retryer不去重试
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder(Retryer retryer) {
        return Feign.builder().retryer(retryer);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @ConditionalOnMissingBean({FeignLoggerFactory.class})
    public FeignLoggerFactory feignLoggerFactory() {
        return new DefaultFeignLoggerFactory(this.logger);
    }

    @Bean
    @ConditionalOnClass(
            name = {"org.springframework.data.domain.Page"}
    )
    public Module pageJacksonModule() {
        return new PageJacksonModule();
    }

    @Configuration
    @ConditionalOnClass({HystrixCommand.class, HystrixFeign.class})
    protected static class HystrixFeignConfiguration {
        protected HystrixFeignConfiguration() {
        }

        @Bean
        @Scope("prototype")
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                name = {"feign.hystrix.enabled"}
        )
        public Feign.Builder feignHystrixBuilder() {
            return HystrixFeign.builder();
        }
    }

    @Bean
    public ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(new CustMappingJackson2HttpMessageConverter());
        return new ObjectFactory<HttpMessageConverters>() {
            @Override
            public HttpMessageConverters getObject() throws BeansException {
                return httpMessageConverters;
            }
        };
    }

    public class CustMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
        CustMappingJackson2HttpMessageConverter() {
            MediaType[] mediaTypes = new MediaType[]{
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.APPLICATION_JSON_UTF8,
                    MediaType.TEXT_HTML,
                    MediaType.TEXT_PLAIN,
                    MediaType.TEXT_XML,
                    MediaType.APPLICATION_ATOM_XML,
                    MediaType.APPLICATION_FORM_URLENCODED,
                    MediaType.APPLICATION_PDF,
            };
            setSupportedMediaTypes(Arrays.asList(mediaTypes));
        }
    }
}
