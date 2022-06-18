package com.wjb.controller;

import com.wjb.enums.YesOrNo;
import com.wjb.service.base.BaseInfoProperties;
import com.wjb.bo.VlogBO;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.service.VlogService;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequestMapping("vlog")
@RestController
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;


    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {

        vlogService.createVlog(vlogBO);

        return GraceJSONResult.ok();
    }

    //@RequestParam(defaultValue = "") 接口调用时reqparam是必填项所以如果不填默认空
    @GetMapping("indexList")
    public GraceJSONResult publish(@RequestParam(defaultValue = "") String search,
                                   @RequestParam Integer page,
                                   @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult indexVlogList = vlogService.getIndexVlogList(search, page, pageSize);

        return GraceJSONResult.ok(indexVlogList);
    }


    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {

         IndexVlogVO indexVlog = vlogService.getVlogById(vlogId);

        return GraceJSONResult.ok(indexVlog);
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {

        vlogService.changePrivateOrPublic(userId, vlogId, YesOrNo.NO.type);

        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId) {
        vlogService.changePrivateOrPublic(userId, vlogId, YesOrNo.YES.type);

        return GraceJSONResult.ok();
    }

    @GetMapping("myPublicList")
    public GraceJSONResult queryPublic(@RequestParam(defaultValue = "") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult queryPrivate(@RequestParam(defaultValue = "") String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

}
