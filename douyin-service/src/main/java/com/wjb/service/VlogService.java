package com.wjb.service;

import com.wjb.bo.VlogBO;
import com.wjb.pojo.Vlog;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;

import java.util.List;

public interface VlogService {

    void createVlog(VlogBO vlogBO);

    Vlog queryById(String vlogId);

    PagedGridResult getIndexVlogList(String search, Integer page, Integer pageSize);

    IndexVlogVO getVlogById(String vlogId);

    void changePrivateOrPublic(String userId, String vlogId, Integer yesOrNo);

    PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo);
}
