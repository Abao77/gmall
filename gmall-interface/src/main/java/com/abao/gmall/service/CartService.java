package com.abao.gmall.service;


import com.abao.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     * @param skuNum
     * @param skuId
     * @param userId
     * @return
     */
    CartInfo addToCart(String skuNum, String skuId, String userId);


    /**
     * 查询用户购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);



    /**
     * 合并购物车
     * @param cartListNoLogin
     * @param userId
     * @return
     */
    List<CartInfo> mergeCartList(List<CartInfo> cartListNoLogin, String userId);


    /**
     * 删除临时用户key的数据和缓存
     * @param userKey
     */
    void delTempKey(String userKey);

    /**
     * 勾选
     * @param isChecked
     * @param skuId
     * @param userId
     */
    void checkCart(String isChecked, String skuId, String userId);

    /**
     * 根据用户id查询购物车已勾选的商品集合
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
