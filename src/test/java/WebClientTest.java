import cn.xuben.net.WebClientAgent;
import cn.xuben.net.UserAgentsLib;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;

public class WebClientTest {

    public void testEncode0() {
        try {
            WebClientAgent agent = WebClientAgent.getInstance();
            URL url = new URL("https://www.jd.com");
            System.err.println(agent.getEncodingName(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testEncode1() {
        String[] urlStrArr = {"http://www.cnblogs.com/moonbay", "https://www.cnblogs.com/moonbay", "https://www.jd.com", "http://www.baidu.com", "https://www.baidu.com", "http://www.gov.cn"};
        try {
            WebClientAgent agent = WebClientAgent.getInstance();
            for (String urlStr : urlStrArr) {
                URL url = new URL(urlStr);
                System.err.printf("url:%s\t%s\n", urlStr, agent.getEncodingName(url));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void test2() throws Exception {
        WebClientAgent muc = WebClientAgent.getInstance();
//		String urlstr = "http://www.baidu.com";
        String urlstr = "http://hd.weixin.kongzhong.com/index.php?g=Wap&m=Guajiang&a=index&token=xsubpm1404353542&wecha_id=ocXrWjm2dEKJ1qPkhq2L9wZ1KQ20&id=58";
//		String urlstr = "http://acm.bjfu.edu.cn/ben/test.jsp";
        URL url = new URL(urlstr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestProperty("User-Agent", microAgentstr);
        con.connect();
        System.out.println(muc.getContentFromConn(con, Charset.forName("utf-8")));
    }

    public void test3() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
//        URL url = new URL("https://www.jd.com");
        URL url = new URL("http://acm.bjfu.edu.cn");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", UserAgentsLib.defaultOfChrome());
        conn.setConnectTimeout(30000);
//        conn.setReadTimeout(30000);
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        System.out.println(code);
    }

    public void testGetSimply() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
//        String url = "https://www.jd.com";
        String url = "https://www.baidu.com";
        System.out.println(muc.doGetSimply(url, "utf-8"));
    }

    public void testUserAgent() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
        URL url = new URL("https://www.baidu.com");
        System.out.println(muc.doGetDirectlyInDefaultTime(url, null, Charset.forName("utf-8")));
        System.out.println();
        System.out.flush();
        System.err.println(muc.doGetDirectlyInDefaultTime(url, UserAgentsLib.defaultOfChrome(), Charset.forName("utf-8")));
    }

    public void testUserAgent2() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
        URL url = new URL("https://www.baidu.com/s?ie=utf-8&wd=手机");
        System.out.println(muc.doGetDirectlyInDefaultTime(url, null, Charset.forName("utf-8")));
        System.out.println();
        System.out.flush();
        System.err.println(muc.doGetDirectlyInDefaultTime(url, UserAgentsLib.defaultOfChrome(), Charset.forName("utf-8")));
    }

    public void testTimeout() throws IOException {
        WebClientAgent agent = WebClientAgent.getInstance();
//        String urlStr = "https://www.jd.com";
        String urlStr = "http://acm.bjfu.edu.cn";
        URL url = new URL(urlStr);
//        StringBuilder str = agent.doGetFromUrl(url, MyUserAgentsLib.getAgentsOfChrome(), 20 * 1000, 40 * 1000, Charset.forName("utf-8"));
//        StringBuilder str = agent.doGetFromUrlInDefaultTime(url, MyUserAgentsLib.getAgentsOfChrome(), Charset.forName("utf-8"));
        StringBuilder str = agent.doGetByDefault(urlStr, UserAgentsLib.defaultOfChrome(), "utf-8");
        System.err.println(str);
    }

    public void testPost() throws IOException {
        WebClientAgent agent = WebClientAgent.getInstance();
        String urlStr = "https://www.baidu.com/s";
        String postData = "ie=utf-8&wd=phone";
        System.out.println(agent.doPost(new URL(urlStr), postData));
    }

    @Test
    public void testProxy() throws IOException {
        InetSocketAddress addr = new InetSocketAddress("112.65.53.177",4275);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
//        Proxy proxy = null;
        WebClientAgent agent = WebClientAgent.getInstance();
        URL url = new URL("https://www.baidu.com/s?ie=utf-8&wd=隆胸");
        StringBuilder str = agent.doGetByProxyInDefaultTime(url, proxy, UserAgentsLib.defaultOfChrome(), Charset.forName("utf-8"));
        System.err.println(str);
    }

}
