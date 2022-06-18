package com.wjb.interceptor;

import com.wjb.service.base.BaseInfoProperties;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    //访问controller之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //从header获得用户id和token
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");

        //判断userId和userToken是否为空
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            String redisToken = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
            //redis中的token有可能失效 判断是否为空 为空则抛异常
            if (StringUtils.isBlank(redisToken)) {
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            }else{
                //比较token是否一致 如果不一致 表示用户在别的手机端登录
                if (!redisToken.equalsIgnoreCase(userToken)) {
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        }else{
            //前端token和userId为空 未登录
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
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
