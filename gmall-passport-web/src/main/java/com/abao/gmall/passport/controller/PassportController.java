package com.abao.gmall.passport.controller;



import com.abao.gmall.bean.UserInfo;
import com.abao.gmall.passport.util.JwtUtil;
import com.abao.gmall.service.UserInfoService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {


    @Value("${token.key}")
    String tokenKey;


    @Reference
    UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(String originUrl, Model model){

        model.addAttribute("originUrl",originUrl);
        return "index";
    }


    @ResponseBody
    @RequestMapping("login")
    public String index(UserInfo userInfo, Model model, HttpServletRequest request){


        //查数据库
        UserInfo info = userInfoService.login(userInfo);

        if(info != null){

            //获取请求头里的参数，他就是服务器ip地址
            String salt = request.getHeader("X-forwarded-for");
            System.out.println("salt:" + salt);

            HashMap<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("id",info.getId());
            userInfoMap.put("nickName", info.getNickName());



            //账号密码正确，JWT生成token
            String token = JwtUtil.encode(tokenKey, userInfoMap, salt);

            return token;
        }

        return "fail";
    }


    //验证http://passport.gmall.com/verify?token=xx&salt=x
    @ResponseBody
    @RequestMapping("verify")
    public String verify(HttpServletRequest request){

        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        //JWT解码
        Map<String, Object> infoMap = JwtUtil.decode(token, tokenKey, salt);

        String id = (String)infoMap.get("id");

        //查询用户id是否登录
        UserInfo userInfo = userInfoService.verify(id);

        if(userInfo != null){
            return "success";
        }

        return "fail";
    }




}
