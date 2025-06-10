package com.example.config;

import com.example.utils.ArrayUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArrayUtilsConfig {

    @Bean
    public ArrayUtils<String> stringArrayUtils(){
        return new ArrayUtils<String>();
    }

    @Bean
    public ArrayUtils<Integer> integerArrayUtils(){
        return new ArrayUtils<Integer>();
    }
}
