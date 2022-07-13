//package com.wjb.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    /**
//     * 1定义交换机
//     * 2定义队列
//     * 3创建交换机
//     * 4创建队列
//     * 5队列和交换机的绑定
//     */
//
//    //1
//    public static final String EXCHANGE_MSG = "exchange_msg";
//
//    //2
//    public static final String QUEUE_SYS_MSG = "queue_sys_msg";
//
//    //3
//    @Bean(EXCHANGE_MSG)
//    public Exchange exchange(){
//        //构建交换机
//        return ExchangeBuilder
//                .topicExchange(EXCHANGE_MSG)//topicExchange使用较多根据自定义匹配规则订阅监听
//                .durable(true)//设置持久化 重启mq后依然会存在
//                .build();
//    }
//
//    //4
//    @Bean(QUEUE_SYS_MSG)
//    public Queue queue(){
//        return new Queue(QUEUE_SYS_MSG);
//    }
//
//    //5
//    @Bean
//    public Binding binding(@Qualifier(EXCHANGE_MSG) Exchange exchange,//@Qualifier可以通过bean名称获取bean并传入
//                           @Qualifier(QUEUE_SYS_MSG) Queue queue){
//        return BindingBuilder
//                .bind(queue)
//                .to(exchange)
//                .with("sys.msg.*")//定义路由规则类似requestmapping
//                .noargs();
//    }
//
//}
