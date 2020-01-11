package com.abao.gmall.order.mq;


import com.abao.gmall.bean.OrderInfo;
import com.abao.gmall.bean.enums.OrderStatus;
import com.abao.gmall.bean.enums.ProcessStatus;
import com.abao.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {


    @Autowired
    OrderService orderService;



    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if("success".equals(result)){

            //修改订单状态已支付
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(orderId);
            orderInfo.setProcessStatus(ProcessStatus.PAID);
            orderService.updateOrderStatus(orderInfo);


            //通知 减库存
            orderService.sendOrderStatus(orderId);

        }

    }


    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerResult(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");

        if("DEDUCTED".equals(status)){

            System.out.println("减库存成功");


            //修改订单状态 待发货
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(orderId);
            orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER);
            orderService.updateOrderStatus(orderInfo);


        }else{

            //库存超卖

        }

    }



}
