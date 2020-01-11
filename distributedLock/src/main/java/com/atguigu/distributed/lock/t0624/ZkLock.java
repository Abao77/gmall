package com.atguigu.distributed.lock.t0624;

/**
 * @auther zzyy
 * @create 2020-01-03 16:48
 */
public interface ZkLock
{
    public void lock();

    public void unlock();
}
