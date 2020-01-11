package com.abao.gmall.listservice.service.impl;


import com.abao.gmall.bean.SkuLsCondition;
import com.abao.gmall.bean.SkuLsDto;
import com.abao.gmall.bean.SkuLsResult;
import com.abao.gmall.conf.RedisUtil;
import com.abao.gmall.service.ListService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import io.searchbox.client.JestClient;

import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ListServiceImpl implements ListService {


    public static final String ES_INDEX = "gmall_index";
    public static final String ES_TYPE = "SkuInfo";

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void savaSkuInfoLsToEs(SkuLsDto skuLsDto) {

        String source = JSON.toJSONString(skuLsDto);

        Index index = new Index.Builder(source).index(ES_INDEX).type(ES_TYPE).id(skuLsDto.getId()).build();

        try {

            log.debug("skuLsDto=", skuLsDto);

            jestClient.execute(index);

        } catch (IOException e) {

            e.printStackTrace();
        }

    }



    @Override
    public SkuLsResult search(SkuLsCondition skuLsCondition) {



        //根据条件构造query
        String query = makeQueryStringForSearch(skuLsCondition);

        //执行查询
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult searchResult = null;
        try {

            searchResult = jestClient.execute(search);

        } catch (IOException e) {

            e.printStackTrace();
        }

        //封装返回结果
        SkuLsResult skuLsResult = makeResultForSearch(skuLsCondition, searchResult);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if(hotScore % 10 == 0){
            Update.Builder builder = new Update.Builder("{\n" +
                    "  \"doc\": {\n" +
                    "    \"hotScore\": " + hotScore + "\n" +
                    "  }\n" +
                    "}");

            Update update = builder.index(ES_INDEX).type(ES_TYPE).id(skuId).build();
            try {
                jestClient.execute(update);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        jedis.close();

    }

    private SkuLsResult makeResultForSearch(SkuLsCondition skuLsCondition, SearchResult searchResult) {

        //返回结果
        SkuLsResult skuLsResult = new SkuLsResult();

        if(searchResult != null){

            skuLsResult.setTotal(searchResult.getTotal());

            skuLsResult.setTotalPages((searchResult.getTotal() + skuLsCondition.getPageSize() -1)/skuLsCondition.getPageSize());


            //source数据的封装
            List<SearchResult.Hit<SkuLsDto, Void>> hits = searchResult.getHits(SkuLsDto.class);

            if(hits != null & hits.size() > 0){

                List<SkuLsDto> skuLsList = new ArrayList<SkuLsDto>();

                for (SearchResult.Hit<SkuLsDto, Void> hit : hits) {

                    SkuLsDto skuLsDto = hit.source;

                    if(hit.highlight != null && hit.highlight.size() > 0){
                        List<String> skuName = hit.highlight.get("skuName");
                        skuLsDto.setSkuName(skuName.get(0));
                    }

                    //基本信息封装
                    skuLsList.add(skuLsDto);
                }

                skuLsResult.setSkuLsInfoList(skuLsList);
            }

            //平台属性值id的封装
            TermsAggregation aggregations = searchResult.getAggregations().getTermsAggregation("groupby_valueId");

            List<TermsAggregation.Entry> buckets = aggregations.getBuckets();

            if(buckets != null && buckets.size() > 0){

                List<String> valueIdList = new ArrayList<>();

                for (TermsAggregation.Entry bucket : buckets) {

                    valueIdList.add(bucket.getKey());
                }

                skuLsResult.setAttrValueIdList(valueIdList);
            }

        }


        return skuLsResult;
    }


    private String makeQueryStringForSearch(SkuLsCondition skuLsCondition) {

        if(skuLsCondition == null){
            //抛一个异常
        }

        //查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //有3级分类id
        if(skuLsCondition.getCatalog3Id()!= null && skuLsCondition.getCatalog3Id().length() > 0){

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsCondition.getCatalog3Id());

            boolQueryBuilder.filter(termQueryBuilder);

        }

        //判断平台属性值id是否有值
        List<String> valueIds = skuLsCondition.getValueIds();

        if(valueIds != null && valueIds.size() > 0){
            for (String valueId : valueIds) {

                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);

                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //sku名称
        if(skuLsCondition.getKeyword() != null && skuLsCondition.getKeyword().length() > 0){

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsCondition.getKeyword());

            boolQueryBuilder.must(matchQueryBuilder);

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();

            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");

            searchSourceBuilder.highlight(highlightBuilder);
        }


        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //分页-from
        searchSourceBuilder.from((skuLsCondition.getPageNo()-1)*skuLsCondition.getPageSize());
        //分页-size
        searchSourceBuilder.size(skuLsCondition.getPageSize());


        //分组
        TermsBuilder groupby_valueId = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId");

        searchSourceBuilder.aggregation(groupby_valueId);


        //查询器执行查询
        searchSourceBuilder.query(boolQueryBuilder);

        String query = searchSourceBuilder.toString();

        System.out.println("query = " + query);

        return query;
    }


}
