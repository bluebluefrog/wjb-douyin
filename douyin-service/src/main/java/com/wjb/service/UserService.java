package com.wjb.service;

import com.wjb.pojo.Users;

public interface UserService {

    public Users queryMobileIsExist(String mobile);

    public Users createUser(String mobile);

}
