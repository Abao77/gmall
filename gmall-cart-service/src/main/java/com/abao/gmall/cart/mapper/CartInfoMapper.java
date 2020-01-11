package com.abao.gmall.cart.mapper;


import com.abao.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * 查询用户的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListByUserId(String userId);



}
