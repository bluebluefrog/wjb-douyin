package com.wjb.controller;

import com.wjb.BaseInfoProperties;
import com.wjb.bo.UpdatedUserBO;
import com.wjb.enums.UserInfoModifyType;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.pojo.Users;
import com.wjb.service.UserService;
import com.wjb.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;


    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) {
        Users userById = userService.getUserById(userId);

        //构建VO返回用户信息 前端需要获取用户信息和token
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userById,usersVO);
        //查询信息用户不需要返回uToken
        //usersVO.setUserToken(uToken);

        //使用redis计算获取数量
        //获取my关注数量
        String myFollowsCountsStr = redisOperator.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);

        //获取my粉丝数量
        String myFansCountsStr = redisOperator.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        //获取my获赞数量 视频加评论
        //vlog获赞
        String likedVlogCountsStr = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        //vloger获赞
        String likedVlogerCountsStr= redisOperator.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;

        //判断redis数据是否为空
        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }

        //设置进vo
        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,@RequestParam Integer type) {
        //不同的修改type不同 后端使用enum存储
        //检测修改type是否合法 是否存在enum中
        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        //更新用户
        Users updatedUsers = userService.updateUserInfo(updatedUserBO,type);
        return GraceJSONResult.ok(updatedUsers);
    }
}
