package com.wjb.controller;

import com.wjb.bo.LoginWithAccountBO;
import com.wjb.bo.RegistLoginBO;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.BaseInfoProperties;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.pojo.UserAccount;
import com.wjb.pojo.Users;
import com.wjb.service.UserAccountService;
import com.wjb.service.UserService;
import com.wjb.utils.IPUtil;
import com.wjb.utils.SMSUtils;
import com.wjb.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RequestMapping("passport")
@RestController
public class PassportController extends BaseInfoProperties {

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {

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
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        //由于没启动短信服务暂时控制台输出
        //smsUtils.sendSMS("61",mobile,code);
        //日志code
        log.info(code);

        //把验证码放入redis 进行后续验证
        redisOperator.set(MOBILE_SMSCODE + ":" + mobile, code, 5 * 60);

        return GraceJSONResult.ok();
    }

    //使用@Valid开启校验
    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO) {

        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        //1从redis中获得验证码校验
        String codeInRedis = redisOperator.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(codeInRedis) || !codeInRedis.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        //2查询数据库是否存在
        Users user = userService.queryMobileIsExist(mobile);

        if (user == null) {
            //如果为空则为用户注册 返回user继续保存会话
            user = userService.createUser(mobile);
        }

        //3 使用redis保存用户会话信息
        //创建token
        String uToken = UUID.randomUUID().toString();
        redisOperator.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);

        //4 登陆后对短信验证码进行清除
        redisOperator.del(MOBILE_SMSCODE + ":" + mobile);

        //5 构建VO返回用户信息 前端需要获取用户信息和token
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user,usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);

    }

    @PostMapping("loginWithAccount")
    public GraceJSONResult loginWithAccount(@Valid @RequestBody LoginWithAccountBO loginWithAccountBO) {

        String username = loginWithAccountBO.getUsername();
        String password = loginWithAccountBO.getPassword();
        //acc password为空什么都不返回
        if (StringUtils.isBlank(username)||StringUtils.isBlank(password)) {
            return GraceJSONResult.ok();
        }
        UserAccount dbUserAccount = userAccountService.queryByUsername(username);
        if (dbUserAccount == null) {
            Users user = userService.createUser("user do not have mobile");
            UserAccount userAccount = new UserAccount();
            userAccount.setUserid(user.getId());
            userAccount.setUsername(loginWithAccountBO.getUsername());
            userAccount.setPassword(loginWithAccountBO.getPassword());
            userAccountService.register(userAccount);
        }else{
            if (!dbUserAccount.getPassword().equals(loginWithAccountBO.getPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
            }
        }
        return GraceJSONResult.ok();
    }


    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) throws Exception {
        //后端需要清除用户token 前端也需要清除 清除本地app中的用户信息和token会话
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);
        return GraceJSONResult.ok();
        }
    }
