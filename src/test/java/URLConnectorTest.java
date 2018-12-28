import ben.net.MyURLConnector;
import ben.net.MyUserAgentsLib;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLConnectorTest {

    public void testEncode0() {
        try {
            MyURLConnector spider = MyURLConnector.getInstance();
            URL url = new URL("https://www.jd.com");
            System.err.printf("%s\n", spider.getEncodingName(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testEncode1() {
        String[] urlStrArr = {"http://www.cnblogs.com/moonbay", "https://www.cnblogs.com/moonbay", "https://www.jd.com", "http://www.baidu.com", "https://www.baidu.com", "http://www.gov.cn"};
        try {
            MyURLConnector spider = MyURLConnector.getInstance();
            for (String urlStr : urlStrArr) {
                URL url = new URL(urlStr);
                System.err.printf("url:%s\t%s\n", urlStr, spider.getEncodingName(url));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void test2() throws Exception {
        MyURLConnector muc = MyURLConnector.getInstance();
//		String urlstr = "http://www.baidu.com";
        String urlstr = "http://hd.weixin.kongzhong.com/index.php?g=Wap&m=Guajiang&a=index&token=xsubpm1404353542&wecha_id=ocXrWjm2dEKJ1qPkhq2L9wZ1KQ20&id=58";
//		String urlstr = "http://acm.bjfu.edu.cn/ben/test.jsp";
        URL url = new URL(urlstr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestProperty("User-Agent", microAgentstr);
        con.connect();
        System.out.println(muc.getContent(con, "utf-8"));
    }

    @Test
    public void test3() throws IOException {
        MyURLConnector muc = MyURLConnector.getInstance();
        URL url = new URL("https://www.jd.com");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		con.setRequestProperty("User-Agent", microAgentstr);
        con.setRequestProperty("User-Agent", MyUserAgentsLib.getAgentsOfChrome());
        con.connect();
        System.out.println(muc.getContent(con, "utf-8"));
    }

}
