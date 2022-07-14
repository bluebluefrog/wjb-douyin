package com.wjb.controller;

import com.wjb.enums.YesOrNo;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.bo.VlogBO;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.service.VlogService;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @Value("${nacos.counts}")
    private Integer nacosCounts;


    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {

        vlogService.createVlog(vlogBO);

        return GraceJSONResult.ok();
    }

    //@RequestParam(defaultValue = "") 接口调用时reqparam是必填项所以如果不填默认空
    @GetMapping("indexList")
    public GraceJSONResult publish(@RequestParam(defaultValue = "") String search,
                                   @RequestParam String userId,
                                   @RequestParam Integer page,
                                   @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult indexVlogList = vlogService.getIndexVlogList(search, userId, page, pageSize);

        return GraceJSONResult.ok(indexVlogList);
    }


    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {

        IndexVlogVO indexVlog = vlogService.getVlogById(vlogId, userId);

        return GraceJSONResult.ok(indexVlog);
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {

        vlogService.changePrivateOrPublic(userId, vlogId, YesOrNo.NO.type);

        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {
        vlogService.changePrivateOrPublic(userId, vlogId, YesOrNo.YES.type);

        return GraceJSONResult.ok();
    }

    @GetMapping("myPublicList")
    public GraceJSONResult queryPublic(@RequestParam(defaultValue = "") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult queryPrivate(@RequestParam(defaultValue = "") String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam(defaultValue = "") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult vlogsLiked = vlogService.getMyLikedVlogList(userId, page, pageSize);

        return GraceJSONResult.ok(vlogsLiked);
    }

    @PostMapping("like")
    public GraceJSONResult likeVlog(@RequestParam String userId, @RequestParam String vlogId,@RequestParam String vlogerId) {
        vlogService.userLikeVlog(userId, vlogId);

        //点赞后视频和视频发布者redis中点赞都加一
        redisOperator.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId,1);
        redisOperator.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId,1);
        //同时点赞视频需要在redis中保存用户点赞关系
        redisOperator.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        //点赞到达阈值更新到数据库
        String countsStr = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);

            if (counts >= nacosCounts) {
                vlogService.flushCounts(vlogId, counts);
            }
        }

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unLikeVlog(@RequestParam String userId, @RequestParam String vlogId,@RequestParam String vlogerId) {
        vlogService.userUnLikeVlog(userId, vlogId);

        //点赞后视频和视频发布者redis中点赞都减一
        redisOperator.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId,1);
        redisOperator.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId,1);
        //同时点赞视频需要在redis中取消保存用户点赞关系
        redisOperator.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("totalLikedCounts")
    public GraceJSONResult unLikeVlog(@RequestParam String vlogId) {

        Integer vlogBeLikedCounts = vlogService.getVlogBeLikedCounts(vlogId);

        return GraceJSONResult.ok(vlogBeLikedCounts);
    }

    @GetMapping("followList")
    public GraceJSONResult myFollowList(@RequestParam("myId") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult vlogsLiked = vlogService.getMyFollowVlogList(userId, page, pageSize);

        return GraceJSONResult.ok(vlogsLiked);
    }

    @GetMapping("friendList")
    public GraceJSONResult myFriendList(@RequestParam("myId") String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult vlogsLiked = vlogService.getMyFriendVlogList(userId, page, pageSize);

        return GraceJSONResult.ok(vlogsLiked);
    }
}
