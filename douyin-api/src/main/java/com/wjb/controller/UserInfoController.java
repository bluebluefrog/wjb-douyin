package com.wjb.controller;

import com.wjb.service.base.BaseInfoProperties;
import com.wjb.bo.UpdatedUserBO;
import com.wjb.config.MinIOConfig;
import com.wjb.enums.FileTypeEnum;
import com.wjb.enums.UserInfoModifyType;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.pojo.Users;
import com.wjb.service.UserService;
import com.wjb.utils.MinIOUtils;
import com.wjb.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private MinIOConfig minIOConfig;


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


    @PostMapping("modifyImage")
    public GraceJSONResult modifyUserInfo(MultipartFile file, @RequestParam String userId,@RequestParam Integer type) throws Exception {

        //type2 头像 1背景图
        //如果不是背景图也不是头像则报错
        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        //获取文件名
        String originalFilename = file.getOriginalFilename();

        //调用utils中的上传方法参数1bucket name2file name3input stream
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), originalFilename, file.getInputStream());

        //拼接获取minIO file存储url
        String url = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + originalFilename;

        //修改图片地址存到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);
        //判断是头像还是背景图
        if(type==FileTypeEnum.BGIMG.type){
            updatedUserBO.setBgImg(url);
        }
        else if (type == FileTypeEnum.FACE.type) {
            updatedUserBO.setFace(url);
        }
        //重新获取用户信息传给前端
        Users users = userService.updateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(users);
    }
}
