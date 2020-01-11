package com.atguigu.distributed.lock.util;

/**
 * @auther zzyy
 * @create 2019-09-15 12:15
 */
public class OrderNumCreateUtil
{
    public static int number = 0;

    public String getOrdNumber()
    {
        return "\t 生成订单号："+(++number);
    }
}
