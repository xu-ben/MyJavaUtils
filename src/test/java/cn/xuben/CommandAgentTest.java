package cn.xuben;

import cn.xuben.CommandAgent;
import cn.xuben.net.UserAgentsLib;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class CommandAgentTest {

    public static StringBuilder spliceCurlCmd(String url, String userAgent, InetSocketAddress addr, String target) {
        String proxy = null;
        if (addr != null) {
            proxy = addr.getHostName() + ":" + addr.getPort();
        }
        return spliceCurlCmd(url, userAgent, proxy, target);
    }

    public static StringBuilder spliceCurlCmd(String url, String userAgent, String proxy, String target) {
        StringBuilder cmd = new StringBuilder("curl ");
        if (userAgent != null) {
            cmd.append('-').append('A').append(' ').append('\"');
            cmd.append(userAgent).append('\"').append(' ');
        }
        if (proxy != null) {
            cmd.append('-').append('x').append(' ').append(proxy).append(' ');
        }
        cmd.append('\"').append(url).append('\"');
        if (target != null) {
            cmd.append(' ').append('>').append(' ').append(target);
        }
        return cmd;
    }

    public static StringBuilder runCurl(String url, String userAgent, InetSocketAddress addr, String target) throws IOException {
        StringBuilder cmd = spliceCurlCmd(url, userAgent, addr, target);
        CommandAgent agent = CommandAgent.getInstance();
        return agent.runShellCmdInPath(cmd.toString(), ".", 30);
    }

    public void test0() throws IOException {
        CommandAgent agent = CommandAgent.getInstance();
//        System.err.println(agent.runShellCmdSimply("ls -lah > a.txt"));
        System.err.println(agent.runShellCmdSimply("ls -lah"));
    }

    @Test
    public void testRunCurl() throws IOException {
        String url = "https://www.baidu.com/s?ie=utf-8&wd=手机";
//        InetSocketAddress addr = new InetSocketAddress("112.65.53.177", 4275);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
//        System.err.println(CurlTools.spliceCurlCmd(url, UserAgentsLib.defaultOfChrome(), addr, "a.txt"));
        System.err.println(runCurl(url, UserAgentsLib.defaultOfChrome(), null, null));
    }

}
