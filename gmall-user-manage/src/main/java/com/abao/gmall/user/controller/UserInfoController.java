package com.abao.gmall.user.controller;


import com.abao.gmall.bean.UserInfo;
import com.abao.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController {

    @Autowired
    UserInfoService userInfoService;



    @RequestMapping("queryAll")
    public List<UserInfo> queryAll(){

        List<UserInfo> userInfoList = userInfoService.queryAll();

        return userInfoList;
    }



}
