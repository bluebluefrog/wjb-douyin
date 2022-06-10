package com.wjb.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component//被spring扫描
@Data//使用lombook简化基本方法
@PropertySource("classpath:tencentcloud.properties")//资源文件映射地址
@ConfigurationProperties(prefix = "tencent.cloud")//增加前缀 因为配置里有前缀
public class TencentCloudProperties {

    private String secretId;
    private String secretKey;

}
