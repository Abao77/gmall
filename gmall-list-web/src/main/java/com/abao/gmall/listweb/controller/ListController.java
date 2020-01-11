package com.abao.gmall.listweb.controller;


import com.abao.gmall.bean.*;
import com.abao.gmall.service.ListService;

import com.abao.gmall.service.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Controller
@CrossOrigin
public class ListController {


    @Reference
    ListService listService;

    @Reference
    ManageService manageService;


    @RequestMapping("list.html")
    public String search(SkuLsCondition skuLsCondition,String exId, Model model){//封装前端页面条件 为对象，接收


        if(exId != null && exId.length() > 0 && skuLsCondition.getValueIds() != null && skuLsCondition.getValueIds().size() > 0) {

            for (int k = 0; k < skuLsCondition.getValueIds().size(); k++) {
                //取消筛选了平台属性值
                if (skuLsCondition.getValueIds().get(k).equals(exId)){
                    skuLsCondition.getValueIds().remove(k);
                }
            }
        }

        //大的返回结果
        SkuLsResult skuLsResult =  listService.search(skuLsCondition);

        //要返回的商品列表
        List<SkuLsDto> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //es中的平台属性值id
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //返回的平台属性栏
        List<BaseAttrInfo> attrInfoList =  manageService.getAttrListByValueIdList(attrValueIdList);


        String urlParam = "";

        List<BaseAttrValue> mbParamList = new ArrayList<>();

        //制作url参数返回前台
        if(skuLsCondition.getCatalog3Id() !=null && skuLsCondition.getCatalog3Id().length() > 0){
            //通过3级分类来的
            urlParam += "catalog3Id=" + skuLsCondition.getCatalog3Id();
        }

        if (skuLsCondition.getKeyword() != null && skuLsCondition.getKeyword().length() > 0){
            //通过全文检索来的
            urlParam += "keyword=" + skuLsCondition.getKeyword();

        }

        if(skuLsCondition.getValueIds() != null && skuLsCondition.getValueIds().size() > 0){
            //筛选了平台属性值
            //页面传来的valueIds
            for (String valueId : skuLsCondition.getValueIds()) {

                urlParam += "&valueIds=" + valueId;

                BaseAttrValue value = new BaseAttrValue();

                //排除已选择的 平台属性
                for (int i = 0; i < attrInfoList.size(); i++) {

                    List<BaseAttrValue> attrValueList = attrInfoList.get(i).getAttrValueList();

                    for (int j = 0; j < attrValueList.size(); j++) {

                        //页面点击了平台属性值过滤
                        if(valueId.equals(attrValueList.get(j).getId())){

                            value.setValueName(attrInfoList.get(i).getAttrName() + ":" + attrValueList.get(j).getValueName());
                            value.setId(attrValueList.get(j).getId());
                            attrInfoList.remove(i);
                            break;
                        }
                    }
                }

                mbParamList.add(value);
            }
        }


        model.addAttribute("skuLsInfoList",skuLsInfoList);

        model.addAttribute("attrInfoList",attrInfoList);

        model.addAttribute("urlParam",urlParam);

        model.addAttribute("mbParamList",mbParamList);

        model.addAttribute("pageNo",skuLsCondition.getPageNo());
        model.addAttribute("totalPages",skuLsResult.getTotalPages());

        return "list";
    }


    @ResponseBody
    @RequestMapping("addToEs/{skuId}")
    public String addToEs(@PathVariable String skuId){

        SkuInfo skuInfo = manageService.selectSkuInfo(skuId);

        SkuLsDto skuLsDto = new SkuLsDto();

        BeanUtils.copyProperties(skuInfo, skuLsDto);

        listService.savaSkuInfoLsToEs(skuLsDto);

        return "ok";
    }


    /**
     * 查询语句
     *
     *     GET gmall_index/SkuInfo/_search
     *         {
     *           "query": {
     *
     *             "bool": {
     *               "filter": [
     *                 {"term": {"catalog3Id": "61"}},
     *                 {"term": {"skuAttrValueList.valueId": "82"}}
     *               ],
     *               "must": [
     *                 {"match": {
     *                   "skuName": "小米手机"
     *                 }}
     *               ]
     *             }
     *           },
     *           "sort": [
     *             {
     *               "hotScore": {
     *                 "order": "desc"
     *               }
     *             }
     *           ],
     *           "from": 0,
     *           "size": 3,
     *           "highlight": {
     *             "pre_tags": "<span style=color:red>",
     *             "post_tags": "</span>",
     *             "fields": {
     *               "skuName": {}
     *             }
     *           },
     *           "aggs": {
     *             "groupby_valueId": {
     *               "terms": {
     *                 "field": "skuAttrValueList.valueId"
     *               }
     *             }
     *           }
     *         }
     */

    /**
     * 自定义mapping
     *
     * PUT gmall_index
     * {
     *   "mappings": {
     *
     *     "SkuInfo":{
     *       "properties": {
     *         "id":{
     *           "type": "keyword",
     *           "index": false
     *         },
     *         "price":{
     *           "type": "double"
     *         },
     *         "skuName":{
     *           "type": "text",
     *           "analyzer": "ik_max_word"
     *         },
     *         "catalog3Id":{
     *           "type": "keyword"
     *         },
     *         "skuDefaultImg":{
     *           "type": "keyword",
     *           "index": false
     *         },
     *         "skuAttrValueList":{
     *           "properties": {
     *             "valueId":{
     *               "type":"keyword"
     *             }
     *           }
     *         }
     *
     *       }
     *     }
     *   }
     * }
     */

}
