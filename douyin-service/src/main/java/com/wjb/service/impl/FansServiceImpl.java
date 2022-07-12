package com.wjb.service.impl;

import com.github.pagehelper.PageHelper;
import com.wjb.enums.MessageEnum;
import com.wjb.enums.YesOrNo;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.mappers.FansMapper;
import com.wjb.mappers.FansMapperCustom;
import com.wjb.pojo.Fans;
import com.wjb.service.FansService;
import com.wjb.service.MsgService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.FansVO;
import com.wjb.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;

    @Transactional
    public void follow(String userId, String vlogerId) {

        String fid = sid.nextShort();

        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(userId);
        fans.setVlogerId(vlogerId);

        //查询关注关系是否已经存在
        boolean follow = queryFansRelationExist(userId, vlogerId);

        if (follow) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

        Fans vloger = queryFansRelationShip(vlogerId, userId);

        if (vloger != null) {

            //查询对方是否关注了我 再做保存
            //0没关注 1关注
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);

            //如果对方也关注需要更改对方的互为好友字段
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        }
        else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);

        //发送关注消息
        msgService.createMsg(userId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
    }

    public Fans queryFansRelationShip(String fanId, String vlogerId) {

        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId).andEqualTo("fanId", fanId);
        List<Fans> fans = fansMapper.selectByExample(example);

        Fans fan = null;
        if (fans != null && fans.size() > 0 && !fans.isEmpty()) {
            fan=(Fans)fans.get(0);
        }
        return fan;
    }

    public boolean queryFansRelationExist(String fanId, String vlogerId) {

        Fans fans = queryFansRelationShip(fanId, vlogerId);

        if (fans != null) {
            return true;
        }

        return false;
    }

    @Transactional
    public void unFollow(String userId, String vlogerId) {

        //判断是不是朋友是则取消关系
        Fans fans = queryFansRelationShip(userId, vlogerId);
        if (fans != null && fans.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            //抹除双方的关系
            Fans vloger = queryFansRelationShip(vlogerId, userId);
            vloger.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        }
        //删除自己的关注关系
        fansMapper.delete(fans);
    }

    public PagedGridResult queryMyFollowList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        
        Map map = new HashMap<>();
        map.put("myId", userId);

        List<VlogerVO> followList = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(followList, page);
    }


    public PagedGridResult queryMyFanList(String userId, Integer page, Integer pageSize) {

 /*     判断粉丝是否互粉
        普通做法
        多表关联+嵌套查询 这样会违反多表关联规范 高并发会出问题
        常规做法
        1避免过多表关联 先查询我的粉里列表
        2判断粉丝关注我并且我也关注粉丝 ->循环粉丝list获取每一个粉丝 再去数据库查询是否关注
        3如果我也关注粉丝则为互粉 标记flag为true
        高端做法
        1关注取关的时候 关联关系保存在redis中 不依赖数据库
        2数据库查询后 直接循环查询redis 避免第二次查询数据库
*/

        PageHelper.startPage(page, pageSize);

        Map map = new HashMap<>();
        map.put("myId", userId);

        List<FansVO> followList = fansMapperCustom.queryMyFans(map);

//        //判断粉丝是否互粉
        for (FansVO fans : followList) {
            String relation = redisOperator.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + userId + ":" + fans.getFanId());
            if (StringUtils.isNotBlank(relation) && relation.equalsIgnoreCase(IS_FRIEND)) {
                fans.setFriend(true);
            }
        }

        return setterPagedGrid(followList, page);
    }
}
