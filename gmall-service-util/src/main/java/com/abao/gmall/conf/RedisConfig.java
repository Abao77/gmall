package com.abao.gmall.conf;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {


    @Value("${spring.redis.host:0}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.timeout:1000}")
    private int timeOut;



    //将RedisUtil初始化并放到容器中
    @Bean
    public RedisUtil getRedisUtil(){


        RedisUtil redisUtil = new RedisUtil();

        redisUtil.initJedisPool(host,port,timeOut);


        return redisUtil;
    }





}
