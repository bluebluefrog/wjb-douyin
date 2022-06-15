package com.wjb.service;

import com.wjb.bo.UpdatedUserBO;
import com.wjb.pojo.Users;

public interface UserService {

    public Users queryMobileIsExist(String mobile);

    public Users createUser(String mobile);

    public Users getUserById(String userId);

    public Users updateUserInfo(UpdatedUserBO updatedUserBO);

    public Users updateUserInfo(UpdatedUserBO updatedUserBO,Integer type);
}
