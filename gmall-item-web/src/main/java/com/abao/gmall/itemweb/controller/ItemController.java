package com.abao.gmall.itemweb.controller;


import com.abao.gmall.bean.SkuInfo;
import com.abao.gmall.bean.SkuSaleAttrValue;
import com.abao.gmall.bean.SpuSaleAttr;
import com.abao.gmall.config.LoginRequire;
import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {




    @Reference
    ManageService manageService;




    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, Model model){


        SkuInfo skuInfo = manageService.selectSkuInfo(skuId);

        List<SpuSaleAttr> spuSaleAttrList = manageService.selectSpuSaleAttrListCheckBySku(skuId,skuInfo.getSpuId());

        List<SkuSaleAttrValue> skuSaleAttrValueList = manageService.selectSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        //封装sku组合

        HashMap<String, String> retMap = new HashMap<>();

        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){

            String key = "";


            for (int i = 0; i < skuSaleAttrValueList.size(); i++) {

                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);

                if(key.length() > 0){
                    key += "|";
                }

                key += skuSaleAttrValue.getSaleAttrValueId();

                if(i == skuSaleAttrValueList.size()-1 || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                    retMap.put(key,skuSaleAttrValue.getSkuId());
                    key = "";
                }


            }

        }

        String valuesSkuJson = JSON.toJSONString(retMap);

        //封装sku组合end

        model.addAttribute("skuInfo",skuInfo);

        model.addAttribute("spuSaleAttrList",spuSaleAttrList);

        model.addAttribute("valuesSkuJson",valuesSkuJson);

        return "item";
    }








}
