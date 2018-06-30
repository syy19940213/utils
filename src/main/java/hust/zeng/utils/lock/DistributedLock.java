package hust.zeng.utils.lock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zookeeper分布式锁
 * @title DistributedLock
 * @author zengzhihua
 */
public class DistributedLock implements Lock, Watcher {

    private static Logger logger = LoggerFactory.getLogger(DistributedLock.class);
    private ZooKeeper zk;
    private String lockName;
    private String lockRootPath = "/apps/cache/mylocks";
    private int sessionTimeout = 30000;
    private String PREV_LOCK;
    private String CURRENT_LOCK;
    private CountDownLatch countDownLatch;
    private List<Exception> exception = new ArrayList<Exception>();
    private String splitStr = "_lock_";

    /**
     * @param config 127.0.0.1:2181
     * @param lockName 竞争资源标识
     */
    public DistributedLock(String config, String lockName) {
        this(config, lockName, null);
    }

    /**
     * @param config 127.0.0.1:2181
     * @param lockName 竞争资源标识
     * @param lockRootPath 锁节点路径
     */
    public DistributedLock(String config, String lockName, String lockRootPath) {
        if (StringUtils.isBlank(lockName)) {
            throw new RuntimeException("lockName is blank!");
        }
        this.lockName = lockName;
        if (lockRootPath != null) {
            this.lockRootPath = lockRootPath;
        }
        initZookeeper(config);
    }

    private void initZookeeper(String config) {
        try {
            zk = new ZooKeeper(config, sessionTimeout, this);
            while (zk.exists(lockRootPath, false) == null) {
                logger.info(Thread.currentThread().getName() + " 发现 " + lockRootPath + " 节点为空，开始创建");
                try {
                    zk.create(lockRootPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    logger.info(Thread.currentThread().getName() + " 创建 " + lockRootPath + " 节点成功");
                } catch (Exception e) {
                    logger.warn(Thread.currentThread().getName() + " 创建 " + lockRootPath + " 节点失败，重新检测");
                }
            }
        } catch (IOException e) {
            exception.add(e);
        } catch (KeeperException e) {
            exception.add(e);
        } catch (InterruptedException e) {
            exception.add(e);
        }
    }

    /**
     * @title 获取锁。
     */
    @Override
    public void lock() {
        if (exception.size() > 0) {
            throw new RuntimeException(exception.get(0));
        }
        if (tryLock()) {
            logger.info(Thread.currentThread().getName() + " 获得锁 " + CURRENT_LOCK);
            return;
        } else {
            // 等待前一个锁释放
            try {
                waitForLock(PREV_LOCK, sessionTimeout);
            } catch (KeeperException e) {
                exception.add(e);
            } catch (InterruptedException e) {
                exception.add(e);
            }
        }
    }

    /**
     * @title 如果当前线程未被中断，则获取锁。
     * @throws InterruptedException
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock();
    }

    /**
     * @title 仅在调用时锁为空闲状态才获取该锁。
     * @return boolean
     */
    @Override
    public boolean tryLock() {
        try {
            CURRENT_LOCK = zk.create(lockRootPath + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            // 取出ROOT_LOCK的所有子节点
            List<String> subNodes = zk.getChildren(lockRootPath, false);
            // 取出所有lockName相关的锁
            List<String> lockObjects = new ArrayList<String>();
            for (String node : subNodes) {
                String lock = node.split(splitStr)[0];
                if (lock.equals(lockName)) {
                    lockObjects.add(node);
                }
            }
            Collections.sort(lockObjects);
            // 若当前节点为最小节点，则获取锁成功
            if (CURRENT_LOCK.equals(lockRootPath + "/" + lockObjects.get(0))) {
                logger.info(Thread.currentThread().getName() + " 对应的 " + CURRENT_LOCK + " 是最小节点");
                return true;
            }
            // 若当前节点不是最小节点，则找到自己的前一个节点
            String prevNode = CURRENT_LOCK.substring(CURRENT_LOCK.lastIndexOf("/") + 1);
            PREV_LOCK = lockObjects.get(Collections.binarySearch(lockObjects, prevNode) - 1);
        } catch (KeeperException e) {
            exception.add(e);
        } catch (InterruptedException e) {
            exception.add(e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (tryLock()) {
            return true;
        } else {
            try {
                waitForLock(PREV_LOCK, unit.toMillis(time));
            } catch (KeeperException e) {
                exception.add(e);
            }
        }
        return false;
    }

    private boolean waitForLock(String prev, long waitTime) throws KeeperException, InterruptedException {
        // 判断前一个节点是否存在，如果存在则等待它消失
        Stat stat = zk.exists(lockRootPath + "/" + prev, true);
        if (stat != null) {
            logger.info(Thread.currentThread().getName() + " 等待锁 " + PREV_LOCK + " 释放");
            countDownLatch = new CountDownLatch(1);
            // 等待节点发生变化调用process方法
            countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
            countDownLatch = null;
            logger.info(Thread.currentThread().getName() + " 拿到锁 " + CURRENT_LOCK);
        }
        return true;
    }

    @Override
    public void unlock() {
        try {
            logger.info(Thread.currentThread().getName() + " 释放锁 " + CURRENT_LOCK);
            zk.delete(CURRENT_LOCK, -1);
            CURRENT_LOCK = null;
            zk.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 5; ++i) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; ++j) {
                        DistributedLock lock = new DistributedLock("132.122.232.73:2181", "testLock");
                        System.out.println(Thread.currentThread().getName() + " start, the " + j + " times");
                        lock.lock();
                        try {
                            System.out.println(Thread.currentThread().getName() + " get lock!");
                            System.out.println(Thread.currentThread().getName() + " sleep 2s!");
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            e.printStackTrace();

                        } finally {
                            lock.unlock();
                            System.out.println(Thread.currentThread().getName() + " unlock!");
                        }
                    }
                }
            });

            thread.start();
        }

    }
}
