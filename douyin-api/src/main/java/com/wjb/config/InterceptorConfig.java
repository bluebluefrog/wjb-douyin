package com.wjb.config;

import com.wjb.interceptor.PassportInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    //时spring可以再初始化扫描并返回
    @Bean
    public PassportInterceptor passportInterceptor(){
        return new PassportInterceptor();
    }

    //注册拦截器方法
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //这侧拦截器并添加拦截路径url
        registry.addInterceptor(passportInterceptor()).addPathPatterns("/passport/getSMSCode/");
    }
}
