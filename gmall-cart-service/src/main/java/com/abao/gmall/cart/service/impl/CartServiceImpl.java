package com.abao.gmall.cart.service.impl;


import com.abao.gmall.bean.CartInfo;
import com.abao.gmall.bean.SkuInfo;
import com.abao.gmall.cart.mapper.CartInfoMapper;
import com.abao.gmall.cart.utils.CartConst;
import com.abao.gmall.conf.RedisUtil;
import com.abao.gmall.service.CartService;
import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Override
    public CartInfo addToCart(String skuNum, String skuId, String userId) {

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        //如果数据库有该购物车，缓存中没有，就同步数据库的到redis中
        if(!jedis.exists(cartKey)){

            loadCartAndToCache(userId);
        }


        //判断该商品是否存在该用户的购物车
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId",skuId).andEqualTo("userId",userId);
        CartInfo cartInfo = cartInfoMapper.selectOneByExample(example);


        //存在 数量增加
        if(cartInfo != null){

            cartInfo.setSkuNum(cartInfo.getSkuNum() + Integer.parseInt(skuNum));

            cartInfoMapper.updateByPrimaryKeySelective(cartInfo);

            //更新redis中的价格
            cartInfo.setSkuPrice(cartInfo.getCartPrice());


        }else{
            //不存在，添加到数据库 同时写到缓存中
            cartInfo = new CartInfo();

            SkuInfo skuInfo = manageService.selectSkuInfo(skuId);

            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuNum(Integer.parseInt(skuNum));
            cartInfo.setCartPrice(skuInfo.getPrice());

            cartInfoMapper.insertSelective(cartInfo);

        }

        //本次数据更新到redis中
        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));


        //设置缓存的时间,ck中的userKey过期后cart跟随过期
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        if(jedis.exists(userKey)){

            Long ttl = jedis.ttl(userKey);
            jedis.expire(cartKey,ttl.intValue());

        }else{

            jedis.expire(cartKey,7*24*3600);
        }


        jedis.close();

        return cartInfo;
    }


    @Override
    public List<CartInfo> getCartList(String userId) {

        List<CartInfo> cartInfoList = new ArrayList<>();

        //先查缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        if(jedis.exists(cartKey)){

            List<String> cartListStr = jedis.hvals(cartKey);
            if(cartListStr != null && cartListStr.size()>0){
                for (String cartStr : cartListStr) {

                    cartInfoList.add(JSON.parseObject(cartStr,CartInfo.class));
                }
            }

        }else{

            cartInfoList = loadCartAndToCache(userId);
        }

        jedis.close();
        return cartInfoList;
    }



    /**
     * 将数据库的购物车信息同步到缓存中
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartAndToCache(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        //查询数据库
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListByUserId(userId);

        if (cartInfoList != null && cartInfoList.size() > 0) {

            HashMap<String, String> map = new HashMap<>();

            for (CartInfo cartInfo : cartInfoList) {

                map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));

            }

            jedis.hmset(cartKey, map);

        }

        jedis.close();
        return cartInfoList;
    }



    @Override
    public List<CartInfo> mergeCartList(List<CartInfo> cartListNoLogin, String userId) {

        //先查userId的缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        List<CartInfo> userCartList = new ArrayList<>();

        if(jedis.exists(cartKey)){

            List<String> userRedisCartList = jedis.hvals(cartKey);
            if(userRedisCartList != null && userRedisCartList.size() > 0){
                for (String userRedisCartInfo : userRedisCartList) {

                    CartInfo cartInfo = JSON.parseObject(userRedisCartInfo, CartInfo.class);
                    userCartList.add(cartInfo);
                }
            }
        }else{//没有就查数据库

            userCartList = cartInfoMapper.selectCartListByUserId(userId);
        }


        if(userCartList != null && userCartList.size() > 0){
            //两个集合进行比较
            for (CartInfo cartInfoNo : cartListNoLogin) {

                boolean flag = false;

                for (CartInfo cartInfo : userCartList) {

                    if(cartInfoNo.getSkuId().equals(cartInfo.getSkuId())){

                        if("1".equals(cartInfoNo.getIsChecked()) || "1".equals(cartInfo.getIsChecked())){
                            cartInfo.setIsChecked("1");
                        }
                        cartInfo.setSkuNum(cartInfo.getSkuNum() + cartInfoNo.getSkuNum());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
                        flag = true;
                    }
                }
                if(!flag){
                    cartInfoNo.setId(null);
                    cartInfoNo.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNo);
                }
            }
        }else{
            for (CartInfo cartInfoNo : cartListNoLogin) {
                cartInfoNo.setId(null);
                cartInfoNo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoNo);
            }
        }
        //最后根据userid查询数据库
        List<CartInfo> retCartInfoList = loadCartAndToCache(userId);

        jedis.close();
        return retCartInfoList;
    }


    @Override
    public void delTempKey(String userKey) {

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userKey + CartConst.USER_CART_KEY_SUFFIX;

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userKey);

        cartInfoMapper.deleteByExample(example);

        jedis.del(cartKey);

        jedis.close();
    }



    @Override
    public void checkCart(String isChecked, String skuId, String userId) {

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);

        Example example = new Example(CartInfo.class);

        example.createCriteria().andEqualTo("skuId",skuId).andEqualTo("userId",userId);

        cartInfoMapper.updateByExampleSelective(cartInfo,example);

        //删除redis中缓存
        jedis.del(cartKey);


        loadCartAndToCache(userId);

        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        List<CartInfo> cartCheckList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        List<String> cartInfoList = jedis.hvals(cartKey);
        if(cartInfoList != null && cartInfoList.size() > 0){
            for (String cartInfo : cartInfoList) {
                CartInfo info = JSON.parseObject(cartInfo, CartInfo.class);
                if("1".equals(info.getIsChecked())){
                    cartCheckList.add(info);
                }
            }
        }


        jedis.close();
        return cartCheckList;
    }


}
