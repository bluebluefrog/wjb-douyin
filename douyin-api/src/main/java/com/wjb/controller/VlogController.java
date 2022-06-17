package com.wjb.controller;

import com.wjb.bo.VlogBO;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.service.VlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequestMapping("vlog")
@RestController
public class VlogController {

    @Autowired
    private VlogService vlogService;


    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {

        vlogService.createVlog(vlogBO);

        return GraceJSONResult.ok();
    }

}
