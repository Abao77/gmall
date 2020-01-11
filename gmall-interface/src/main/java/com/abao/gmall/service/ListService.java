package com.abao.gmall.service;


import com.abao.gmall.bean.SkuLsCondition;
import com.abao.gmall.bean.SkuLsDto;
import com.abao.gmall.bean.SkuLsResult;

public interface ListService {


    /**
     * 向es中灌入数据
     * @param skuLsDto
     */
    void savaSkuInfoLsToEs(SkuLsDto skuLsDto);


    /**
     * 从es中查询数据
     * @param skuLsCondition
     * @return
     */
    SkuLsResult search(SkuLsCondition skuLsCondition);


    /**
     * 增加热度评分
     * @param skuId
     */
    void incrHotScore(String skuId);

}
