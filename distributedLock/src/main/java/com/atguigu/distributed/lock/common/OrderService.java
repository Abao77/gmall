package com.atguigu.distributed.lock.common;

import com.atguigu.distributed.lock.t0624.ZkDistributedLockImpl;
import com.atguigu.distributed.lock.t0624.ZkLock;
import com.atguigu.distributed.lock.util.OrderNumCreateUtil;

/**
 * @auther zzyy
 * @create 2019-09-15 12:16
 */
public class OrderService
{
    OrderNumCreateUtil orderNumCreateUtil = new OrderNumCreateUtil();

    ZkLock lock = new ZkDistributedLockImpl();
    public String getOrdNumber()
    {
        lock.lock();
        try
        {
            String result = orderNumCreateUtil.getOrdNumber();
            System.out.println("    订单唯一号：==============>: "+result);
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }


    /*Lock lock = new ReentrantLock();
    public String getOrdNumber()
    {
        lock.lock();
        try
        {
            String result = orderNumCreateUtil.getOrdNumber();
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }*/
}
