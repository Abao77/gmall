package com.abao.gmall.service;


import com.abao.gmall.bean.*;

import java.util.List;

public interface ManageService {


    /**
     * 查询一级分类列表
     * @return
     */
    List<BaseCatalog1> getCatalog1();


    /**
     * 查询二级分类列表
     * @param baseCatalog2
     * @return
     */
    List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);


    /**
     * 查询三级分类列表
     * @param baseCatalog3
     * @return
     */
    List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);


    /**
     * 根据三级分类id查询属性
     * @param baseAttrInfo
     * @return
     */
    List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo);

    /**
     * 根据三级分类id查询属性
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoList(String catalog3Id);


    /**
     * 新增或修改属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);


    /**
     * 根据属性id查询属性值集合
     * @param attrId
     */
    List<BaseAttrValue> getAttrValueList(String attrId);


    /**
     * 根据三级分类id查询 spu商品列表
     * @return
     */
    //List<SpuInfo> spuList(String catalog3Id);

    /**
     * 根据三级分类id查询 spu商品列表
     * @return
     */
    List<SpuInfo> spuList(SpuInfo spuInfo);


    /**
     * 获取所有的基本销售属性
     * @return
     */
    List<BaseSaleAttr> baseSaleAttrList();


    /**
     * 保存spu信息
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);


    /**
     * 根据spuId查询销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(SpuSaleAttr spuSaleAttr);


    /**
     * 根据spuId查询销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(String spuId);




    /**
     * 根据supId查询spu图片
     * @param spuImage
     * @return
     */
    List<SpuImage> spuImageList(SpuImage spuImage);


    /**
     * 创建sku
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);



    /**
     * 根据skuId查询sku详情信息
     * @param skuId
     * @return
     */
    SkuInfo selectSkuInfo(String skuId);

    /**
     * 根据skuID查询 spu属性以及 sku属性选中
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId,String spuId);


    /**
     * 查询spuId的所有销售属性值集合 map字符串格式
     * @param spuId
     */
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);


    /**
     * 根据平台属性值id集合 查询平台属性和属性值信息集合
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrListByValueIdList(List<String> attrValueIdList);
}
