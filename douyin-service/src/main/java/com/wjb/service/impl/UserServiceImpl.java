package com.wjb.service.impl;

import com.wjb.enums.Sex;
import com.wjb.enums.YesOrNo;
import com.wjb.mappers.UsersMapper;
import com.wjb.pojo.Users;
import com.wjb.service.UserService;
import com.wjb.utils.DateUtil;
import com.wjb.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    private static final String USER_FACE1 = "douyin-api/src/main/resources/banner/cat.png";

    //判断用户是否存在 存在则返回用户信息
    public Users queryMobileIsExist(String mobile) {
        //创建Example 传入对应操作类
        Example userExample = new Example(Users.class);
        //使用example.createCriteria()创建条件构造器
        Example.Criteria criteria = userExample.createCriteria();
        //使用条件构造器的.andEqualTo方法传入查询colName和参数
        criteria.andEqualTo("mobile", mobile);
        //注意这里传的是example
        Users users = usersMapper.selectOneByExample(userExample);
        return users;
    }

    //增加事务
    @Transactional
    public Users createUser(String mobile) {
        //用户自增id不使用 考虑在后续业务扩展分布分表
        //这里使用全局主键用户id 这里使用外部的包 or.n3r.idworker的sid
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        //使用DesensitizationUtil脱敏用户名生成
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        //默认用户头像
        user.setFace(USER_FACE1);

        //默认生日
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        //是否可以修改账号默认第一次可以修改
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        //使用mapper存储user
        usersMapper.insert(user);

        return user;
    }

}
