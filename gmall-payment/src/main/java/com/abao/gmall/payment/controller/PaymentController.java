package com.abao.gmall.payment.controller;


import com.abao.gmall.bean.OrderInfo;
import com.abao.gmall.bean.PaymentInfo;
import com.abao.gmall.bean.enums.PaymentStatus;
import com.abao.gmall.bean.enums.ProcessStatus;
import com.abao.gmall.config.LoginRequire;
import com.abao.gmall.payment.config.AlipayConfig;
import com.abao.gmall.payment.service.PaymentInfoService;
import com.abao.gmall.service.OrderService;
import com.abao.gmall.util.IdWorker;
import com.abao.gmall.util.StreamUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.MarshalledObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentInfoService paymentInfoService;



    @Autowired
    private AlipayClient alipayClient;


    @Value("${partnerkey}")
    private String partnerkey;





    @LoginRequire
    @RequestMapping("index")
    public String index(HttpServletRequest request){

        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.selectOrderInfo(orderId);

        //request.setAttribute("nickName",nickName);
        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "index";
    }






    @ResponseBody
    @RequestMapping("alipay/submit")//支付宝支付接口
    public String aliSubmit(HttpServletRequest request, HttpServletResponse httpResponse){

        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.selectOrderInfo(orderId);


        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderComment());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());

        paymentInfoService.savPaymentInfo(paymentInfo);//创建订单状态


        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",orderInfo.getTotalAmount());
        map.put("subject","易烊千玺演唱会门票X1");

        alipayRequest.setBizContent(JSON.toJSONString(map));

        String form="";

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=utf-8");

        return form;
    }



    @RequestMapping("alipay/callback/return")//支付宝同步回调
    public String callback(HttpServletRequest request){

        return "redirect://trade.gmall.com/list";
    }



    @RequestMapping("alipay/callback/notify")//支付宝异步回调
    public String notifyCallback(@RequestParam Map<String,String> paramsMap, HttpServletRequest request) throws AlipayApiException {


         boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, "utf-8", "RSA2"); //调用SDK验证签名

        if(signVerified){//验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure

            String trade_status = paramsMap.get("trade_status");//TRADE_SUCCESS 或 TRADE_FINISHED

            if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){



                //验证数据库的交易状态是不是未支付
                String outTradeNo = paramsMap.get("out_trade_no");
                PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(outTradeNo);
                PaymentStatus status = paymentInfo.getPaymentStatus();



                if(PaymentStatus.PAID == status || PaymentStatus.ClOSED == status){//数据库中的订单不是未支付状态
                    return "failure";
                }

                //更改交易状态
                PaymentInfo payment = new PaymentInfo();
                payment.setOutTradeNo(outTradeNo);
                payment.setAlipayTradeNo(paramsMap.get("trade_no"));
                payment.setCallbackTime(new Date());
                payment.setCallbackContent(JSON.toJSONString(paramsMap));
                payment.setPaymentStatus(PaymentStatus.PAID);//更改数据库的状态为已支付
                paymentInfoService.updatePaymentInfo(payment);


                //发送消息 更改订单状态
                sendPaymentResult(paymentInfo.getOrderId(), "success");







                return "success";
            }



        }else{//验签失败则记录异常日志，并在response中返回failure.
            
            return "failure";
        }

        return "failure";
    }



    @ResponseBody
    @RequestMapping("alipay/trade/refund")//支付宝退款接口
    public String alipayRefund(HttpServletRequest request) throws AlipayApiException {

        String outTradeNo = request.getParameter("outTradeNo");

        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(outTradeNo);


        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();

        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("refund_amount",paymentInfo.getTotalAmount());

        refundRequest.setBizContent(JSON.toJSONString(map));

        AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);

        if(response.isSuccess()){

            System.out.println("调用成功");
            return "success";

        } else {
            System.out.println("调用失败");
            return "fail";
        }

    }


    //支付宝查询订单支付状态接口




    // 发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(String orderId,@RequestParam("result") String result){
        paymentInfoService.sendPaymentResult(orderId,result);
        return "sent payment result";
    }
















    @ResponseBody
    @RequestMapping("wx/submit")//微信支付接口
    public Map createNative(HttpServletRequest request){

        //String referer = request.getHeader("Referer");

        //String orderId = referer.substring(referer.indexOf("=")+1);
        IdWorker idWorker = new IdWorker();
        long orderId = idWorker.nextId();
        Map map = paymentInfoService.createNative(orderId+"","1");

        System.out.println(map);

        return map;
    }














    //微信同步回调-14day

    //微信异步回调

    @ResponseBody
    @RequestMapping("/wx/callback/notify")//微信异步回调
    public String notifyCallback(HttpServletRequest request,HttpServletResponse response) throws Exception {

        ServletInputStream inputStream = request.getInputStream();

        String xmlString  = StreamUtil.inputStream2String(inputStream, "utf-8");

        if(WXPayUtil.isSignatureValid(xmlString,partnerkey)){//验签成功

            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlString);

            String return_code = resultMap.get("return_code");

            if("SUCCESS".equals(return_code)){//支付成功

                System.out.println("支付成功");

                //  4  准备返回值 xml
                HashMap<String, String> returnMap = new HashMap<>();
                returnMap.put("return_code","SUCCESS");
                returnMap.put("return_msg","OK");

                String returnXml = WXPayUtil.mapToXml(returnMap);
                response.setContentType("text/xml");
                return  returnXml;
            }else{
                System.out.println("支付失败");
            }


        }


        return null;
    }














}
