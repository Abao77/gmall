package com.abao.gmall.manageweb.controller;


import com.abao.gmall.bean.BaseSaleAttr;
import com.abao.gmall.bean.SpuImage;
import com.abao.gmall.bean.SpuInfo;
import com.abao.gmall.bean.SpuSaleAttr;
import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    ManageService manageService;





    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){

        return manageService.spuList(spuInfo);
    }


    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> spuList(){

        return manageService.baseSaleAttrList();
    }


    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        return manageService.spuSaleAttrList(spuId);
    }


    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage){

        return manageService.spuImageList(spuImage);
    }






}
