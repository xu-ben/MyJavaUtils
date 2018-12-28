package ben.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建日期2013-12-6
 */
public final class MyURLConnector {

    /**
     * 全局唯一实例
     */
    private static MyURLConnector conn = null;

    private MyURLConnector() {
    }


    /**
     * 试图从连接的header中解析出charset name
     *
     * @return charset name或null(未解析到)
     */
    private String getEncodingName(URLConnection conn) {
        String ret = conn.getContentEncoding();
        if (ret != null) {
            return ret;
        }
        ret = conn.getContentType();
        if (ret == null) {
            return null;
        }
        int index = ret.toLowerCase().indexOf("charset=");
        if (index != -1) {
            return ret.substring(index + 8);
        }
        return null;
    }

    /**
     * 获取编码名称字符串
     *
     * @return 解析获得的编码名称字符串，若解析不到编码，则返回null
     */
    public String getEncodingName(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        /**
         * 如果能从header中解析出charset，则返回
         */
        String result = getEncodingName(conn);
        if (result != null) {
            return result;
        }
        /**
         * 如果不能从header中解析出charset，则解析网页meta从中获取charset
         * TODO 优化
         * 目前这种写法是有问题的，一是应该只解析<head></head>内部, 二是不能按行读
         */
        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        // example1:  <meta charset="UTF-8">
        // example2: <meta http-equiv="content-type" content="text/html;charset=utf-8">
        String regex = "<\\s*meta[^>]*content=\"[^\"]*charset\\s*=\\s*([a-zA-Z_0-9-]+)\\s*\"";
        Pattern pa = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Pattern pb = Pattern.compile(".*<\\s*body\\s*>.*", Pattern.CASE_INSENSITIVE);
        String line;
//        pa.matcher()
        while ((line = br.readLine()) != null) {
            Matcher m = pa.matcher(line);
            if (m.find()) {
                br.close();
                return m.group(1);
            } else {
                if (pb.matcher(line).matches()) {
                    break;
                }
            }
        }
        // 以上两种方法均解析不出，则返回null
        br.close();
        return null;
    }

    /**
     * 给定一个url，返回其内容
     * <b>注意，这会访问两次，第一次是为了取编码</b>
     *
     * @param urlStr 指定的url
     * @return
     * @throws IOException
     */
    public String getContent(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        URLConnection uc = url.openConnection();
        String encoding = getEncodingName(uc);
        return getContent(uc, encoding);
    }

   /**
     * 给定一个url连接及其网页的编码，返回其网页内容文本
     *
     * @param uc
     * @param encoding
     * @return
     * @throws IOException
     */
    public String getContent(URLConnection uc, String encoding) throws IOException {
        uc.connect();
        InputStream is = uc.getInputStream();
        return getTextFromStreamReader(is, Charset.forName(encoding));
    }

    /**
     * @return 一个本类的实例
     */
    public static synchronized MyURLConnector getInstance() {
        if (conn == null) {
            conn = new MyURLConnector();
        }
        return conn;
    }

}
