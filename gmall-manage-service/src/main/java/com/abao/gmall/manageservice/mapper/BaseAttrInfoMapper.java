package com.abao.gmall.manageservice.mapper;

import com.abao.gmall.bean.BaseAttrInfo;
import com.abao.gmall.bean.BaseCatalog1;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {


    /**
     * 根据3级分类id查询 平台属性和平台属性值
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoList(String catalog3Id);


    /**
     * 根据平台属性值ids查询平台属性和值 们
     * @param valueIds
     * @return
     */
    List<BaseAttrInfo> getAttrListByValueIdList(@Param("valueIds") String valueIds);


}
