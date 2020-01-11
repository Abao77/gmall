package com.abao.gmall.manageservice.mapper;

import com.abao.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {



    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);



}
