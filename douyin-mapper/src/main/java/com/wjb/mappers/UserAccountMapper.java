package com.wjb.mappers;

import com.wjb.my.mapper.MyMapper;
import com.wjb.pojo.UserAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountMapper extends MyMapper<UserAccount> {
}