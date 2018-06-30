package hust.zeng.utils.staticm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import hust.zeng.utils.pojo.CommandResult;

/**
 * SSH工具类
 * @title SSHUtil
 * @author zengzhihua
 */
public class SSHUtil {

    private static Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    /**
     * 私钥文件路径
     */
    private static final String RSA_FILE_PATH = "~/.ssh/id_rsa";

    /**
     * 使用密码建立连接，默认22端口
     * 
     * @param ip
     * @param user
     * @param password
     * @return Connection
     * @throws IOException
     */
    public static Connection getConnection(String ip, String user, String password) throws IOException {
        return getConnectionWithPassword(ip, null, user, password);
    }

    /**
     * 使用密码建立连接
     * 
     * @param ip
     * @param port can be set to null
     * @param user
     * @param password
     * @return Connection
     * @throws IOException
     */
    public static Connection getConnection(String ip, Integer port, String user, String password) throws IOException {
        return getConnectionWithPassword(ip, port, user, password);
    }

    /**
     * 使用公钥免密登录，默认22端口
     * 
     * @param ip
     * @param user
     * @return
     * @throws IOException
     */
    public static Connection getConnection(String ip, String user) throws IOException {

        return getConnectionWithPublicKey(ip, null, user, null, null);
    }

    /**
     * 使用公钥免密登录
     * 
     * @param ip
     * @param port can be set to null
     * @param user
     * @return
     * @throws IOException
     */
    public static Connection getConnection(String ip, Integer port, String user) throws IOException {

        return getConnectionWithPublicKey(ip, port, user, null, null);
    }

    /**
     * 使用公钥免密登录
     * 
     * @param ip
     * @param port can be set to null
     * @param user
     * @param pemFile can be set to null. A File object pointing to a file
     *            containing a DSA or RSA private key of the user in OpenSSH key
     *            format (PEM, you can't miss the "-----BEGIN DSA PRIVATE
     *            KEY-----" or "-----BEGIN RSA PRIVATE KEY-----" tag).
     * @param pemFilePassword can be set to null. If the PEM file is encrypted
     *            then you must specify the password. Otherwise, this argument
     *            will be ignored and can be set to null.
     * @return Connection
     * @throws IOException
     */
    public static Connection getConnection(String ip, Integer port, String user, File pemFile, String pemFilePassword)
            throws IOException {
        return getConnectionWithPublicKey(ip, port, user, pemFile, pemFilePassword);
    }

    /**
     * 执行Linux命令
     * 
     * @param connection 执行完毕后并不关闭连接
     * @param command 一个或者多个linux命令
     * @return ExeCommandResult
     * @throws IOException
     */
    public static CommandResult execCommand(Connection connection, String command) throws IOException {
        Session session = null;
        CommandResult exeCommandResult = new CommandResult();
        StringBuffer printInfo = new StringBuffer();
        BufferedReader br = null;
        try {
            session = connection.openSession();
            printInfo.append("open a session ").append("<br>\n");
            session.execCommand(command);
            br = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            printInfo.append("execute command:").append(command).append(",print:").append("<br>\n");
            StringBuffer sb = new StringBuffer();
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                sb.append(line).append("<br>\n");
            }
            session.waitForCondition(ChannelCondition.EXIT_STATUS, 5000);
            Integer status = session.getExitStatus();
            logger.info("the command(" + command + ")execute result status " + status + " print :<br>\n" + sb);
            exeCommandResult.setExitStatus(status);
            printInfo.append(sb).append("<br>\n");
            printInfo.append("execute result status ").append(status).append("<br>\n");
        } finally {
            if (br != null)
                br.close();
            if (session != null) {
                session.close();
            }
            printInfo.append("closed session").append("<br>\n");
        }
        exeCommandResult.setPrintInfo(printInfo.toString());
        return exeCommandResult;
    }

    private static Connection getConnectionWithPassword(String ip, Integer port, String user, String password)
            throws IOException {
        if (port == null) {
            port = 22;
        }
        logger.info("ssh connect ip:{}, port:{}, username:{}", ip, port, user);
        Connection conn = new Connection(ip, port);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithPassword(user, password);
        if (!isAuthenticated) {
            throw new RuntimeException(String.format("ssh connet to %s:%s fail!", ip, port));
        }
        logger.info("ssh connet to %s:%s success!", ip, port);
        return conn;
    }

    private static Connection getConnectionWithPublicKey(String ip, Integer port, String user, File pemFile,
            String pemFilePassword) throws IOException {
        if (port == null) {
            port = 22;
        }
        if (pemFile == null) {
            pemFile = new File(RSA_FILE_PATH);
        }
        logger.info("ssh connect ip:{}, port:{}, username:{}", ip, port, user);
        Connection conn = new Connection(ip, port);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pemFilePassword);
        if (!isAuthenticated) {
            throw new RuntimeException(String.format("ssh connet to %s:%s fail!", ip, port));
        }
        logger.info("ssh connet to %s:%s success!", ip, port);
        return conn;
    }

}
