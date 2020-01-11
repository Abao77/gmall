package com.abao.gmall.banlance.service;


import com.abao.gmall.banlance.bean.AccountIntegralLog;

import java.util.List;

public interface IntegralService {


    /**
     *
     * @param userId
     * @return
     */
    List<AccountIntegralLog> getIntegralDetail(Long userId);
}
