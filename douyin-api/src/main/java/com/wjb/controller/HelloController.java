package com.wjb.controller;

import com.wjb.config.MinIOConfig;
import com.wjb.grace.result.GraceJSONResult;
import com.wjb.utils.MinIOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

//    @Autowired
//    public RabbitTemplate rabbitTemplate;

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

//    @GetMapping("produce")
//    public GraceJSONResult produce(){
//        //发送消息1交换机2交换规则3消息
//        //路由规则
//        //*代表一个占位符 a.*.* 匹配 a.b.c.d不能匹配
//        //#代表多个占位符 a.# 匹配 a.b.c.d能匹配
//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.send", "创建");
//
//        return GraceJSONResult.ok();
//    }
//
//    @GetMapping("produce2")
//    public GraceJSONResult produce2(){
//        //发送消息1交换机2交换规则3消息
//        //路由规则
//        //*代表一个占位符 a.*.* 匹配 a.b.c.d不能匹配
//        //#代表多个占位符 a.# 匹配 a.b.c.d能匹配
//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.delete", "删除");
//
//        return GraceJSONResult.ok();
//    }

}
