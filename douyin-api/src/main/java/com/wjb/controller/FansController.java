package com.wjb.controller;

import com.wjb.config.MinIOConfig;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.pojo.Fans;
import com.wjb.pojo.Users;
import com.wjb.service.FansService;
import com.wjb.service.UserService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.utils.MinIOUtils;
import com.wjb.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("fans")
public class FansController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private FansService fansService;


    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam("myId") String userId, @RequestParam String vlogerId) {

        //判断两个Id不能为空
        if(StringUtils.isBlank(userId)||StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        //判罚当前用户不能关注自己
        if (userId.equals(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        //查询用户是否存在
        Users userById = userService.getUserById(userId);
        Users vlogerById = userService.getUserById(vlogerId);
        if (userById == null || vlogerById == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        //保存粉丝关系
        fansService.follow(userId, vlogerId);

        //博主粉丝+1 我的关注+1
        redisOperator.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + userId, 1);
        redisOperator.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //我和博主的关联关系 依赖redis 不要储存数据库 避免数据库压力
        redisOperator.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + userId + ":" + vlogerId,IS_FRIEND);

        return GraceJSONResult.ok();
    }

    @PostMapping("cancel")
    public GraceJSONResult unFollow(@RequestParam("myId") String userId, @RequestParam String vlogerId) {

        //判断两个Id不能为空
        if(StringUtils.isBlank(userId)||StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        //查询用户是否存在
        Users userById = userService.getUserById(userId);
        Users vlogerById = userService.getUserById(vlogerId);
        if (userById == null || vlogerById == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        fansService.unFollow(userId, vlogerId);

        //博主粉丝-1 我的关注-1
        redisOperator.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + userId, 1);
        redisOperator.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //我和博主的关联关系 依赖redis 不要储存数据库 避免数据库压力
        redisOperator.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + userId + ":" + vlogerId);
        return GraceJSONResult.ok();
    }

    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryIsFollow(@RequestParam("myId") String userId,
                                    @RequestParam String vlogerId) {

        boolean follow = fansService.queryFansRelationExist(userId, vlogerId);
        return GraceJSONResult.ok(follow);
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam("myId") String userId,
                                         @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = fansService.queryMyFollowList(userId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam("myId") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = fansService.queryMyFanList(userId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
