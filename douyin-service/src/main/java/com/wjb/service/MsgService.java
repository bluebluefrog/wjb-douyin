package com.wjb.service;

import com.wjb.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {

    void createMsg(String formUserId, String toUserId, Integer type, Map content);

    List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize);
}
