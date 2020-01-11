package com.atguigu.distributed.lock.t0624;

import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

/**
 * @auther zzyy
 * @create 2020-01-03 16:49
 */
public abstract class ZkAbstractTemplate implements ZkLock
{
    public static final String ZKSERVERS = "192.168.111.144:2181";
    public static final int CONNECTION_TIMEOUT = 45 * 1000;
    ZkClient zkClient = new ZkClient(ZKSERVERS,CONNECTION_TIMEOUT);

    String path = "/zklock0624";

    CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void lock()
    {
        if(tryLock())
        {
            System.out.println(Thread.currentThread().getName()+"\t 抢到锁");
        }else{
            waitLock();
            lock();
        }
    }

    public abstract boolean tryLock();

    public abstract void waitLock();


    @Override
    public void unlock()
    {
        if(null != zkClient)
        {
            System.out.println(Thread.currentThread().getName()+"\t 释放锁");
            zkClient.close();  //zookeeper --> quit
        }
        System.out.println();
    }
}
