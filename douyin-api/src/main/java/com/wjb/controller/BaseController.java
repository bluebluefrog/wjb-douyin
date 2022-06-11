package com.wjb.controller;

import com.wjb.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//
////通用controller继承该controller可以使用其属性
//public class BaseController {
//
//    @Autowired
//    public RedisOperator redisOperator;
//
//    public static final String MOBILE_SMSCODE = "mobile:smscode";
//    public static final String REDIS_USER_TOKEN = "redis_user_token";
//    public static final String REDIS_USER_INFO = "redis_user_info";
//
//}
