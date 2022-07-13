package com.wjb.config;

import com.wjb.enums.MessageEnum;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.mo.MessageMO;
import com.wjb.service.MsgService;
import com.wjb.service.base.RabbitMQConfig;
import com.wjb.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumer {

    @Autowired
    private MsgService msgService;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})//@RabbitListener指定监听的队列
    public void watchQueue(String payload, Message message) {

        String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();

        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);

        if(receivedRoutingKey.equalsIgnoreCase("sys.msg.follow")){
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.FOLLOW_YOU.type, null);
        }
        else if(receivedRoutingKey.equalsIgnoreCase("sys.msg.reply")){
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.LIKE_COMMENT.type, messageMO.getMsgContent());
        }
        else if(receivedRoutingKey.equalsIgnoreCase("sys.msg.comment")){
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.LIKE_COMMENT.type, messageMO.getMsgContent());
        }
        else if(receivedRoutingKey.equalsIgnoreCase("sys.msg.likeComment")){
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.LIKE_COMMENT.type, messageMO.getMsgContent());
        }
        else if(receivedRoutingKey.equalsIgnoreCase("sys.msg.likeVideo")){
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.LIKE_VLOG.type, messageMO.getMsgContent());
        }else{
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

    }
}
