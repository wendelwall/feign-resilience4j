package com.base.basefeign.aic.retry;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: SyncRetryService
 *
 * @author: chentong
 * Date:     2018/12/15 23:57
 */
@Slf4j
@Component
public class SyncRetryService {


    private static final long DEFAULT_WAIT_DURATION = 1 * 1000L;
    private static RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)//最大重试次数，如果在重试次数达到maxAttempts设置的值后依然无法成功结束， 调用线程或通过异步结果对象会得到失败结果。默认重试次数为3次。
            .waitDuration(Duration.ofMillis(DEFAULT_WAIT_DURATION))//重试间隔为DEFAULT_WAIT_DURATION定义的毫秒数
//            .retryExceptions(RetryNeedException.class)//RetryNeedException类型的异常触发重试
            .retryExceptions(Throwable.class)//RetryNeedException类型的异常触发重试
//            .ignoreExceptions(RetryNoNeedException.class)//RetryNoNeedException异常不触发重试
            .retryOnException(throwable -> throwable instanceof RuntimeException)
            .retryOnResult(resp -> resp.toString().contains("result cause retry"))//如果返回值包含result cause retry字符串触发重试
            .build();
    private final CircuitBreaker circuitBreaker;

    public SyncRetryService(CircuitBreakerRegistry registry){
        this.circuitBreaker = registry.circuitBreaker("circuitA");
    }

    private Retry retry = Retry.of("sync retry", config);

    private int executeTimes = 0;

    public <T> Supplier<T> retrySupplierOnException(Supplier<T> supplier) {
        return Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, supplier));
    }

    public <T> T retryOnException(Supplier<T> supplier) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        log.info("failure rate = {},failed = {},success = {}",metrics.getFailureRate(),metrics.getNumberOfFailedCalls(),metrics.getNumberOfSuccessfulCalls());
        return Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, supplier)).get();
    }

    public <T> T retryOnException1(Supplier<Try<T>> supplier) {
        return Retry.decorateTrySupplier(retry, supplier).get().get();
    }


    public void retryOnException() {
        Retry.decorateRunnable(retry, new Runnable() {
            @Override
            public void run() {
                if (executeTimes++ < 3) {
//                    throw RetryNeedException.defaultException();
                    throw new RuntimeException("系统异常！");
                }
            }
        }).run();
    }

    public void noRetryOnException() {
        Retry.decorateRunnable(retry, new Runnable() {
            @Override
            public void run() {
                if (executeTimes++ < 3) {
//                    throw RetryNoNeedException.defaultException();
                    throw new RuntimeException("系统异常！");
                }
            }
        }).run();
    }

    public void resultNeedRetry() {
        try {
            Retry.decorateCallable(retry, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    if (executeTimes++ < 3) {
                        return "result cause retry";
                    }
                    return "success";
                }
            }).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getExecuteTimes() {
        return executeTimes;
    }
}
