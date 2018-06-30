package hust.zeng.utils.staticm;

import java.io.File;
import java.net.URL;

/**
 * @title PathUtil
 * @author zengzhihua
 */
public class PathUtil {

    /**
     * 获取类(或者jar)所在路径
     * 
     * @return
     */
    public static String getProjectPath() {

        URL url = PathUtil.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filePath.endsWith(".jar")) {
            filePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
        }
        File file = new File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }

}
