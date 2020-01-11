package com.abao.gmall.user.service.impl;

import com.abao.gmall.bean.UserAddress;
import com.abao.gmall.bean.UserInfo;
import com.abao.gmall.conf.RedisUtil;
import com.abao.gmall.service.UserInfoService;
import com.abao.gmall.user.mapper.UserAddressMapper;
import com.abao.gmall.user.mapper.UserInfoMapper;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;


import java.util.List;


@Service
public class UserInfoServiceImpl implements UserInfoService {


    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public List<UserInfo> queryAll() {

        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserInfo> queryAll(UserInfo userInfo) {
        return null;
    }

    @Override
    public List<UserInfo> queryLike(UserInfo userInfo) {
        return null;
    }

    @Override
    public void saveUserInfo(UserInfo userInfo) {

    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {

    }

    @Override
    public void delUserInfo(UserInfo userInfo) {

    }

    @Override
    public List<UserAddress> queryAddress(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        userInfo.setPasswd(password);

        UserInfo info = userInfoMapper.selectOne(userInfo);

        if(info != null){

            //登陆成功，将用户信息保存到redis
            Jedis jedis = redisUtil.getJedis();

            jedis.setex("user:"+info.getId()+":info",7*24*3600,JSON.toJSONString(info));

            jedis.close();
        }

        return info;
    }

    @Override
    public UserInfo verify(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String userJson = jedis.get("user:" + userId + ":info");

        if(!StringUtils.isEmpty(userJson)){

            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);

            return userInfo;
        }

        return null;
    }


}
