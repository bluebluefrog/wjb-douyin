package com.wjb.service;

import com.wjb.bo.VlogBO;
import com.wjb.pojo.Vlog;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;

import java.util.List;

public interface VlogService {

    void createVlog(VlogBO vlogBO);

    Vlog queryById(String vlogId);

    PagedGridResult getIndexVlogList(String search,String userId,Integer page, Integer pageSize);

    Integer getVlogBeLikedCounts(String vlogId);

    IndexVlogVO getVlogById(String vlogId,String userId);

    void changePrivateOrPublic(String userId, String vlogId, Integer yesOrNo);

    PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo);

    void userLikeVlog(String userId,String vlogId);

    Vlog getVlog(String vlogId);

    void userUnLikeVlog(String userId, String vlogId);

    PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize);

    PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize);

    PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize);

    void flushCounts(String vlogId, Integer counts);
}
