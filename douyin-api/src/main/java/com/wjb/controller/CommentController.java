package com.wjb.controller;

import com.wjb.bo.CommentBO;
import com.wjb.enums.MessageEnum;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.mo.MessageMO;
import com.wjb.pojo.Comment;
import com.wjb.pojo.Vlog;
import com.wjb.service.CommentService;
import com.wjb.service.MsgService;
import com.wjb.service.VlogService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.service.base.RabbitMQConfig;
import com.wjb.utils.JsonUtils;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController()
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    private VlogService vlogService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) {
        CommentVO commentVO = commentService.createComment(commentBO);

        return GraceJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {
        String countStr = redisOperator.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countStr)) {
            countStr = "0";
        }

        return GraceJSONResult.ok(Integer.valueOf(countStr));
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {

        PagedGridResult pagedGridResult = commentService.queryVlogComments(vlogId, userId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String vlogId,
                                @RequestParam String commentId,
                                @RequestParam String commentUserId) {

        commentService.deleteComments(commentUserId, commentId, vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId) {

        //bigkey存储不建议使用
//      redisOperator.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
//      redisOperator.setHashValue(REDIS_USER_LIKE_COMMENT, userId, "1");
        redisOperator.increment(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId, 1);
        redisOperator.set(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId, "1");

        //系统消息
        Comment comment = commentService.getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent=new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getCommentUserId());
        messageMO.setMsgContent(msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.likeComment", JsonUtils.objectToJson(messageMO));

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unLike(@RequestParam String commentId,
                                @RequestParam String userId) {

        redisOperator.decrement(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId, 1);
        redisOperator.del(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId);

        return GraceJSONResult.ok();
    }
}
