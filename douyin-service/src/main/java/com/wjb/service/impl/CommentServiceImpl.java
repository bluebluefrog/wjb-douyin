package com.wjb.service.impl;

import com.github.pagehelper.PageHelper;
import com.wjb.bo.CommentBO;
import com.wjb.enums.MessageEnum;
import com.wjb.enums.YesOrNo;
import com.wjb.mappers.CommentMapper;
import com.wjb.mappers.CommentMapperCustom;
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
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private Sid sid;


    @Transactional
    public CommentVO createComment(CommentBO commentBO) {

        Comment comment = new Comment();

        String commentId = sid.nextShort();
        comment.setId(commentId);

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        //累加当前视频评论数
        redisOperator.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        //留言后的最新评论 返回给前端进行第一条展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        //系统消息回复/评论
        //使用消息队列
        Integer type = MessageEnum.COMMENT_VLOG.type;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId())
                && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            type = MessageEnum.REPLY_YOU.type;
        }
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent=new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);
        msgContent.put("commentContent", commentBO.getContent());

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId());
        messageMO.setToUserId(commentBO.getVlogerId());
        messageMO.setMsgContent(msgContent);

        if (type.equals(MessageEnum.COMMENT_VLOG.type)) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.comment", JsonUtils.objectToJson(messageMO));
        } else {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.reply", JsonUtils.objectToJson(messageMO));
        }

        return commentVO;
    }

    public PagedGridResult queryVlogComments(String vlogId,String userId, Integer page, Integer pageSize) {

        Map<String, Object> map = new HashMap();
        map.put("vlogId", vlogId);

        PageHelper.startPage(page, pageSize);

        List<CommentVO> commentList = commentMapperCustom.getCommentList(map);

        for (CommentVO commentVO : commentList
        ) {
            //当前视频的某个评论点赞总数
            String like = redisOperator.get(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentVO.getCommentId());
            Integer counts = 0;
            if (StringUtils.isNotBlank(like)) {
                counts = Integer.valueOf(like);
            }
            commentVO.setLikeCounts(counts);
            
            //判断当前用户是否点赞过该评论
            String doILike = redisOperator.get(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentVO.getCommentId());
            if (StringUtils.isNotBlank(doILike) && doILike.equals("1")) {
                commentVO.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(commentList, page);
    }

    public void deleteComments(String commentUserId, String commentId, String vlogId) {
        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);

        commentMapper.delete(pendingDelete);

        //累减当前视频评论数
        redisOperator.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    public Comment getComment(String id){
        return commentMapper.selectByPrimaryKey(id);
    }
}
