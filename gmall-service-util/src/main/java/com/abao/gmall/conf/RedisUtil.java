package com.abao.gmall.conf;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class RedisUtil {



    //jedis连接池
    private JedisPool jedisPool;



    /**
     * 初始化jedis连接池
     * @return
     */
     void initJedisPool(String host,int port,int timeOut){

        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(200);//连接池最大数

        config.setMaxWaitMillis(10*1000);//最长等待时间

        config.setMinIdle(10);//最小剩余数

        config.setBlockWhenExhausted(true);

        config.setTestOnBorrow(true);//自检


        jedisPool = new JedisPool(config,host,port,timeOut);

    }



    /**
     * 用于获取一个jedis连接
     * @return
     */
    public Jedis getJedis(){

        Jedis jedis = jedisPool.getResource();

        return jedis;
    }
    
    
    
}
