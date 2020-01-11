package com.abao.gmall.banlance.controller;


import com.abao.gmall.banlance.bean.AccountIntegralLog;
import com.abao.gmall.banlance.bean.AccountUpdateParam;
import com.abao.gmall.banlance.service.IntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IntegralController {


    @Autowired
    private IntegralService integralService;







    //查询用户积分收支明细信息
    @ResponseBody
    @RequestMapping("integral/detail")
    public List<AccountIntegralLog> getIntegralDetail(AccountUpdateParam param){

        if(param == null){}

        Long userId = param.getAcctId();

        if(userId == null){}


        List<AccountIntegralLog> logList = integralService.getIntegralDetail(userId);

        return logList;
    }








}
