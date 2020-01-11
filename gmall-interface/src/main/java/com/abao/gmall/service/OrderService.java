package com.abao.gmall.service;

import com.abao.gmall.bean.OrderInfo;

public interface OrderService {

    /**
     * 生成订单
     * @param orderInfo
     * @return
     */
    String createOrder(OrderInfo orderInfo);

    /**
     * 根据订单id查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo selectOrderInfo(String orderId);

    /**
     * 查库存系统，检查库存是否足够
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkHasStock(String skuId, Integer skuNum);

    /**
     * 生成订单唯一标识
     * @param userId
     * @return
     */
    String createTradeNo(String userId);

    /**
     * 检查订单唯一标识
     * @param userId
     * @return
     */
    boolean checkTradeNo(String userId,String tradeNo);

    /**
     * 删除订单唯一标识
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     * 修改订单支付状态
     * @param orderInfo
     */
    void updateOrderStatus(OrderInfo orderInfo);

    /**
     * 发送消息减库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);
}
