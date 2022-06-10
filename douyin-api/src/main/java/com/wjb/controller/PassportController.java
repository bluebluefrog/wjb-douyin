package com.wjb.controller;

import com.wjb.grace.result.GraceJSONResult;
import com.wjb.utils.IPUtil;
import com.wjb.utils.SMSUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequestMapping("passport")
@RestController
public class PassportController extends BaseController{

    @Autowired
    private SMSUtils smsUtils;

    @PostMapping("getSMSCode")
    public Object getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {

        //mobile为空什么都不返回
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }

        //获得用户ip 根据用户ip进行限制
        String userIp = IPUtil.getRequestIp(request);
        //使用gredis限制用户在60秒之内只能获取一次验证码
        //根据ip进行限制 如果redis中存在则不发送 使用拦截器实现存在则不能发送
        redisOperator.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);

        //随机生成验证码并发送
        String code = (int) (Math.random() * 9 + 1) * 100000 + "";
        smsUtils.sendSMS("61",mobile,code);
        //日志code
        log.info(code);

        //把验证码放入redis 进行后续验证
        redisOperator.set(MOBILE_SMSCODE + ":" + mobile, code, 5 * 60);

        return GraceJSONResult.ok();
    }

}
