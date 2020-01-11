package com.abao.gmall.bean;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class SkuLsCondition implements Serializable {


    String catalog3Id;

    String keyword;

    List<String> valueIds = new ArrayList<>();

    int pageNo=1;

    int pageSize=8;

}
