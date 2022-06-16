package com.wjb.controller;

import com.wjb.config.MinIOConfig;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.utils.MinIOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
public class HelloController {

    @Autowired
    private MinIOConfig minIOConfig;

    @GetMapping("hello")
    public Object hello(){
        return "hello";
    }

    //MultipartFile form data中传来的file
    @PostMapping("upload")
    public GraceJSONResult upload(MultipartFile file) throws Exception {

        //获取文件名
        String originalFilename = file.getOriginalFilename();

        //调用utils中的上传方法参数1bucket name2file name3input stream
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), originalFilename, file.getInputStream());

        //拼接获取minIO file存储url
        String url = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + originalFilename;

        return GraceJSONResult.ok(url);
    }

}
