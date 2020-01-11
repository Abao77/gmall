package com.abao.gmall.manageservice.constant;




public class ManageConst {



    //skuinfo-key的前缀
    public static final String SKUKEY_PREFIX = "sku:";

    //skuinfo-key的后缀
    public static final String SKUKEY_SUFFIX = ":info";

    //lock的后缀
    public static final String SKULOCK_SUFFIX = ":lock";

    //缓存的过期时间
    public static final int SKUKEY_TIMEOUT = 60*60*1000;

    //lock的过期时间
    public static final int SKULOCK_EXPIRE_PX = 10*1000;


}
