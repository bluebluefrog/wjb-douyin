package com.wjb.service.impl;

import com.github.pagehelper.PageHelper;
import com.wjb.bo.VlogBO;
import com.wjb.enums.MessageEnum;
import com.wjb.enums.YesOrNo;
import com.wjb.mappers.MyLikedVlogMapper;
import com.wjb.mappers.VlogMapper;
import com.wjb.mappers.VlogMapperCustom;
import com.wjb.mo.MessageMO;
import com.wjb.pojo.MyLikedVlog;
import com.wjb.pojo.Vlog;
import com.wjb.service.FansService;
import com.wjb.service.MsgService;
import com.wjb.service.VlogService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.service.base.RabbitMQConfig;
import com.wjb.utils.JsonUtils;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private FansService fansService;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    private Sid sid;

    @Transactional
    public void createVlog(VlogBO vlogBO) {

        String vid = sid.nextShort();

        Vlog vlog = new Vlog();

        BeanUtils.copyProperties(vlogBO, vlog);

        vlog.setId(vid);
        vlog.setCommentsCounts(0);
        vlog.setLikeCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    public Vlog queryById(String vlogId) {
        Vlog vlog = vlogMapper.selectByPrimaryKey(vlogId);

        return vlog;
    }


    public PagedGridResult getIndexVlogList(String search,String  userId,Integer page, Integer pageSize) {

        //使用pageHelper做分页
        //pageHelper在查询之后会对查询后的方法做拦截将返回的数据做limit 省去在sql中做limit
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }

        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);

        //查询视频是否被点赞
        for (IndexVlogVO v : indexVlogList) {
            String vlogId = v.getVlogId();
            String vlogerId = v.getVlogerId();

            if (StringUtils.isNotBlank(userId)) {
                //用户是否关注该播主
                boolean doIFollowVloger = fansService.queryFansRelationExist(userId, vlogerId);
                v.setDoIFollowVloger(doIFollowVloger);

                boolean doILike = doILikeVlog(userId, vlogId);
                v.setDoILikeThisVlog(doILike);
            }

            //查询视频被点赞的数量
            Integer vlogBeLikedCounts = getVlogBeLikedCounts(vlogId);
            v.setLikeCounts(vlogBeLikedCounts);
        }

        //对PagedGridResult属性做封装
        return setterPagedGrid(indexVlogList, page);
    }

    public Integer getVlogBeLikedCounts(String vlogId) {
        String count = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isNotBlank(count)) {
            return Integer.valueOf(count);
        }
        return 0;
    }

    private boolean doILikeVlog(String userId,String vlogId){
        String doILike = redisOperator.get(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);
        boolean isLike=false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    public IndexVlogVO getVlogById(String vlogId,String userId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> vlogDetailById = vlogMapperCustom.getVlogDetailById(map);

        if (vlogDetailById != null && vlogDetailById.size() > 0 && !vlogDetailById.isEmpty()) {
            IndexVlogVO indexVlogVO = vlogDetailById.get(0);
            String dbVlogId = indexVlogVO.getVlogId();
            String vlogerId = indexVlogVO.getVlogerId();

            if (StringUtils.isNotBlank(userId)) {
                //用户是否关注该播主
                boolean doIFollowVloger = fansService.queryFansRelationExist(userId, vlogerId);
                indexVlogVO.setDoIFollowVloger(doIFollowVloger);

                boolean doILike = doILikeVlog(userId, dbVlogId);
                indexVlogVO.setDoILikeThisVlog(doILike);
                //查询视频被点赞的数量
                Integer vlogBeLikedCounts = getVlogBeLikedCounts(dbVlogId);
                indexVlogVO.setLikeCounts(vlogBeLikedCounts);
                 return indexVlogVO;
            }
        }



        return null;
    }

    @Transactional
    public void changePrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId).andEqualTo("id", vlogId);

        Vlog vlog = new Vlog();
        vlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(vlog, example);
    }

    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("vlogerId", userId).andEqualTo("isPrivate", yesOrNo);

        List<Vlog> vlogs = vlogMapper.selectByExample(example);

        return setterPagedGrid(vlogs,page);
    }

    @Transactional
    public void userLikeVlog(String userId, String vlogId) {

        //可以增加判空校验以及数据库查询id是否存在校验
        //CAN ADD

        String rid = sid.nextShort();
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(rid);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);

        myLikedVlogMapper.insert(myLikedVlog);


        //系统消息点赞短视频
        //使用消息隊列
        Vlog vlog = this.getVlog(vlogId);
        Map msgContent=new HashMap<>();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.likeVideo", JsonUtils.objectToJson(messageMO));

    }

    public Vlog getVlog(String vlogId) {
        return vlogMapper.selectByPrimaryKey(vlogId);
    }

    @Transactional
    public void userUnLikeVlog(String userId, String vlogId) {
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);

        //根据属性删除
        myLikedVlogMapper.delete(myLikedVlog);
    }


    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);
        Map map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> myLikedVlogList = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(myLikedVlogList,page);
    }

    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> myFollowVlogList = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : myFollowVlogList) {
            String vlogId = v.getVlogId();
            String vlogerId = v.getVlogerId();

            if (StringUtils.isNotBlank(userId)) {
                //用户必定关注该播主
                v.setDoIFollowVloger(true);

                boolean doILike = doILikeVlog(userId, vlogId);
                v.setDoILikeThisVlog(doILike);
            }

            //查询视频被点赞的数量
            Integer vlogBeLikedCounts = getVlogBeLikedCounts(vlogId);
            v.setLikeCounts(vlogBeLikedCounts);
        }


        return setterPagedGrid(myFollowVlogList,page);
    }

    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> myFriendVlogList = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : myFriendVlogList) {
            String vlogId = v.getVlogId();
            String vlogerId = v.getVlogerId();

            if (StringUtils.isNotBlank(userId)) {
                //用户必定关注该播主
                v.setDoIFollowVloger(true);

                boolean doILike = doILikeVlog(userId, vlogId);
                v.setDoILikeThisVlog(doILike);
            }

            //查询视频被点赞的数量
            Integer vlogBeLikedCounts = getVlogBeLikedCounts(vlogId);
            v.setLikeCounts(vlogBeLikedCounts);
        }

        return setterPagedGrid( myFriendVlogList,page);
    }

    @Transactional
    public void flushCounts(String vlogId, Integer counts) {

        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);
    }
}
