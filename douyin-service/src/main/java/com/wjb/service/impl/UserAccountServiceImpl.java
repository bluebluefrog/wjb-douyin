package com.wjb.service.impl;

import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.mappers.UserAccountMapper;
import com.wjb.pojo.UserAccount;
import com.wjb.service.UserAccountService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private Sid sid;

    public UserAccount queryByUsername(String username) {
        Example example = new Example(UserAccount.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        UserAccount userAccount = userAccountMapper.selectOneByExample(example);
        return userAccount;
    }

    @Transactional
    public UserAccount register(UserAccount userAccount) {
        String userAccountId = sid.nextShort();
        userAccount.setId(userAccountId);
        int result = userAccountMapper.insert(userAccount);
        if (result == 0) {
            GraceException.display(ResponseStatusEnum.FAILED);
        }
        return userAccount;
    }
}
