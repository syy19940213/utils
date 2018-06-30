package hust.zeng.utils.staticm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @title MD5工具类
 * @author zengzhihua
 */
public class MD5Util {

    /**
     * 生成文件内容的MD5校验码
     * @param fileName
     * @return
     */
    public static String MD5File(String fileName) {
        File file = new File(fileName);
        InputStream data = null;
        try {
            data = new FileInputStream(file);
            return MD5Stream(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (data != null) {
                    data.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    /**
     * 生成输入流的MD5校验码
     * @param data
     * @return
     */
    public static String MD5Stream(InputStream data) {
        String md5Hex = null;
        try {
            md5Hex = DigestUtils.md5Hex(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5Hex;
    }

    /**
     * 生成字符串的MD5校验码
     * 
     * @param src
     * @return
     */
    public static String MD5String(String src) {
        String md5Hex = DigestUtils.md5Hex(src);
        return md5Hex;
    }
}
