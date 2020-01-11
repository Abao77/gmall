package com.abao.gmall.banlance.bean;


import lombok.Data;

@Data
public class AccountIntegralLog {


    private String id;

    private String integralId;//外键

    private String userId;

    private Integer count;//数量

    private String content;//来源




}
