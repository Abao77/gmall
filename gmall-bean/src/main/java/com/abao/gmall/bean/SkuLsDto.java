package com.abao.gmall.bean;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuLsDto implements Serializable {


    String id;

    String skuName;


    BigDecimal price;


    String skuDefaultImg;


    String catalog3Id;

    Long hotScore=0L;


    List<SkuAttrValue> skuAttrValueList;


}
