package com.abao.gmall.cart.controller;


import com.abao.gmall.bean.CartInfo;
import com.abao.gmall.config.CookieUtil;
import com.abao.gmall.config.LoginRequire;
import com.abao.gmall.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {


    @Reference
    CartService cartService;


    @LoginRequire(autoRedirect = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String)request.getAttribute("userId");


        if(userId == null){//未登录，采用临时key

            userId = CookieUtil.getCookieValue(request, "user-key", false);

            if(userId == null){

                userId = UUID.randomUUID().toString().replaceAll("-","");
                CookieUtil.setCookie(request,response,"user-key",userId,7*24*3600,false);
            }

        }

        //添加到购物车
        CartInfo cartInfo = cartService.addToCart(skuNum,skuId,userId);

        //返回数据给页面展示
        cartInfo.setSkuNum(Integer.parseInt(skuNum));

        request.setAttribute("cartInfo",cartInfo);

        return "success";
    }




    @LoginRequire(autoRedirect = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request){

        String userId = (String)request.getAttribute("userId");

        List<CartInfo> cartInfoList = new ArrayList<>();

        if(userId != null){//用户已登录

            //合并购物车
            String userKey = CookieUtil.getCookieValue(request, "user-key", false);

            List<CartInfo> cartListNoLogin = null;

            if(userKey != null){//有临时user-key

                //查询临时userKey的购物车
                cartListNoLogin = cartService.getCartList(userKey);

                if(cartListNoLogin != null && cartListNoLogin.size()>0){
                    //合并临时购物车和登录后的购物车
                    cartInfoList = cartService.mergeCartList(cartListNoLogin,userId);

                    //删除临时用户key的数据和缓存
                    cartService.delTempKey(userKey);
                }
            }

            if(userKey == null || (cartListNoLogin == null || cartListNoLogin.size() == 0)){

                cartInfoList = cartService.getCartList(userId);
            }

        }else{
            //用户未登录，获取ck中临时的user-key

            String userKey = CookieUtil.getCookieValue(request, "user-key", false);

            if(userKey != null){

                cartInfoList = cartService.getCartList(userKey);
            }
        }

        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";
    }



    @LoginRequire(autoRedirect = false)
    @ResponseBody
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request){

        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");

        String userId = (String)request.getAttribute("userId");


        if(userId == null){

            userId = CookieUtil.getCookieValue(request, "user-key", false);
        }

        cartService.checkCart(isChecked,skuId,userId);

        return "ok";
    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request){


        String userId = (String)request.getAttribute("userId");

        //判断有无临时用户key进行合并购物车
        String userTempKey = CookieUtil.getCookieValue(request, "user-key", false);

        if(userTempKey != null){
            List<CartInfo> tempCartList = cartService.getCartList(userTempKey);

            if(tempCartList != null && tempCartList.size() > 0){
                cartService.mergeCartList(tempCartList,userTempKey);

                //删除临时用户key的数据和缓存
                cartService.delTempKey(userTempKey);
            }
        }


        return "redirect://trade.gmall.com/trade";
    }







}
