package com.atguigu.distributed.lock.common;

/**
 * @auther zzyy
 * @create 2019-09-15 15:22
 */
public class Client
{
    public static  void main(String[] args) throws InterruptedException
    {
        /*// 一个班级系统，加普通lock锁，N：1班主任
        OrderService orderService = new OrderService();
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                System.out.println(orderService.getOrdNumber());
            },String.valueOf(i)).start();
        }*/

        // N个班级系统，加普通lock锁无用了，N线程：N个班主任
        /*for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                System.out.println(new OrderService().getOrdNumber());
            },String.valueOf(i)).start();
        }*/

        //分布式锁定
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                //System.out.println(new OrderService().getOrdNumber());
                new OrderService().getOrdNumber();
            },String.valueOf(i)).start();
        }
    }
}