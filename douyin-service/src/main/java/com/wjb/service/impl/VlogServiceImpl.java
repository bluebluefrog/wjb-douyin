package com.wjb.service.impl;

import com.github.pagehelper.PageHelper;
import com.wjb.bo.VlogBO;
import com.wjb.enums.YesOrNo;
import com.wjb.exceptions.GraceException;
import com.wjb.grace.result.ResponseStatusEnum;
import com.wjb.mappers.VlogMapper;
import com.wjb.mappers.VlogMapperCustom;
import com.wjb.pojo.Vlog;
import com.wjb.service.VlogService;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
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


    public PagedGridResult getIndexVlogList(String search, Integer page, Integer pageSize) {

        //使用pageHelper做分页
        //pageHelper在查询之后会对查询后的方法做拦截将返回的数据做limit 省去在sql中做limit
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }

        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);

        //对PagedGridResult属性做封装
        return setterPagedGrid(indexVlogList, page);
    }

    public IndexVlogVO getVlogById(String vlogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> vlogDetailById = vlogMapperCustom.getVlogDetailById(map);

        if (vlogDetailById != null && vlogDetailById.size() > 0 && !vlogDetailById.isEmpty()) {
            IndexVlogVO indexVlogVO = vlogDetailById.get(0);
            return indexVlogVO;
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

}
