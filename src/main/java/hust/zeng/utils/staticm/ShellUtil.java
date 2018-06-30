package hust.zeng.utils.staticm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.ethz.ssh2.StreamGobbler;
import hust.zeng.utils.pojo.CommandResult;

/**
 * Shell工具类
 * @title ShellUtil
 * @author zengzhihua
 */
public class ShellUtil {

    public static CommandResult exeCommand(String cmd) throws IOException {

        CommandResult result = new CommandResult();
        String[] cmdarray = { "/bin/bash", "-c", cmd };
        Process process = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec(cmdarray);
            br = new BufferedReader(new InputStreamReader(new StreamGobbler(process.getInputStream())));
            StringBuffer sb = new StringBuffer();
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
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

}
