package com.wjb.interceptor;

import com.wjb.service.base.BaseInfoProperties;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    //访问controller之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获得用户的ip
        String ip = IPUtil.getRequestIp(request);

        //得到是否存在的判断
        boolean exsit = redisOperator.keyIsExist(MOBILE_SMSCODE + ":" + ip);

        if(exsit){
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率太大");
            //请求拦截
            return false;
        }

        //true代表请求放行
        return true;
    }

    //访问到controller在渲染视图之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //视图渲染之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
