package com.abao.gmall.payment.service.impl;

import com.abao.gmall.bean.PaymentInfo;
import com.abao.gmall.conf.ActiveMQUtil;
import com.abao.gmall.payment.mapper.PaymentInfoMapper;
import com.abao.gmall.payment.service.PaymentInfoService;
import com.abao.gmall.util.HttpClient;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;



@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {


    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;





    @Override
    public void savPaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insert(paymentInfo);
    }


    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {

        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);

        return paymentInfoMapper.selectOneByExample(example);
    }


    @Override
    public void updatePaymentInfo(PaymentInfo payment) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",payment.getOutTradeNo());
        payment.setOutTradeNo(null);
        paymentInfoMapper.updateByExampleSelective(payment,example);
    }


    @Override
    public void sendPaymentResult(String orderId, String result) {

        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            MapMessage mapMessage = session.createMapMessage();
            mapMessage.setString("orderId",orderId);
            mapMessage.setString("result",result);

            producer.send(mapMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Map createNative(String orderId,String totalFee) {

        HashMap<String, String> param = new HashMap<>();

        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","宝马汽车一辆");
        param.put("out_trade_no", orderId);
        param.put("total_fee",totalFee);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://abao1.free.idcfengye.com/wx/callback/notify");
        param.put("trade_type","NATIVE");

        HashMap<String, String> map = new HashMap<>();
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);

            httpClient.post();

            String result = httpClient.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);

            map.put("code_url",resultMap.get("code_url"));
            map.put("total_fee",totalFee);
            map.put("out_trade_no",orderId);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return map;

    }


}
