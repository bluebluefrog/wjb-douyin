package com.wjb.service.impl;

import com.github.pagehelper.PageHelper;
import com.wjb.enums.YesOrNo;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.mappers.FansMapper;
import com.wjb.mappers.FansMapperCustom;
import com.wjb.pojo.Fans;
import com.wjb.service.FansService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.FansVO;
import com.wjb.vo.VlogerVO;
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
        PageHelper.startPage(page, pageSize);

        Map map = new HashMap<>();
        map.put("myId", userId);

        List<FansVO> followList = fansMapperCustom.queryMyFans(map);

        return setterPagedGrid(followList, page);
    }
}
