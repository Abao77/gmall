package com.abao.gmall.banlance.bean;


import lombok.Data;

@Data
public class AccountUpdateParam {


    private Long acctId;//账户id 必须参数

    private String acctName;//账户名称

    private Integer paycheck = 0;//支付密码是否验证，默认0 不验证

    private String payPassword;//支付密码 默认000000

    private Integer status;//账户状态 1：未冻结 0：冻结

    private String regType;//注册方式

}
