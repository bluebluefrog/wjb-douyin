package com.wjb;

import com.wjb.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//通用info继承该类可以使用其属性
public class BaseInfoProperties {

    @Autowired
    public RedisOperator redisOperator;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

//    public Map<String,String> getErrors(BindingResult result){
//        //收集所有错误
//        Map<String,String> map = new HashMap<>();
//        //获得每一项校验出现的错误
//        List<FieldError> fieldErrors = result.getFieldErrors();
//        for (FieldError error:fieldErrors
//        ) {
//            //错误所对应的属性字段名
//            String field = error.getField();
//            //错误信息
//            String msg = error.getDefaultMessage();
//            //放入map
//            map.put(field, msg);
//        }
//        return map;
//    }

}
