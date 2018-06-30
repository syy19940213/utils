package hust.zeng.utils.singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * @title 配置文件读取类，单例
 * @author zengzhihua
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static Map<String, Properties> propsMap = new HashMap<String, Properties>();

    private PropertiesUtil() {
    }

    private static class SingletonHolder {
        private static PropertiesUtil pUtil = new PropertiesUtil();
    }

    public static PropertiesUtil getInstance() {
        return SingletonHolder.pUtil;
    }

    /**
     * 从classpath中读取配置文件
     * @param propName
     * @return
     */
    public Properties getProp(String propName) {
        return getResource(propName);
    }

    /**
     * 从任意给定路径加载配置文件
     * @param propNamePath
     * @return
     */
    public Properties getPropByPath(String propNamePath) {
        return getResourceBypath(propNamePath);
    }

    private Properties getResource(String propName) {
        Properties props = propsMap.get(propName);
        if (props == null) {
            synchronized (this) {
                logger.info("new Properties(), propName = " + propName);
                try {
                    props = new Properties();
                    ClassLoader classLoader = PropertiesUtil.class.getClassLoader();
                    logger.info("getResource [{}]", classLoader.getResource(propName).getPath());
                    InputStream inStream = classLoader.getResourceAsStream(propName);
                    props.load(inStream);
                    propsMap.put(propName, props);
                } catch (IOException e) {
                    throw new RuntimeException("There's no resource file named [" + propName + "]", e);
                }
            }
        }
        return props;
    }

    private Properties getResourceBypath(String propName) {
        Properties props = propsMap.get(propName);
        if (props == null) {
            synchronized (this) {
                logger.info("new Properties(), propName = " + propName);
                try {
                    Resource resource = new ClassPathResource(propName);
                    EncodedResource enResource = new EncodedResource(resource, "UTF-8");
                    props = PropertiesLoaderUtils.loadProperties(enResource);
                    logger.info("loadProperties [{}]", propName);
                    propsMap.put(propName, props);
                } catch (IOException e) {
                    throw new RuntimeException("There's no resource file named [" + propName + "]", e);
                }
            }
        }
        return props;
    }

}
