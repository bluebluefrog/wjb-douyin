package com.wjb.service;

import com.wjb.pojo.Fans;
import com.wjb.utils.PagedGridResult;

public interface FansService {

    void follow(String userId, String vlogerId);

    Fans queryFansRelationShip(String fanId, String vlogerId);

    boolean queryFansRelationExist(String fanId, String vlogerId);

    void unFollow(String userId, String vlogerId);

    PagedGridResult queryMyFollowList(String userId, Integer page, Integer pageSize);

    PagedGridResult queryMyFanList(String userId, Integer page, Integer pageSize);
}
