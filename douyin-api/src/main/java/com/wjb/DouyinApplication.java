package com.wjb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.wjb.mappers")
public class DouyinApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinApplication.class, args);
    }
}
