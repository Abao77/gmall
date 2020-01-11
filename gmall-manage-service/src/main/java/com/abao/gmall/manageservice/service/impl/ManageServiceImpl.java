package com.abao.gmall.manageservice.service.impl;

import com.abao.gmall.bean.*;
import com.abao.gmall.conf.RedisUtil;
import com.abao.gmall.manageservice.constant.ManageConst;
import com.abao.gmall.manageservice.mapper.*;
import com.abao.gmall.service.ListService;
import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class ManageServiceImpl implements ManageService {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Reference
    ListService listService;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }



    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {


        return baseCatalog2Mapper.select(baseCatalog2);
    }



    @Override
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo) {

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {

        return baseAttrInfoMapper.selectAttrInfoList(catalog3Id);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {


        if(baseAttrInfo.getId()==null){
            //添加

            baseAttrInfoMapper.insert(baseAttrInfo);

        }else{
            //修改

            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);


            //先根据属性id 删除之前的属性值，重新添加
            BaseAttrValue attrValue = new BaseAttrValue();
            attrValue.setAttrId(baseAttrInfo.getId());

            baseAttrValueMapper.delete(attrValue);

        }


        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if(attrValueList != null && attrValueList.size() > 0){

            for (BaseAttrValue baseAttrValue : attrValueList) {

                baseAttrValue.setAttrId(baseAttrInfo.getId());

                baseAttrValueMapper.insertSelective(baseAttrValue);

            }
        }

    }

    
    
    
    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        
        if(baseAttrInfo != null){

            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrId);

            List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);

            //baseAttrInfo.setAttrValueList(attrValueList);

            return attrValueList;
        }

        return null;
    }



    @Override
    public List<SpuInfo> spuList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);
    }



    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }



    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        //商品详情
        spuInfoMapper.insert(spuInfo);

        //商品图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size() > 0){

            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());

                spuImageMapper.insert(spuImage);
            }
        }

        //商品属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList != null && spuSaleAttrList.size() > 0){

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());

                spuSaleAttrMapper.insert(spuSaleAttr);


                //商品属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0){

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        spuSaleAttrValue.setSaleAttrId(spuSaleAttr.getSaleAttrId());
                        spuSaleAttrValue.setSpuId(spuInfo.getId());

                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }

                }

            }

        }



    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(SpuSaleAttr spuSaleAttr) {

        return spuSaleAttrMapper.select(spuSaleAttr);
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {


        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public List<SpuImage> spuImageList(SpuImage spuImage) {

        return spuImageMapper.select(spuImage);
    }


    @Transactional(isolation = Isolation.DEFAULT,propagation = Propagation.REQUIRED)
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        //info
        skuInfoMapper.insertSelective(skuInfo);

        //image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() > 0){

            for (SkuImage skuImage : skuImageList) {

                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        //attrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size() > 0){

            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }

        }

        //saleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                skuSaleAttrValue.setSkuId(skuInfo.getId());

                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }


        //--------------将数据向es中添加一份--------------
        SkuLsDto skuLsDto = new SkuLsDto();

        BeanUtils.copyProperties(skuInfo, skuLsDto);

        listService.savaSkuInfoLsToEs(skuLsDto);

    }



    //----------------------------------详情页-------------------------------------------------


    @Override
    public SkuInfo selectSkuInfo(String skuId) {

        //热度增加
        //listService.incrHotScore(skuId);


        //方法一：通过jedis
        return getSkuInfoByJedis(skuId);

        //方法二：通过 redisson
        //return getSkuInfoByRedisson(skuId);


    }

    private SkuInfo getSkuInfoByRedisson(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;

        try {

            jedis = redisUtil.getJedis();

            String skuInfoKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;


            if(jedis.exists(skuInfoKey)){
                //走缓存

                System.out.println("redisson-走缓存查询");

                String skuInfoVal = jedis.get(skuInfoKey);

                if(!StringUtils.isEmpty(skuInfoVal)){

                    skuInfo = JSON.parseObject(skuInfoVal, SkuInfo.class);
                }


            }else{

               //redisson实现分布式锁
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.233.100:6379");
                RedissonClient redissonClient = Redisson.create(config);

                RLock lock  = redissonClient.getLock("my-lock");

                boolean b = lock.tryLock(100, 10, TimeUnit.SECONDS);

                if(b){

                    try {

                        System.out.println("抢到了锁，获得查询数据库的机会");

                        //通过数据库查询
                        skuInfo = getSkuInfoByDB(skuId);

                        //redisson将数据缓存到redis
                        String skuInfoVal = JSON.toJSONString(skuInfo);

                        jedis.set(skuInfoKey, skuInfoVal, "NX", "PX", ManageConst.SKUKEY_TIMEOUT);

                    } finally {

                        lock.unlock();

                    }

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

            //通过数据库查询
            skuInfo = getSkuInfoByDB(skuId);

        } finally {

            if(jedis != null){
                jedis.close();//将redis连接关闭
            }
        }


        return skuInfo;
    }

    private SkuInfo getSkuInfoByJedis(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;

        try {

            jedis = redisUtil.getJedis();

            String skuInfoKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;


            if(jedis.exists(skuInfoKey)){
                //走缓存

                System.out.println("走缓存查询");

                String skuInfoVal = jedis.get(skuInfoKey);

                if(!StringUtils.isEmpty(skuInfoVal)){

                    skuInfo = JSON.parseObject(skuInfoVal, SkuInfo.class);
                }


            }else{

                //分布式锁    》》一个线程去查数据库就行了，其他线程等着走缓存
                String lockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;

                String lockVal = UUID.randomUUID().toString().replace("-","");

                String lockResult = jedis.set(lockKey, lockVal, "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);

                //抢到了锁，获得查询数据库的机会
                if("OK".equals(lockResult)){
                    System.out.println("抢到了锁，获得查询数据库的机会");
                    //通过数据库查询
                    skuInfo = getSkuInfoByDB(skuId);

                    //将数据缓存到redis
                    String skuInfoVal = JSON.toJSONString(skuInfo);

                    String setResult = jedis.set(skuInfoKey, skuInfoVal, "NX", "PX", ManageConst.SKUKEY_TIMEOUT);

                    //解锁  删除锁
                    String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                    jedis.eval(script, Collections.singletonList(lockKey),Collections.singletonList(lockVal));

                }else{
                    //没抢到锁，等着大佬查完数据写到缓存
                    System.out.println("没抢到锁，等着大佬查完数据写到缓存");
                    Thread.sleep(2000);
                    return selectSkuInfo(skuId);
                }


            }

        } catch (Exception e) {

            e.printStackTrace();

            //通过数据库查询
            skuInfo = getSkuInfoByDB(skuId);

        } finally {

            if(jedis != null){
                jedis.close();//将redis连接关闭
            }

        }

        return skuInfo;
    }

    private SkuInfo getSkuInfoByDB(String skuId) {
        System.out.println("走数据库查询");

        //sku基本信息
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        //查询sku图片集合
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        //sku图片集合封装
        skuInfo.setSkuImageList(skuImageList);


        //sku平台属性值集合
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValues);

        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId,String spuId) {


        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);

        return spuSaleAttrList;
    }



    @Override
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId) {

        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIdList(List<String> attrValueIdList) {

        String valueIds = String.join(",", attrValueIdList);

        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.getAttrListByValueIdList(valueIds);
        return attrInfoList;
    }


}
