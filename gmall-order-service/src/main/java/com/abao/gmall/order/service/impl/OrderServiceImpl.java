package com.abao.gmall.order.service.impl;

import com.abao.gmall.HttpClientUtil;
import com.abao.gmall.bean.OrderDetail;
import com.abao.gmall.bean.OrderInfo;
import com.abao.gmall.bean.enums.OrderStatus;
import com.abao.gmall.bean.enums.PaymentWay;
import com.abao.gmall.bean.enums.ProcessStatus;
import com.abao.gmall.conf.ActiveMQUtil;
import com.abao.gmall.conf.RedisUtil;
import com.abao.gmall.order.mapper.OrderDetailMapper;
import com.abao.gmall.order.mapper.OrderInfoMapper;
import com.abao.gmall.service.OrderService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;


    @Transactional
    @Override
    public String createOrder(OrderInfo orderInfo) {

        //初始化订单的一些信息
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,7);
        orderInfo.setExpireTime(calendar.getTime());
        orderInfo.setOutTradeNo("AB" + System.currentTimeMillis() + new Random().nextInt(10000));


        //保存订单
        orderInfoMapper.insertSelective(orderInfo);

        //保存订单项
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if(orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setId(null);
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }


        return orderInfo.getId();
    }


    @Override
    public OrderInfo selectOrderInfo(String orderId) {

        return orderInfoMapper.selectByPrimaryKey(orderId);
    }




    @Override
    public boolean checkHasStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId="+ skuId + "&num=" + skuNum);

        return "1".equals(result);
    }



    @Override
    public String createTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey = "user:" + userId + ":tradeNo";

        String tradeNo = UUID.randomUUID().toString().replaceAll("-","");

        jedis.set(tradeNoKey,tradeNo);

        jedis.close();

        return tradeNo;
    }



    @Override
    public boolean checkTradeNo(String userId,String tradeNo) {

        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeNo";


        String tradeNoVal = jedis.get(tradeNoKey);

        jedis.close();

        return tradeNo.equals(tradeNoVal);
    }




    @Override
    public void delTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeNo";

        jedis.del(tradeNoKey);

        jedis.close();
    }

    @Override
    public void updateOrderStatus(OrderInfo orderInfo) {

        orderInfo.setOrderStatus(orderInfo.getProcessStatus().getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }



    @Override
    public void sendOrderStatus(String orderId) {

        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(order_result_queue);

            TextMessage textMessage = session.createTextMessage();

            //封装消息内容
            Map mapMsg = new HashMap<String,Object>();

            //查询
            OrderInfo orderInfo = selectOrderInfo(orderId);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

            orderInfo.setOrderDetailList(orderDetailList);

            //封装
            mapMsg.put("orderId",orderId);
            mapMsg.put("consignee",orderInfo.getConsignee());
            mapMsg.put("consigneeTel",orderInfo.getConsigneeTel());
            mapMsg.put("orderComment",orderInfo.getOrderComment());
            mapMsg.put("orderBody","ok");
            mapMsg.put("deliveryAddress",orderInfo.getDeliveryAddress());
            mapMsg.put("paymentWay","2");

            ArrayList<Map> detailMaps = new ArrayList<>();

            if(orderDetailList != null && orderDetailList.size() > 0){
                for (OrderDetail detail : orderDetailList) {

                    HashMap<String, String> detailMap = new HashMap<>();

                    detailMap.put("skuId",detail.getSkuId());
                    detailMap.put("skuNum",detail.getSkuNum().toString());
                    detailMap.put("skuName",detail.getSkuName());

                    detailMaps.add(detailMap);
                }
            }

            mapMsg.put("details",detailMaps);

            textMessage.setText(JSON.toJSONString(mapMsg));

            producer.send(textMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }


}
