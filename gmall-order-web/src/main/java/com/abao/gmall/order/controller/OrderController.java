package com.abao.gmall.order.controller;


import com.abao.gmall.bean.CartInfo;
import com.abao.gmall.bean.OrderDetail;
import com.abao.gmall.bean.OrderInfo;
import com.abao.gmall.bean.UserAddress;
import com.abao.gmall.config.LoginRequire;
import com.abao.gmall.service.CartService;
import com.abao.gmall.service.OrderService;
import com.abao.gmall.service.UserInfoService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;



@Controller
public class OrderController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    OrderService orderService;

    @Reference
    CartService cartService;



    @LoginRequire
    @RequestMapping("trade")
    public String toTrade(HttpServletRequest request){

        String userId = (String)request.getAttribute("userId");

        List<UserAddress> userAddressList = userInfoService.queryAddress(userId);

        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        if(cartCheckedList != null && cartCheckedList.size() > 0){
            for (CartInfo cartInfo : cartCheckedList) {
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(cartInfo.getSkuPrice());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());


                orderDetailList.add(orderDetail);
            }

        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();

        //生成唯一标识
        String tradeNo = orderService.createTradeNo(userId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("userAddressList",userAddressList);
        request.setAttribute("orderDetailList",orderDetailList);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }



    @LoginRequire
    @RequestMapping("submitOrder")
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){

        String userId = (String)request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");

        orderInfo.setUserId(userId);


        //防止表单重复提交
        boolean result = orderService.checkTradeNo(userId,tradeNo);

        if(!result){

            request.setAttribute("errMsg","表单不能重复提交");
            return "tradeFail";
        }


        orderService.delTradeNo(userId);

        //验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        if(orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {

                boolean hasStock = orderService.checkHasStock(orderDetail.getSkuId(),orderDetail.getSkuNum());

                if(!hasStock){

                    request.setAttribute("errMsg",orderDetail.getSkuName()+" : <span style=\"color: red;\">库存不足</span>");
                    return "tradeFail";
                }
            }
        }




        String orderId = orderService.createOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }



    @RequestMapping("list")
    public String list(){

        return "list";
    }



}
