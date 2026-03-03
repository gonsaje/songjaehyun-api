package com.songjaehyun.api.config;

import com.songjaehyun.api.demos.expiringkv.domain.ExpiringKeyValueStore;
import com.songjaehyun.api.demos.expiringkv.application.ExpiringKeyValueService;

import java.util.function.LongSupplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpiringKeyValueDemoConfig {

    @Bean
    public LongSupplier expiringKeyValueClock() {
        return System::currentTimeMillis;
    }

    @Bean
    public ExpiringKeyValueStore expiringKeyValueStore(LongSupplier expiringKeyValueClock) {
        return new ExpiringKeyValueStore(expiringKeyValueClock);
    }

    @Bean
    public ExpiringKeyValueService expiringKeyValueService(ExpiringKeyValueStore store) {
        return new ExpiringKeyValueService(store);
    }

}