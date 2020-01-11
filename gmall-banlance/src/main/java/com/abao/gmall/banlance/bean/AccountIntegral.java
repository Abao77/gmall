package com.abao.gmall.banlance.bean;


import lombok.Data;

import java.util.List;

@Data
public class AccountIntegral {


    private String id;

    private String userId;

    private Integer currentIntegral;


    private List<AccountIntegralLog> logList;

}
