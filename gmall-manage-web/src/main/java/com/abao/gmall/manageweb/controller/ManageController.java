package com.abao.gmall.manageweb.controller;


import com.abao.gmall.bean.*;
import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class ManageController {


    @Reference
    ManageService manageService;


    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){

        return manageService.getCatalog1();
    }


    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2){

        return manageService.getCatalog2(baseCatalog2);
    }


    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3){

        return manageService.getCatalog3(baseCatalog3);
    }


    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        return manageService.attrInfoList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        manageService.saveAttrInfo(baseAttrInfo);
    }


    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        return manageService.getAttrValueList(attrId);
    }


    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){

        manageService.saveSpuInfo(spuInfo);
    }





}
