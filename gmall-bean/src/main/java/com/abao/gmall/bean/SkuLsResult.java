package com.abao.gmall.bean;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuLsResult implements Serializable {


    List<SkuLsDto> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;


}
