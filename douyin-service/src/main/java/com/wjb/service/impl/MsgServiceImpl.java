package com.wjb.service.impl;

import com.wjb.enums.MessageEnum;
import com.wjb.mo.MessageMO;
import com.wjb.pojo.Users;
import com.wjb.repository.MessageRepository;
import com.wjb.service.MsgService;
import com.wjb.service.UserService;
import com.wjb.service.base.BaseInfoProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    public void createMsg(String formUserId, String toUserId, Integer type, Map content) {

        Users fromUser = userService.getUserById(formUserId);

        MessageMO messageMO = new MessageMO();
        //mongodb id会自增不用设置
        messageMO.setFromUserId(formUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());

        messageMO.setToUserId(toUserId);
        if (content != null) {
            messageMO.setMsgContent(content);
        }
        messageMO.setMsgType(type);

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {

        //构建分页查询
        Pageable pageable = PageRequest.of(page, pageSize);
        //可以在分页中进行排序等价于下面方法中OrderByCreateTimeDesc
        //Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC,"createTime");

        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageable);

        for (MessageMO message : list
        ) {
            //如果类型是关注消息，需要查询用户是否关注过该用户
            if (message.getMsgType() != null && message.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map msgContent = message.getMsgContent();
                if (msgContent == null) {
                    msgContent = new HashMap();
                }
                String follow = redisOperator.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + message.getToUserId() + ":" + message.getFromUserId());
                if (StringUtils.isNotBlank(follow) && follow.equalsIgnoreCase("1")) {
                    msgContent.put("isFriend", true);
                }else{
                    msgContent.put("isFriend", false);
                }
                message.setMsgContent(msgContent);
            }
        }

        return list;
    }
}
