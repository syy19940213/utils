package hust.zeng.utils.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mysql工具类，单例
 * @title MysqlUtil
 * @author zengzhihua
 */
public class MysqlUtil {

    private static Logger logger = LoggerFactory.getLogger(MysqlUtil.class);
    private static Map<String, Connection> connMap = new HashMap<String, Connection>();

    private static class SingletonHolder {
        private static MysqlUtil mysqlUtil = new MysqlUtil();
    }

    public static MysqlUtil getInstance() {
        return SingletonHolder.mysqlUtil;
    }

    private MysqlUtil() {
    }

    public Connection getConn(String driver, String url, String username, String password) throws Exception {
        Connection conn = connMap.get(url);
        if (conn == null) {
            synchronized (this) {
                try {
                    logger.info("Create Connection, url = " + url);
                    Class.forName(driver);
                    conn = DriverManager.getConnection(url, username, password);
                    connMap.put(url, conn);

                } catch (Exception e) {
                    throw e;
                }
            }
        }
        logger.info("Succeeded connecting to the Database!");
        return conn;
    }

}
