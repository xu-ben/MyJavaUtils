package cn.xuben;

import java.io.*;

/**
 * TODO 包装一下执行命令的返回结果，里面应该放上命令的退出状态等
 */
public final class CommandAgent {

    /**
     * 全局唯一实例
     */
    private static CommandAgent agent = null;

    private CommandAgent() {
    }


    /**
     * 在当前目录(".")下，阻塞式执行shell命令，返回命令在stdout中的输出内容
     * <b>这是在linux系统下使用</b>
     *
     * @return <b>即使没有输出，也不会为null</b>
     */
    public StringBuilder runShellCmdSimply(String cmd) throws IOException {
        return runShellCmdInPath(cmd, ".", -1);
    }

    /**
     * 执行cmd命令，如果timeout <= 0，则阻塞式执行，否则，最多等待timeout秒即返回
     *
     * @param dirPath 执行命令时的所在路径
     * @param timeout 单位为秒
     * @return 所执行的命令在stdout中输出的内容, 如果没有输出不会为null。如果命令执行因为超时被kill，则返回null
     */
    public StringBuilder runShellCmdInPath(String cmd, String dirPath, int timeout) throws IOException {
        if (cmd == null) {
            throw new NullPointerException();
        }
        File d = new File(dirPath);
        if (!d.exists() || !d.isDirectory()) {
            throw new FileNotFoundException();
        }
        if (cmd.trim().isEmpty()) {
            throw new IOException("cmd error");// todo 优化
        }
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        String[] cmds = {"/bin/sh", "-c", cmd};
        if (timeout < 0) {
            myExecSync(cmds, d, stdout, stderr);
        } else {
            if (!myExecAsync(cmds, d, timeout, stdout, stderr)) {
                return null;
            }
        }
        return stdout;
    }


    /**
     *  TODO 待测试
     */
    public StringBuilder runCmdInWindows(String cmd, int timeout) throws IOException {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        String osName = System.getProperty("os.name");
        String[] cmds = null;
        if (osName.equals("Windows NT")) {
            cmds = new String[]{"cmd.exe", "/C", cmd};
        } else if (osName.equals("Windows 95")) {
            cmds = new String[]{"command.com", "/C", cmd};
        }
        if (cmds == null) {
            throw new UnknownError(); // todo
        }
        if (timeout < 0) {
            myExecSync(cmds, null, stdout, stderr);
        } else {
            if (!myExecAsync(cmds, null, timeout, stdout, stderr)) {
                return null;
            }
        }
        return stdout;
    }

    /**
     * 同步执行cmd命令，方法会阻塞至运行的子进程结束
     */
    private void myExecSync(String[] cmds, File dir, StringBuilder out, StringBuilder err) throws IOException {
        Process proc = null;
        BufferedReader brOut = null, brErr = null;
        char[] buf = new char[512];
        int len;
        try {
            proc = Runtime.getRuntime().exec(cmds, null, dir);
            brOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            brErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((len = brOut.read(buf)) > 0) {
                out.append(buf, 0, len);
            }
            while ((len = brErr.read(buf)) > 0) {
                err.append(buf, 0, len);
            }
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (proc != null) {
                proc.destroy();
            }
            if (brOut != null) {
                brOut.close();
            }
            if (brErr != null) {
                brErr.close();
            }
        }
    }

    /**
     * 异步执行cmd命令，最多等待timeout秒
     *
     * @param timeout 单位：秒
     */
    private boolean myExecAsync(String[] cmds, File dir, int timeout, StringBuilder out, StringBuilder err) throws IOException {
        long start = System.currentTimeMillis();
        Process proc = null;
        BufferedReader brOut = null, brErr = null;
        char[] buf = new char[512];
        int len;
        try {
            proc = Runtime.getRuntime().exec(cmds, null, dir);
            brOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            brErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (true) {
                while (brOut.ready()) {
                    if ((len = brOut.read(buf)) > 0) {
                        out.append(buf, 0, len);
                    }
                }
                while (brErr.ready()) {
                    if ((len = brErr.read(buf)) > 0) {
                        err.append(buf, 0, len);
                    }
                }
                try {
                    proc.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {// 进程未结束会到这
                }
                if (System.currentTimeMillis() - start > timeout * 1000) {
//					System.err.println("命令执行超时退出.\n");
                    return false;
                }
                Thread.sleep(50);
            }
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (proc != null) {
                proc.destroy();
            }
            if (brOut != null) {
                brOut.close();
            }
            if (brErr != null) {
                brErr.close();
            }
        }
        return true;
    }

    /**
     * @return 一个本类的实例
     */
    public static synchronized CommandAgent getInstance() {
        if (agent == null) {
            agent = new CommandAgent();
        }
        return agent;
    }

}
