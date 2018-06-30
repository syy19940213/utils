package hust.zeng.utils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTP工具类
 * @title FtpUtils
 * @author zengzhihua
 */
public class FtpUtils {
    private static final Logger logger = LoggerFactory.getLogger(FtpUtils.class);
    private static final String CHARSET_NAME = "UTF-8";

    private FTPClient ftpClient = null;
    private String server;
    private int port;
    private String userName;
    private String userPassword;

    public FtpUtils(String server, int port, String userName, String userPassword) {
        this.server = server;
        this.port = port;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    /**
     * 连接服务器
     * 
     * @return 连接成功与否 true:成功， false:失败
     * @throws Exception
     */
    public boolean open() throws Exception {
        if (ftpClient != null && ftpClient.isConnected()) {
            return true;
        }
        try {
            ftpClient = new FTPClient();
            // 连接
            ftpClient.connect(this.server, this.port);
            ftpClient.login(this.userName, this.userPassword);
            setFtpClient(ftpClient);
            // 检测连接是否成功
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.close();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
            logger.info("FtpUtils: FTP connect! IP:{}, port:{}, userName:{}", this.server, this.port, this.userName);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return true;
        } catch (Exception ex) {
            this.close();
            throw ex;
        }
    }

    /**
     * 切换到父目录
     * 
     * @return 切换结果 true：成功， false：失败
     */
    public boolean changeToParentDir() {
        try {
            return ftpClient.changeToParentDirectory();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 改变当前目录到指定目录
     * 
     * @param dir 目的目录
     * @return 切换结果 true：成功，false：失败
     */
    private boolean cd(String dir) {
        try {
            return ftpClient.changeWorkingDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取目录下所有的文件名称
     * 
     * @param filePath 指定的目录
     * @return 文件列表,或者null
     */
    private FTPFile[] getFileList(String filePath) {
        try {
            ftpClient.enterLocalPassiveMode();
            return ftpClient.listFiles(filePath);
        } catch (IOException e) {
            logger.error("FtpUtils:ftpclient list file exception", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 层层切换工作目录
     * 
     * @param ftpPath 目的目录
     * @return 切换结果
     */
    public boolean changeDir(String ftpPath) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            // 将路径中的斜杠统一
            char[] chars = ftpPath.toCharArray();
            StringBuffer sbStr = new StringBuffer(256);
            for (int i = 0; i < chars.length; i++) {
                if ('\\' == chars[i]) {
                    sbStr.append('/');
                } else {
                    sbStr.append(chars[i]);
                }
            }
            ftpPath = sbStr.toString();
            if (ftpPath.indexOf('/') == -1) {
                // 只有一层目录
                ftpClient.changeWorkingDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
            } else {
                // 多层目录循环创建
                String[] paths = ftpPath.split("/");
                for (int i = 0; i < paths.length; i++) {
                    ftpClient.changeWorkingDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 循环创建目录，并且创建完目录后，设置工作目录为当前创建的目录下
     * 
     * @param ftpPath 需要创建的目录
     * @return
     */
    public boolean mkDir(String ftpPath) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            // 将路径中的斜杠统一
            char[] chars = ftpPath.toCharArray();
            StringBuffer sbStr = new StringBuffer(256);
            for (int i = 0; i < chars.length; i++) {
                if ('\\' == chars[i]) {
                    sbStr.append('/');
                } else {
                    sbStr.append(chars[i]);
                }
            }
            ftpPath = sbStr.toString();
            if (ftpPath.indexOf('/') == -1) {
                // 只有一层目录
                ftpClient.makeDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
                ftpClient.changeWorkingDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
            } else {
                // 多层目录循环创建
                String[] paths = ftpPath.split("/");
                for (int i = 0; i < paths.length; i++) {
                    ftpClient.makeDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
                    ftpClient.changeWorkingDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
                }
            }
            logger.info("FtpUtils:新建文件夹：{}", ftpPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传文件到FTP服务器
     * 
     * @param localDirectoryAndFileName 本地文件目录和文件名
     * @param ftpFileName 上传到服务器的文件名
     * @param ftpDirectory FTP目录如:/path1/pathb2,如果目录不存在会自动创建目录
     * @return
     */
    public boolean upload(String localDirectoryAndFileName, String ftpFileName, String ftpDirectory) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        if (!ftpDirectory.endsWith("/")) {
            ftpDirectory = ftpDirectory + "/";
        }
        boolean flag = false;
        if (ftpClient != null) {
            File srcFile = new File(localDirectoryAndFileName);
            logger.info("FtpUtils:准备上传文件：{}", srcFile.getName());
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(srcFile);
                // 如果目录不存在，则创建
                if (!this.cd(ftpDirectory)) {
                    this.mkDir(ftpDirectory);
                }
                this.changeDir(ftpDirectory);
                ftpClient.setBufferSize(100000);
                // 设置编码格式
                ftpClient.setControlEncoding(CHARSET_NAME);
                // 设置文件传输类型（二进制）
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 上传
                ftpClient.enterLocalPassiveMode();
                flag = ftpClient.storeFile(new String(ftpFileName.getBytes(), CHARSET_NAME), fis);
            } catch (Exception e) {
                logger.error("FtpUtils:上传文件时发生异常！", e);
                this.close();
                e.printStackTrace();
                return false;
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("FtpUtils:上传文件成功！路径：{}", ftpDirectory);
        return flag;
    }

    /**
     * 从FTP服务器上下载文件
     * 
     * @param ftpDirectoryAndFileName ftp服务器文件路径，以/dir形式开始
     * @param localDirectoryAndFileName 保存到本地的目录
     * @return
     */
    public boolean get(String ftpDirectoryAndFileName, String localDirectoryAndFileName) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        ftpClient.enterLocalPassiveMode(); // Use passive mode as default
        try {
            // 将路径中的斜杠统一
            char[] chars = ftpDirectoryAndFileName.toCharArray();
            StringBuffer sbStr = new StringBuffer(256);
            for (int i = 0; i < chars.length; i++) {
                if ('\\' == chars[i]) {
                    sbStr.append('/');
                } else {
                    sbStr.append(chars[i]);
                }
            }
            ftpDirectoryAndFileName = sbStr.toString();
            String filePath = ftpDirectoryAndFileName.substring(0, ftpDirectoryAndFileName.lastIndexOf("/"));
            String fileName = ftpDirectoryAndFileName.substring(ftpDirectoryAndFileName.lastIndexOf("/") + 1);
            this.changeDir(filePath);
            ftpClient.enterLocalPassiveMode();
            ftpClient.retrieveFile(new String(fileName.getBytes(), "iso-8859-1"),
                    new FileOutputStream(localDirectoryAndFileName)); // download
            // file
            logger.info(ftpClient.getReplyString()); // check result
            logger.info("FtpUtils:从ftp服务器上下载文件：" + ftpDirectoryAndFileName + "， 保存到：" + localDirectoryAndFileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回FTP目录下的文件列表
     * 
     * @param pathName
     * @return
     */
    public String[] getFilePathList(String pathName) {
        try {
            return ftpClient.listNames(pathName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getFileNameList(String pathName) {
        logger.info("FtpUtils:pathName=" + pathName);
        int i = 0;
        // 遍历
        FTPFile[] list = getFileList(pathName);
        String names[] = new String[list.length];
        for (FTPFile file : list) {
            String name = file.getName();
            names[i] = name;
            i++;
        }
        logger.info("FtpUtils:getFileList size=" + names.length);
        return names;
    }

    /**
     * 删除FTP上的文件
     * 
     * @param ftpDirAndFileName 路径开头不能加/，比如应该是test/filename1
     * @return
     */
    public boolean deleteFile(String ftpDirAndFileName) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            logger.info("FtpUtils:删除文件");
            return ftpClient.deleteFile(ftpDirAndFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除FTP目录
     * 
     * @param ftpDirectory
     * @return
     */
    public boolean deleteDirectory(String ftpDirectory) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            logger.info("ftpDirectory: {}", ftpDirectory);
            return ftpClient.removeDirectory(ftpDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭链接
     */
    public void close() {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
            logger.info("FtpUtils: FTP disconnect! IP:{}, port:{}", this.server, this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

}
