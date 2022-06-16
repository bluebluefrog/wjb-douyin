package com.wjb.service;

import com.wjb.pojo.UserAccount;

public interface UserAccountService {

    UserAccount queryByUsername(String username);

    UserAccount register(UserAccount userAccount);
}
