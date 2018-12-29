import cn.xuben.net.WebClientAgent;
import cn.xuben.net.MyUserAgentsLib;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        System.out.println(muc.getContent(con, Charset.forName("utf-8")));
    }

    public void test3() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
        URL url = new URL("https://www.jd.com");
        System.out.println(muc.getContent(url, MyUserAgentsLib.getAgentsOfChrome(), Charset.forName("utf-8")));
    }

    public void testUserAgent() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
        URL url = new URL("https://www.baidu.com/s?ie=utf-8&wd=手机");
        System.out.println(muc.getContent(url, "", Charset.forName("utf-8")));
        System.out.println();
        System.out.flush();
        System.err.println(muc.getContent(url, MyUserAgentsLib.getAgentsOfChrome(), Charset.forName("utf-8")));
    }


    @Test
    public void testTimeout() throws IOException {
        WebClientAgent muc = WebClientAgent.getInstance();
        URL url = new URL("http://acm.bjfu.edu.cn");
        System.out.println(muc.getContent(url, MyUserAgentsLib.getAgentsOfChrome(), Charset.forName("utf-8")));
    }

}
