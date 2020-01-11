package com.atguigu.distributed.lock.t0624;

import org.I0Itec.zkclient.IZkDataListener;

/**
 * @auther zzyy
 * @create 2020-01-03 17:01
 */
public class ZkDistributedLockImpl extends ZkAbstractTemplate
{
    @Override
    public boolean tryLock()
    {
        try
        {
            zkClient.createEphemeral(path);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void waitLock()
    {
        IZkDataListener iZkDataListener = new IZkDataListener()
        {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception
            {

            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception
            {
                if(countDownLatch != null)
                {
                    countDownLatch.countDown();
                }
            }
        };

        zkClient.subscribeDataChanges(path,iZkDataListener);

        ////////wait。。。。,其它线程被卡在这里，不能往下走
        if(zkClient.exists(path))
        {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        zkClient.unsubscribeDataChanges(path,iZkDataListener);
    }
}
