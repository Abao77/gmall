package com.abao.gmall.banlance.bean.enums;

public enum ResultCodeEnum {


    OK(000000,"修改密码成功"),
    NOT_FIND(000001,"未找到指定账户"),
    VERIFY_ERROR(000002,"资料验证失败"),
    ERROR(999999,"系统异常");

    private Integer code ;
    private String message;

    ResultCodeEnum(Integer code,String message ){
        this.code=code;
        this.message=message;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public String getMessage(){return message;}

    public void setMessage(String message){this.message = message;}


}
