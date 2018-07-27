package hust.zeng.utils.staticm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import ch.ethz.ssh2.StreamGobbler;
import hust.zeng.utils.pojo.CommandResult;

/**
 * Shell工具类
 * 
 * @title ShellUtil
 * @author zengzhihua
 */
public class ShellUtil {

    private static Logger logger = LoggerFactory.getLogger(ShellUtil.class);

    public static CommandResult exeCommand(String cmd) throws IOException {

        CommandResult result = new CommandResult();
        String[] cmdarray = { "/bin/bash", "-c", cmd };
        Process process = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec(cmdarray);
            br = new BufferedReader(new InputStreamReader(new StreamGobbler(process.getInputStream())));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            try {
                result.setExitStatus(process.waitFor());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result.setPrintInfo(sb.toString());
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }

    /**
     * 远程执行命令，22端口，不设置连接超时
     * 
     * @param ipAddress
     * @param username
     * @param password
     * @param command
     * @return
     * @throws IOException
     */
    public static CommandResult remoteExec(String ipAddress, String username, String password, String command) throws IOException {
        return remoteExec(ipAddress, username, password, 22, command, 0);
    }

    /**
     * 远程执行命令，不设置连接超时
     * 
     * @param ipAddress
     * @param username
     * @param port
     * @param password
     * @param command
     * @return
     * @throws IOException
     */
    public static CommandResult remoteExec(String ipAddress, String username, int port, String password, String command) throws IOException {
        return remoteExec(ipAddress, username, password, port, command, 0);
    }

    /**
     * 远程执行命令，22端口
     * 
     * @param ipAddress
     * @param username
     * @param password
     * @param command
     * @return
     * @param connectTimeout
     * @throws IOException
     */
    public static CommandResult remoteExec(String ipAddress, String username, String password, String command, int connectTimeout) throws IOException {
        return remoteExec(ipAddress, username, password, 22, command, connectTimeout);
    }

    /**
     * 远程执行命令
     * 
     * @param ipAddress
     * @param username
     * @param password
     * @param port
     * @param command
     * @return
     * @param connectTimeout
     * @throws IOException
     * @throws JSchException
     */
    public static CommandResult remoteExec(String ipAddress, String username, String password, int port, String command, int connectTimeout)
            throws IOException {
        logger.info("The remote command is: " + command);
        long startTime = System.currentTimeMillis();
        CommandResult exeCommandResult = new CommandResult();
        JSch jsch = new JSch();
        UserInfo userInfo = new UserInfo() {
            @Override
            public void showMessage(String message) {}

            @Override
            public boolean promptYesNo(String message) {
                return false;
            }

            @Override
            public boolean promptPassword(String message) {
                return false;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return false;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getPassphrase() {
                return null;
            }
        };

        Session session = null;
        Channel channel = null;
        BufferedReader br = null;
        try {
            // Create and connect session.
            session = jsch.getSession(username, ipAddress, port);
            session.setPassword(password);
            session.setUserInfo(userInfo);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(connectTimeout);
            long endTime = System.currentTimeMillis();

            // Create and connect channel.
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect();

            // Get the output of remote command.
            br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            exeCommandResult.setPrintInfo(sb.toString());

            // Get the return code only after the channel is closed.
            if (channel.isClosed()) {
                logger.info("Remote command exec success, connect cost {}s, exec cost {}s", (endTime - startTime) / 1000,
                        (System.currentTimeMillis() - endTime) / 1000);
                exeCommandResult.setExitStatus(channel.getExitStatus());
            } else {
                exeCommandResult.setExitStatus(-1);
            }
        } catch (JSchException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (br != null) {
                br.close();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return exeCommandResult;
    }
}
