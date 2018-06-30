package hust.zeng.utils.singleton;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zk工具类，单例
 * @title ZookeeperUtil
 * @author zengzhihua
 */
public class ZookeeperUtil {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperUtil.class);
    private ZooKeeper zk;
    private String zkAddress;
    private int sessionTimeout = 30000;

    private ZookeeperUtil() {
    }

    private static class SingleTon {
        private static ZookeeperUtil zkUtil = new ZookeeperUtil();
    }

    public static ZookeeperUtil getInstance() {
        return SingleTon.zkUtil;
    }

    public ZooKeeper connect(String zkAddress) {
        this.zkAddress = zkAddress;
        try {
            zk = new ZooKeeper(zkAddress, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.info("Watcher eventType = " + event.getType());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("reconnect zk...");
            connect(zkAddress);
        }
        logger.info("connect zk success!");
        return zk;
    }

    public String getZkAddress() {
        return zkAddress;
    }
}
