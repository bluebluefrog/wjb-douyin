package com.wjb.service;

import com.wjb.bo.VlogBO;
import com.wjb.pojo.Vlog;

public interface VlogService {

    void createVlog(VlogBO vlogBO);

    Vlog queryById(String vlogId);
}
