package com.abao.gmall.payment.service;

import com.abao.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService {


    /**
     * 创建交易状态
     * @param paymentInfo
     */
    void savPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 查询交易状态
     * @param outTradeNo
     * @return
     */
    PaymentInfo getPaymentInfo(String outTradeNo);

    /**
     * 修改交易状态
     * @param payment
     */
    void updatePaymentInfo(PaymentInfo payment);

    /**
     * 发送消息 修改订单状态
     * @param orderId
     * @param result
     */
    void sendPaymentResult(String orderId, String result);

    /**
     *
     * @param orderId
     * @return
     */
    Map createNative(String orderId,String totalFee);
}
