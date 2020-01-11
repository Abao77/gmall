package com.abao.gmall.bean;

import com.abao.gmall.bean.enums.OrderStatus;
import com.abao.gmall.bean.enums.PaymentWay;
import com.abao.gmall.bean.enums.ProcessStatus;
import lombok.Data;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class OrderInfo implements Serializable {


    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String consignee;//收货人

    @Column
    private String consigneeTel;//电话


    @Column
    private BigDecimal totalAmount;//总价格

    @Column
    private OrderStatus orderStatus;//订单状态

    @Column
    private ProcessStatus processStatus;//订单进度


    @Column
    private String userId;//用户id

    @Column
    private PaymentWay paymentWay;//支付方式

    @Column
    private Date expireTime;//订单过期时间

    @Column
    private String deliveryAddress;//收获地址

    @Column
    private String orderComment;//订单备注

    @Column
    private Date createTime;//订单生成时间

    @Column
    private String parentOrderId;//拆单用

    @Column
    private String trackingNo;//物流单号


    @Transient
    private List<OrderDetail> orderDetailList;//商品列表


    @Transient
    private String wareId;//

    @Column
    private String outTradeNo;//支付编号


    public void sumTotalAmount(){

        BigDecimal totalAmount=new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount= totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount=  totalAmount;
    }
}
