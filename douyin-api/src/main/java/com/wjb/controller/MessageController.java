package com.wjb.controller;

import com.wjb.grace.result.GraceJSONResult;
import com.wjb.mo.MessageMO;
import com.wjb.service.MsgService;
import com.wjb.service.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("msg")
public class MessageController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize) {

        //mongodb从0开始分页
        if (page == null) {
            page = COMMON_START_PAGE_ZERO;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        List<MessageMO> messageMOS = msgService.queryList(userId, page, pageSize);
        return GraceJSONResult.ok(messageMOS);
    }
}
