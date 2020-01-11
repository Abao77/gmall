package com.abao.gmall.service;


import com.abao.gmall.bean.UserAddress;
import com.abao.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {


    /**
     * 查询所有
     * @return
     */
    List<UserInfo> queryAll();


    /**
     * 条件查询
     * @param userInfo
     * @return
     */
    List<UserInfo> queryAll(UserInfo userInfo);


    /**
     * 根据nickName模糊查询
     * @param userInfo
     * @return
     */
    List<UserInfo> queryLike(UserInfo userInfo);


    /**
     * 添加
     * @param userInfo
     */
    void saveUserInfo(UserInfo userInfo);


    /**
     * 根据loginName修改nikeName
     * @param userInfo
     */
    void updateUserInfo(UserInfo userInfo);


    /**
     * 根据loginName删除
     * @param userInfo
     */
    void delUserInfo(UserInfo userInfo);


    /**
     * 根据用户id查询地址
     * @param userId
     * @return
     */
    List<UserAddress> queryAddress(String userId);


    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户id查询用户是否存在于redis（是否登录）
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
